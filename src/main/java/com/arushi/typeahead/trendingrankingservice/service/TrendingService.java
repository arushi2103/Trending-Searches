package com.arushi.typeahead.trendingrankingservice.service;

import com.arushi.typeahead.trendingrankingservice.model.SearchTerm;
import com.arushi.typeahead.trendingrankingservice.repository.SearchTermRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;


@Slf4j
@Service
public class TrendingService {
    // This class is responsible for handling the trending search terms.
    // It will interact with the SearchTermRepository to fetch and update search terms.
    // The methods will include:
    // - getTrendingTerms(String prefix): Fetches top 10 trending terms based on the prefix.
    // - updateSearchTermFrequency(String term): Updates the frequency of a search term.
    // - addNewSearchTerm(String term): Adds a new search term to the database.

    private  final RedisTemplate<String, String> redisTemplate;
    private  final SearchTermRepository repository;
    public TrendingService(SearchTermRepository repository, RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.repository = repository;
    }

    public List<String> getTrendingTerms() {
        // Fetch top 10 trending terms based on the prefix
        try{
        return repository.findTop10ByOrderByFrequencyDesc()
                .stream()
                .map(SearchTerm::getTerm)
                .toList();
        }catch(DataAccessException e){
            log.error(" Database error while fetching trending terms:{}",e.getMessage(),e);
            throw new RuntimeException("Failed to fetch trending terms from the database");
        }
    }
    public List<String> getTrendingTerms(String prefix) {
        // Fetch top 10 trending terms based on the prefix
        String cacheKey = "trending:" + prefix.toLowerCase();
//          Check redis
        try{
        if(redisTemplate.hasKey(cacheKey)) {
            log.info("✅ Cache hit for prefix: {}", prefix);
            return redisTemplate.opsForList().range(cacheKey, 0, 9);
        }else {
            log.info("❌ Cache miss. Fetching from DB for prefix: {}", prefix);
            List<String> trendingTerms = repository.findTop10ByTermStartingWithOrderByFrequencyDesc(prefix)
                    .stream()
                    .map(SearchTerm::getTerm)
                    .toList();
            log.info(trendingTerms.toString());

            if(trendingTerms.isEmpty()){
                log.info("⚠️ No trending terms found for prefix: {}",prefix);
            }else{
                log.info("✅ Cached trending terms for prefix: {}: {}", prefix, trendingTerms);
                // Cache the results in Redis
                redisTemplate.opsForList().rightPushAll(cacheKey, trendingTerms.toArray(new String[0]));
                redisTemplate.expire(cacheKey, Duration.ofHours(1));

            }
            return trendingTerms;
        }
        }catch(RedisConnectionFailureException e){
            log.warn("⚠️ Redis unavailable. Falling back to DB for prefix: {}", prefix);
            return repository.findTop10ByTermStartingWithOrderByFrequencyDesc(prefix)
                    .stream()
                    .map(SearchTerm::getTerm)
                    .toList();
        } catch (RuntimeException e) {
            log.warn("⚠️ Redis runtime error. Falling back to DB for prefix: {}", prefix, e);
            return repository.findTop10ByTermStartingWithOrderByFrequencyDesc(prefix)
                    .stream()
                    .map(SearchTerm::getTerm)
                    .toList();
        } catch (Exception e) {
            log.error("Unexpected error:{}",e.getMessage(),e);
            throw new RuntimeException("An unexpected error occurred.");
        }
    }
}
