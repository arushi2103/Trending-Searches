package com.arushi.typeahead.trendingrankingservice.controller;

import com.arushi.typeahead.trendingrankingservice.TrendingRankingServiceApplication;
import com.arushi.typeahead.trendingrankingservice.model.SearchTerm;
import com.arushi.typeahead.trendingrankingservice.repository.SearchTermRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TrendingRankingServiceApplication.class)          //for loading the application context
@AutoConfigureMockMvc                   //for testing the controller
public class TrendingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SearchTermRepository searchTermRepository;

    @BeforeEach
    public void setUpData(){
        //searchTermRepository.deleteAll();
        searchTermRepository.saveAll(
                Arrays.asList(
                        new SearchTerm("explain", 10, null),
                        new SearchTerm("expect", 8, null),
                        new SearchTerm("expire", 5, null),
                        new SearchTerm("expert", 4,null)
                )
        );

    }
    @Test
    public void testGetTrendingTermsWithoutPrefix()throws Exception{
        mockMvc.perform(get("/trending")
                        .param("prefix", "exp")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    List<String> expectedTerms = Arrays.asList("explain", "expect", "expire","expert");
                    List<String> actualTerms = new ObjectMapper().readValue(responseBody, List.class);
                    assertEquals(expectedTerms, actualTerms);
                });
    }

    @Test
    public void testGetTrendingTermsWithPrefix()throws Exception{
        mockMvc.perform(get("/trending")
                .param("prefix", "exp")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    List<String> expectedTerms = Arrays.asList("explain", "expect", "expire","expert");
                    List<String> actualTerms = new ObjectMapper().readValue(responseBody, List.class);
                    assertEquals(expectedTerms, actualTerms);
                });
    }

    @Test
    public void testGetTrendingTermsWithNoMatch() throws  Exception{
        mockMvc.perform(get("/trending")
                        .param("prefix", "xyz")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String responseBody = result.getResponse().getContentAsString();
                    List<String> actualTerms = new ObjectMapper().readValue(responseBody, List.class);
                    if (actualTerms == null) {
                        actualTerms = Arrays.asList(); // Handle null response as an empty list
                    }
                    assertEquals(Collections.emptyList(), actualTerms);
                });
    }
}
