package com.nursery.repository;

import com.nursery.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByConversationIdOrderBySentAtAsc(String conversationId);
    List<Message> findByConversationIdAndRecipientIdAndIsRead(String conversationId, String recipientId, Boolean isRead);
    long countByConversationIdAndRecipientIdAndIsRead(String conversationId, String recipientId, Boolean isRead);
}
