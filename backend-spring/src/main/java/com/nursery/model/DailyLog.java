package com.nursery.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "daily_logs")
public class DailyLog {

    @Id
    private String id;

    private String childId;
    private String nurseryId;
    private String educatorId;
    private String educatorName;
    private String date; // YYYY-MM-DD

    // Pulse scores (1-5)
    private Integer mood;
    private Integer appetite;
    private Integer energy;
    private Integer sociability;
    private Integer concentration;
    private Integer autonomy;
    private Integer sleep;
    private Integer motorSkills;

    // Tag cloud selections
    private List<String> tags;

    // Remarks
    private String remarks;

    // Internal (staff only) flag
    private Boolean isInternal = false;

    // AI-generated summary for parents
    private String aiSummary;

    // Vector embedding (384-dim, all-MiniLM-L6-v2)
    private List<Double> embedding;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public DailyLog() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getChildId() { return childId; }
    public void setChildId(String childId) { this.childId = childId; }

    public String getNurseryId() { return nurseryId; }
    public void setNurseryId(String nurseryId) { this.nurseryId = nurseryId; }

    public String getEducatorId() { return educatorId; }
    public void setEducatorId(String educatorId) { this.educatorId = educatorId; }

    public String getEducatorName() { return educatorName; }
    public void setEducatorName(String educatorName) { this.educatorName = educatorName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Integer getMood() { return mood; }
    public void setMood(Integer mood) { this.mood = mood; }

    public Integer getAppetite() { return appetite; }
    public void setAppetite(Integer appetite) { this.appetite = appetite; }

    public Integer getEnergy() { return energy; }
    public void setEnergy(Integer energy) { this.energy = energy; }

    public Integer getSociability() { return sociability; }
    public void setSociability(Integer sociability) { this.sociability = sociability; }

    public Integer getConcentration() { return concentration; }
    public void setConcentration(Integer concentration) { this.concentration = concentration; }

    public Integer getAutonomy() { return autonomy; }
    public void setAutonomy(Integer autonomy) { this.autonomy = autonomy; }

    public Integer getSleep() { return sleep; }
    public void setSleep(Integer sleep) { this.sleep = sleep; }

    public Integer getMotorSkills() { return motorSkills; }
    public void setMotorSkills(Integer motorSkills) { this.motorSkills = motorSkills; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Boolean getIsInternal() { return isInternal; }
    public void setIsInternal(Boolean isInternal) { this.isInternal = isInternal; }

    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }

    public List<Double> getEmbedding() { return embedding; }
    public void setEmbedding(List<Double> embedding) { this.embedding = embedding; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
