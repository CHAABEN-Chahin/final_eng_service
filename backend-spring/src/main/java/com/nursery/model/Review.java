package com.nursery.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "reviews")
public class Review {

    @Id
    private String id;

    private String nurseryId;
    private String parentId;
    private Integer rating;
    private String comment;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public Review() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNurseryId() { return nurseryId; }
    public void setNurseryId(String nurseryId) { this.nurseryId = nurseryId; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
