package com.arushi.typeahead.trendingrankingservice.controller;

import com.arushi.typeahead.trendingrankingservice.service.TrendingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrendingController.class)
public class TrendingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private TrendingService trendingService;

    @Test
    void shouldReturnTermsForPrefix()throws Exception{
        when (trendingService.getTrendingTerms("exp"))
                .thenReturn(List.of("explain", "expect"));
        mockMvc.perform(get("/trending").param("prefix","exp"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"explain\", \"expect\"]"));
    }
}
