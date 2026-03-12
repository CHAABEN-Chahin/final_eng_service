package com.nursery.repository;

import com.nursery.model.Nursery;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface NurseryRepository extends MongoRepository<Nursery, String> {
    List<Nursery> findByOwnerId(String ownerId);
    List<Nursery> findByCityIgnoreCase(String city);
    List<Nursery> findByPricePerMonthLessThanEqual(Double maxPrice);
    List<Nursery> findByRatingGreaterThanEqual(Double minRating);
    List<Nursery> findByAvailableSpotsGreaterThanEqual(Integer minSpots);
}
