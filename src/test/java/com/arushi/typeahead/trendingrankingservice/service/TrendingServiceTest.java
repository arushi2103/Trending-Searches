package com.arushi.typeahead.trendingrankingservice.service;


import com.arushi.typeahead.trendingrankingservice.model.SearchTerm;
import com.arushi.typeahead.trendingrankingservice.repository.SearchTermRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.beans.PropertyEditorSupport;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class TrendingServiceTest {

    private TrendingService trendingService;
    private SearchTermRepository mockRepository;
    private RedisTemplate<String, String> mockRedisTemplate;
    private ListOperations<String, String> mockListOps;

    @BeforeEach
    public void setUp(){
        mockRepository=mock(SearchTermRepository.class);
        mockRedisTemplate=mock(RedisTemplate.class);
        mockListOps=mock(ListOperations.class);

        when(mockRedisTemplate.opsForList()).thenReturn(mockListOps);
        trendingService=new TrendingService(mockRepository,mockRedisTemplate);
    }
    @Test
    public void testGetTrendingTermsFromDBWhenCacheMisses(){
        String prefix="ex";
        List<String>expected =List.of("explain","expect","expire");

        when(mockRedisTemplate.hasKey("trending:"+prefix)).thenReturn(false);
        when(mockRepository.findTop10ByTermStartingWithOrderByFrequencyDesc(prefix))
                .thenReturn(
                        List.of(new SearchTerm("explain", 10, null),
                                new SearchTerm("expect", 8, null),
                                new SearchTerm("expire", 5, null))
                );
        List<String>result=trendingService.getTrendingTerms(prefix);
        assertEquals(expected,result);

        //verify repository is called
        verify(mockListOps,times(1))
                .rightPushAll("trending:"+prefix,expected.toArray(new String[0]));
        verify(mockListOps,times(1))
                .rightPushAll(eq("trending:"+prefix),any(String[].class));
    }
    @Test
    public void testGetTrendingTermsFromRedisWhenCacheHits(){
        String prefix="ex";
        List<String>cachedData=List.of("explain","expect","expire");
        when(mockRedisTemplate.hasKey("trending:"+prefix)).thenReturn(true);
        when(mockListOps.range("trending:"+prefix,0,9)).thenReturn(cachedData);

        List<String> result=trendingService.getTrendingTerms(prefix);
        assertEquals(cachedData,result);

        //verify repository is not called
        verify(mockRepository,never()).findTop10ByTermStartingWithOrderByFrequencyDesc(prefix);
        //verify redis is called and queries correctly
        verify(mockRedisTemplate,times(1)).hasKey("trending:"+prefix);
        verify(mockListOps,times(1)).range("trending:"+prefix,0,9);
    }
    @Test
    public void testGetTrendingTermsWithoutPrefix(){
        List<String>expected=List.of("java","spring","redis");
        when(mockRepository.findTop10ByOrderByFrequencyDesc())
                .thenReturn(
                        List.of(
                                new SearchTerm("java", 10, null),
                                new SearchTerm("spring", 8, null),
                                new SearchTerm("redis", 5, null)
                        )
                );
        List<String> result =trendingService.getTrendingTerms();

        assertEquals(expected,result);

        //verify repository is called
        verify(mockRepository,times(1)).findTop10ByOrderByFrequencyDesc();

        //verify redis is not called
        verifyNoInteractions(mockRedisTemplate);
    }
    @Test
    public void testGetTrendingTermsReturnsEmptyWhenNoMatchInDB(){
        String prefix="zzz";
        when(mockRedisTemplate.hasKey("trending:"+prefix)).thenReturn(false);
        when(mockRepository.findTop10ByTermStartingWithOrderByFrequencyDesc(prefix))
                .thenReturn(List.of());
        List<String> result= trendingService.getTrendingTerms(prefix);

        assertEquals(List.of(),result);

        //verify mockListOps
        verify(mockListOps).rightPushAll("trending:"+prefix, List.of().toArray(new String[0]));
        verify(mockRedisTemplate).expire("trending:"+prefix, Duration.ofHours(1));
    }
    @Test
    public void testRedisExpireCalledWhenCachingResults() {
        String prefix = "test";
        List<String> expected = List.of("test1", "test2");

        when(mockRedisTemplate.hasKey("trending:" + prefix)).thenReturn(false);
        when(mockRepository.findTop10ByTermStartingWithOrderByFrequencyDesc(prefix))
                .thenReturn(
                        List.of(new SearchTerm("test1", 5, null),
                                new SearchTerm("test2", 3, null))
                );

        trendingService.getTrendingTerms(prefix);

        // Verify caching
        verify(mockListOps).rightPushAll("trending:" + prefix, expected.toArray(new String[0]));

        // ✅ Verify that Redis expiry is also set
        verify(mockRedisTemplate).expire(eq("trending:" + prefix), eq(Duration.ofHours(1)));
    }

//    @Test
//    public void testGetTrendingTerms_RedisThrowsException_FallbackToDB() {
//        String prefix = "fa";
//        List<String> expected = List.of("fast", "fame");
//
//        // Simulate Redis throwing exception
//        when(mockRedisTemplate.hasKey("trending:" + prefix))
//                .thenThrow(new RuntimeException("Redis unavailable"));
//
//        when(mockRepository.findTop10ByTermStartingWithOrderByFrequencyDesc(prefix))
//                .thenReturn(List.of(
//                        new SearchTerm("fast", 10, null),
//                        new SearchTerm("fame", 8, null)
//                ));
//
//
//        List<String> result = trendingService.getTrendingTerms(prefix);
//
//        assertEquals(expected, result);
//        verify(mockRepository, times(1)).findTop10ByTermStartingWithOrderByFrequencyDesc(prefix);
//    }
//
//    @Test
//    public void testGetTrendingTerms_DBThrowsException() {
//        String prefix = "cr";
//
//        when(mockRedisTemplate.hasKey("trending:" + prefix)).thenReturn(false);
//
//        when(mockRepository.findTop10ByTermStartingWithOrderByFrequencyDesc(prefix))
//                .thenThrow(new RuntimeException("Database failure"));
//
//        try {
//            trendingService.getTrendingTerms(prefix);
//        } catch (RuntimeException e) {
//            assertEquals("Database failure", e.getMessage());
//        }
//    }


}
