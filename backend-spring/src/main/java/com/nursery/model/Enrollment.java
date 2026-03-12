package com.nursery.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "enrollments")
public class Enrollment {

    @Id
    private String id;

    private String childId;
    private String nurseryId;
    private String parentId;
    private String startDate;
    private String status = "pending"; // pending, active, completed, cancelled
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public Enrollment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getChildId() { return childId; }
    public void setChildId(String childId) { this.childId = childId; }

    public String getNurseryId() { return nurseryId; }
    public void setNurseryId(String nurseryId) { this.nurseryId = nurseryId; }

    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
