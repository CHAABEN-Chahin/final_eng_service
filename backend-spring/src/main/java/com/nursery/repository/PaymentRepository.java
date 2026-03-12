package com.nursery.repository;

import com.nursery.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findByParentId(String parentId);
    List<Payment> findByNurseryId(String nurseryId);
    List<Payment> findByParentIdAndPaymentStatus(String parentId, String paymentStatus);
    Optional<Payment> findByEnrollmentId(String enrollmentId);
    List<Payment> findByNurseryIdAndPaymentStatus(String nurseryId, String paymentStatus);
    List<Payment> findByParentIdOrderByCreatedAtDesc(String parentId);
}
