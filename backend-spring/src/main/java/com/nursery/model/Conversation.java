package com.nursery.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "conversations")
public class Conversation {

    @Id
    private String id;

    private String parentId;
    private String nurseryId;
    private Instant lastMessageAt = Instant.now();

    public Conversation() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getNurseryId() { return nurseryId; }
    public void setNurseryId(String nurseryId) { this.nurseryId = nurseryId; }

    public Instant getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(Instant lastMessageAt) { this.lastMessageAt = lastMessageAt; }
}
