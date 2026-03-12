package com.nursery.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "daily_schedules")
public class DailySchedule {

    @Id
    private String id;

    private String nurseryId;
    private String timeSlot;
    private String activityName;
    private String description;
    private Integer participantCount;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public DailySchedule() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNurseryId() { return nurseryId; }
    public void setNurseryId(String nurseryId) { this.nurseryId = nurseryId; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getParticipantCount() { return participantCount; }
    public void setParticipantCount(Integer participantCount) { this.participantCount = participantCount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
