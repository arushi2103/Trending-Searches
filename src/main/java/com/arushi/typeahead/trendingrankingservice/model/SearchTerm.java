package com.arushi.typeahead.trendingrankingservice.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
@Data
@AllArgsConstructor
@Document(collection = "SearchTerm") // Marks this class as a MongoDB collection.
public class SearchTerm {
    @Id
    private String term;
    private int frequency;
    private Instant lastSearched;

    // Getters, Setters, Constructors
}
