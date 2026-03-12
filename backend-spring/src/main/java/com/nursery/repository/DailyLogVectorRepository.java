package com.nursery.repository;

import com.nursery.model.DailyLog;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom repository for MongoDB Atlas Vector Search on daily_logs.
 * Uses the $vectorSearch aggregation stage.
 */
@Repository
public class DailyLogVectorRepository {

    private static final Logger log = LoggerFactory.getLogger(DailyLogVectorRepository.class);
    private final MongoTemplate mongoTemplate;

    public DailyLogVectorRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Find daily logs semantically similar to the given embedding vector.
     *
     * @param queryEmbedding the 384-dimensional query vector
     * @param childId        optional child filter (null to search all)
     * @param limit          max results to return
     * @return list of matching DailyLog documents, ordered by similarity
     */
    public List<DailyLog> vectorSearch(List<Double> queryEmbedding, String childId, int limit) {
        try {
            // Build the $vectorSearch stage as a raw BSON document
            Document vectorSearchStage = new Document("$vectorSearch",
                    new Document("index", "daily_logs_vector_index")
                            .append("path", "embedding")
                            .append("queryVector", queryEmbedding)
                            .append("numCandidates", limit * 10)
                            .append("limit", limit)
            );

            // Add child filter if specified
            if (childId != null && !childId.isBlank()) {
                vectorSearchStage.get("$vectorSearch", Document.class)
                        .append("filter", new Document("childId", childId));
            }

            // Build the aggregation pipeline
            List<Document> pipeline = new ArrayList<>();
            pipeline.add(vectorSearchStage);

            // Add a $project to include the search score
            pipeline.add(new Document("$project",
                    new Document("_id", 1)
                            .append("childId", 1)
                            .append("nurseryId", 1)
                            .append("educatorName", 1)
                            .append("date", 1)
                            .append("mood", 1)
                            .append("appetite", 1)
                            .append("energy", 1)
                            .append("sociability", 1)
                            .append("concentration", 1)
                            .append("autonomy", 1)
                            .append("sleep", 1)
                            .append("motorSkills", 1)
                            .append("tags", 1)
                            .append("remarks", 1)
                            .append("isInternal", 1)
                            .append("aiSummary", 1)
                            .append("date", 1)
                            .append("score", new Document("$meta", "vectorSearchScore"))
            ));

            List<DailyLog> results = mongoTemplate
                    .getCollection("daily_logs")
                    .aggregate(pipeline, DailyLog.class)
                    .into(new ArrayList<>());

            log.info("Vector search returned {} results", results.size());
            return results;

        } catch (Exception e) {
            log.error("Vector search failed (index may not exist yet): {}", e.getMessage());
            return List.of();
        }
    }
}
