package com.nursery.repository;

import com.nursery.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByNurseryId(String nurseryId);
    List<Review> findByParentId(String parentId);
    List<Review> findByNurseryIdOrderByCreatedAtDesc(String nurseryId);
}
