package com.nursery.repository;

import com.nursery.model.ChildProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ChildProfileRepository extends MongoRepository<ChildProfile, String> {

    Optional<ChildProfile> findByChildId(String childId);
}
