package com.arushi.typeahead.trendingrankingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication                  //entry point , only in main class -
                                        // does 3 jobs - > @configuration , @EnableAutoConfiguration , @ComponentScan
public class TrendingRankingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrendingRankingServiceApplication.class, args);
    }

}
