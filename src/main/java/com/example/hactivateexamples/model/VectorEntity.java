package com.example.hactivateexamples.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VectorEntity {

    @JsonProperty("id")
    private String id;


    @JsonProperty("text")
    private String text;

    @JsonProperty("type")
    private String type;

    @JsonProperty("similarity")
    private Double similarity;

    @JsonProperty("embeddings")
    private float[] embeddings;


}
