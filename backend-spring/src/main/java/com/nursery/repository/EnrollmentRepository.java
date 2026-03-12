package com.nursery.repository;

import com.nursery.model.Enrollment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends MongoRepository<Enrollment, String> {
    List<Enrollment> findByNurseryId(String nurseryId);
    List<Enrollment> findByParentId(String parentId);
    List<Enrollment> findByChildId(String childId);
    List<Enrollment> findByStatus(String status);
    List<Enrollment> findByNurseryIdAndStatus(String nurseryId, String status);
    Optional<Enrollment> findByChildIdAndNurseryId(String childId, String nurseryId);
    long countByNurseryId(String nurseryId);
    long countByNurseryIdAndStatus(String nurseryId, String status);
}
