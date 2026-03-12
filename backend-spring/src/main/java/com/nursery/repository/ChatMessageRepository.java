package com.nursery.repository;

import com.nursery.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    List<ChatMessage> findByChildIdAndUserIdOrderByCreatedAtDesc(String childId, String userId);

    List<ChatMessage> findByChildIdOrderByCreatedAtDesc(String childId);

    List<ChatMessage> findTop20ByChildIdAndUserIdOrderByCreatedAtDesc(String childId, String userId);
}
