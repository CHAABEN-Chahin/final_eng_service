package com.nursery.repository;

import com.nursery.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserIdOrderBySentAtDesc(String userId);
    long countByUserIdAndIsRead(String userId, Boolean isRead);
    List<Notification> findByUserIdAndIsRead(String userId, Boolean isRead);
}
