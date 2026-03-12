package com.nursery.repository;

import com.nursery.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation, String> {
    Optional<Conversation> findByParentIdAndNurseryId(String parentId, String nurseryId);
    List<Conversation> findByParentId(String parentId);
    List<Conversation> findByNurseryId(String nurseryId);
}
