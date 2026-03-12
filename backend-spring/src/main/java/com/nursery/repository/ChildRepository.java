package com.nursery.repository;

import com.nursery.model.Child;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ChildRepository extends MongoRepository<Child, String> {
    List<Child> findByParentId(String parentId);
}
