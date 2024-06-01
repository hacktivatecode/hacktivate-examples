package com.example.hactivateexamples.service;


import com.example.hactivateexamples.config.constants.ErrorCodes;
import com.example.hactivateexamples.dto.RecommendationRequest;
import com.example.hactivateexamples.exception.NotFoundException;
import com.example.hactivateexamples.model.VectorEntity;
import com.example.hactivateexamples.repository.VectorEntityRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class SearchService {


    @Autowired
    VectorEntityRepository vectorEntityRepository;

    @Autowired
    EmbeddingService embeddingService;


    public List<VectorEntity> getSimilarEntities(RecommendationRequest recommendationRequest) throws NotFoundException {


        // Initialize embeddings variable
        float[] embeddings;

        // First try to get the embeddings from an existing entity by ID
        Optional<float[]> embeddingsOpt = Optional.ofNullable(recommendationRequest.getEntityId())
                .flatMap(id -> vectorEntityRepository.findOneByIdAndType(id, recommendationRequest.getEntityType()))
                .map(VectorEntity::getEmbeddings);

        if (embeddingsOpt.isPresent()) {
            // If embeddings are found from the repository
            embeddings = embeddingsOpt.get();
        } else if (StringUtils.isNotBlank(recommendationRequest.getEntityText())) {
            // Only call getEmbeddings if the entity text is not blank
            Map<String, float[]> embeddingsMap = embeddingService.getEmbeddings(Collections.singletonList(recommendationRequest.getEntityText()));
            // Since we know it's a single entry, retrieve the embedding using the original text as the key
            embeddings = embeddingsMap.get(recommendationRequest.getEntityText());
        } else {
            // Handle case where neither ID nor text provides a valid embedding
            throw new NotFoundException("No valid entity ID or non-blank text provided to retrieve embeddings.", ErrorCodes.ENTITY_NOT_FOUND);
        }

        // Find and return similar entities based on the type(s) to recommend, limit, and the obtained embeddings
        return vectorEntityRepository.findSimilarByType(recommendationRequest.getEntityTypesToRecommend(), recommendationRequest.getLimit(), embeddings);
    }


}
