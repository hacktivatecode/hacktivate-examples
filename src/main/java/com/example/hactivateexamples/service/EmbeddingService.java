package com.example.hactivateexamples.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class EmbeddingService {


    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    RestTemplate restTemplate;
    @Value("${embeddings.endpoint}")
    private String embeddingEndpoint;

    public Map<String, float[]> getEmbeddings(List<String> contents) {
        Map<String, float[]> result = new HashMap<>();
        // Fetch embeddings for any contents not found in cache or failed deserialization
        if (!contents.isEmpty()) {
            result.putAll(fetchEmbeddingsFromAPI(contents));
        }

        return result;
    }

    private Map<String, float[]> fetchEmbeddingsFromAPI(List<String> missingContents) {
        log.debug("Fetching embeddings from API for {} contents", missingContents.size());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, Object> map = new HashMap<>();
        List<Map<String, String>> instances = new ArrayList<>();
        for (String content : missingContents) {
            Map<String, String> instance = new HashMap<>();
            instance.put("content", content);
            instances.add(instance);
        }
        map.put("instances", instances);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<String> response = restTemplate.exchange(embeddingEndpoint, HttpMethod.POST, entity, String.class);
        Map<String, float[]> fetchedEmbeddings = new HashMap<>();

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
                List<Map<String, List<Double>>> predictions = (List<Map<String, List<Double>>>) responseBody.get("predictions");
                for (int i = 0; i < missingContents.size(); i++) {
                    List<Double> embeddingList = predictions.get(i).get("embedding");
                    float[] embeddingArray = new float[embeddingList.size()];
                    for (int j = 0; j < embeddingList.size(); j++) {
                        embeddingArray[j] = embeddingList.get(j).floatValue();
                    }
                    fetchedEmbeddings.put(missingContents.get(i), embeddingArray);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse embeddings from API response", e);
            }
        } else {
            throw new RuntimeException("Failed to fetch embeddings from API");
        }

        return fetchedEmbeddings;
    }


}
