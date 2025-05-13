package com.arushi.typeahead.trendingrankingservice.controller;

import jakarta.annotation.PostConstruct;
import com.arushi.typeahead.trendingrankingservice.service.TrendingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@ControllerAdvice
@RestController
@RequestMapping("/trending")
public class TrendingController {

    private final TrendingService trendingService;
    public TrendingController(TrendingService trendingService) {
        this.trendingService = trendingService;
    }
    // This endpoint fetches the top 10 trending terms.
    @GetMapping
    public List<String> getTrendingTerms(@RequestParam(required = false, defaultValue = "") String prefix) {
        try {
            if (prefix.isEmpty()) {
                log.info("Fetching all trending terms");
                return trendingService.getTrendingTerms();
            }
            log.info("Fetching trending terms with prefix: {}", prefix);
        return trendingService.getTrendingTerms(prefix);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("error fetching trending terms:",e);
        }
    }
    @PostConstruct
    public void init() {
        log.info("🔥 TrendingController is active!");
    }
}
