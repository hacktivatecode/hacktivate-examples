package com.example.hactivateexamples.dto;


import com.example.hactivateexamples.model.enums.EntityType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {


    private String entityText;

    private String entityId;

    private EntityType entityType;

    private List<EntityType> entityTypesToRecommend;


    @Min(1)
    @Max(100)
    private Integer limit = 10;


}
