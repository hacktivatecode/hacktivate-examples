package com.example.hactivateexamples.repository;


import com.datastax.astra.client.Collection;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.*;
import com.example.hactivateexamples.model.VectorEntity;
import com.example.hactivateexamples.model.enums.EntityType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static com.datastax.astra.client.model.Projections.include;

@Slf4j
@Repository
public class VectorEntityRepository {

    private static final String VECTOR_COLLECTION = "vector_v4";
    private final Collection<Document> vectorCollection;


    @Autowired
    public VectorEntityRepository(Database astraDb) {
        this.vectorCollection = astraDb.getCollection(VECTOR_COLLECTION);


    }


    public List<VectorEntity> findAll() {
        log.info("Finding all");
        int count = 0;

        try (FindIterable<Document> results = vectorCollection.find()) {
            List<VectorEntity> entities = new ArrayList<>();
            for (Document doc : results) {
                count++;
                log.info("Added: " + count);
                entities.add(docToTaxonomyEntity(doc));
            }
            return entities;
        } catch (Exception e) {
            log.error("Error finding all ", e);
            return Collections.emptyList();
        }
    }

    public List<VectorEntity> findAllByText(List<String> textList) {
        Filter filter = Filters.in("text", textList.toArray());

        FindOptions options = new FindOptions().limit(20);

        try (FindIterable<Document> results = vectorCollection.find(filter, options)) {
            List<VectorEntity> entities = new ArrayList<>();
            for (Document doc : results) {
                entities.add(docToTaxonomyEntity(doc));
            }
            return entities;
        } catch (Exception e) {
            log.error("Error finding all by text: ", e);
            return Collections.emptyList();
        }
    }

    public List<VectorEntity> findSimilarByType(List<EntityType> entityTypes, Integer maxRecord, float[] embeddings) {
        if (maxRecord == null || maxRecord <= 0 || embeddings == null || embeddings.length == 0) {
            return Collections.emptyList();
        }

        FindOptions options = new FindOptions()
                .projection(include("id", "text", "type"))
                .sort(embeddings)
                .limit(maxRecord)
                .includeSimilarity();

        FindIterable<Document> results;

        if (entityTypes != null && !entityTypes.isEmpty()) {
            Filter typeFilter = Filters.in("type", entityTypes.stream().map(Enum::name).toArray(String[]::new));
            results = vectorCollection.find(typeFilter, options);
        } else {
            results = vectorCollection.find(options);
        }

        try (results) {
            List<VectorEntity> entities = new ArrayList<>();
            for (Document doc : results) {
                VectorEntity entity = docToTaxonomyEntity(doc);
                entity.setSimilarity(doc.getSimilarity().orElse(null));
                entities.add(entity);
            }
            return entities;
        } catch (Exception e) {
            log.error("Error finding similar by type: ", e);
            return Collections.emptyList();
        }
    }

    public Optional<VectorEntity> findOneByType(EntityType entityType, float[] embeddings) {
        Filter filter = Filters.eq("type", entityType.name());

        FindOptions options = new FindOptions()
                .sort(embeddings)
                .limit(1)
                .includeSimilarity();

        try (FindIterable<Document> results = vectorCollection.find(filter, options)) {
            if (results.iterator().hasNext()) {
                Document doc = results.iterator().next();
                VectorEntity entity = docToTaxonomyEntity(doc);
                entity.setSimilarity(doc.getSimilarity().orElse(null));
                return Optional.of(entity);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error finding one by type: ", e);
            return Optional.empty();
        }
    }

    public Optional<VectorEntity> findOneByIdAndType(String id, EntityType type) {
        log.info("Finding one by id and type: id={}, type={}", id, type.name());

        Filter filter = Filters.and(
                Filters.eq("id", id),
                Filters.eq("type", type.name())
        );

        try {
            Optional<Document> result = vectorCollection.findOne(filter);
            if (result.isPresent()) {
                VectorEntity entity = docToTaxonomyEntity(result.get());
                return Optional.of(entity);
            }
            return Optional.empty();
        } catch (Exception e) {

            log.error("Error finding one by id and type: ", e);
            return Optional.empty();
        }
    }

    public List<VectorEntity> findAllByTypeAndId(Map<String, EntityType> entityIdToType) {
        if (entityIdToType == null || entityIdToType.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> entityIds = new ArrayList<>(entityIdToType.keySet());
        List<String> entityTypes = entityIdToType.values().stream().map(Enum::name).collect(Collectors.toList());

        Filter filter = Filters.and(
                Filters.in("id", entityIds.toArray()),
                Filters.in("type", entityTypes.toArray())
        );

        FindOptions options = new FindOptions();

        try (FindIterable<Document> results = vectorCollection.find(filter, options)) {
            List<VectorEntity> entities = new ArrayList<>();
            for (Document doc : results) {
                entities.add(docToTaxonomyEntity(doc));
            }
            return entities;
        } catch (Exception e) {
            log.error("Error finding all by type and id: ", e);
            return Collections.emptyList();
        }
    }

    private VectorEntity docToTaxonomyEntity(Document doc) {
        VectorEntity entity = new VectorEntity();
        entity.setId(doc.getString("id"));
        entity.setText(doc.getString("text"));
        entity.setType(doc.getString("type"));
        entity.setSimilarity(doc.getSimilarity().orElse(null));
        entity.setEmbeddings(doc.get("$vector", float[].class));
        // Add other field mappings as necessary
        return entity;
    }
}
