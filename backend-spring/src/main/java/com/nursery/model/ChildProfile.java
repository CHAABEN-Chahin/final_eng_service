package com.nursery.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Knowledge graph for a child — stores derived patterns, milestones,
 * correlations and weekly digests built from daily logs.
 * Updated incrementally each time a new log is saved.
 */
@Document(collection = "child_profiles")
public class ChildProfile {

    @Id
    private String id;

    private String childId;

    /** Running averages per dimension for different windows (7d, 14d, 30d) */
    private Map<String, Map<String, Double>> averages = new HashMap<>();
    // e.g. { "mood": { "7d": 3.8, "14d": 3.5, "30d": 3.6 }, ... }

    /** Detected trend patterns */
    private List<Pattern> patterns = new ArrayList<>();

    /** Notable milestones / events */
    private List<Milestone> milestones = new ArrayList<>();

    /** Cross-dimension correlations */
    private List<Correlation> correlations = new ArrayList<>();

    /** Weekly digest summaries */
    private List<WeeklyDigest> weeklyDigests = new ArrayList<>();

    private int totalLogs = 0;
    private Instant lastUpdated = Instant.now();

    public ChildProfile() {}

    // --- Inner classes ---

    public static class Pattern {
        private String dimension;       // "mood", "sleep", etc.
        private String trend;           // "improving", "declining", "stable"
        private String period;          // "2026-02 to 2026-03"
        private String summary;         // human-readable French summary
        private Instant detectedAt = Instant.now();

        public Pattern() {}

        public String getDimension() { return dimension; }
        public void setDimension(String dimension) { this.dimension = dimension; }
        public String getTrend() { return trend; }
        public void setTrend(String trend) { this.trend = trend; }
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public Instant getDetectedAt() { return detectedAt; }
        public void setDetectedAt(Instant detectedAt) { this.detectedAt = detectedAt; }
    }

    public static class Milestone {
        private String date;
        private String type;            // "behavioral", "improvement", "concern"
        private String description;
        private String sourceLogId;

        public Milestone() {}

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSourceLogId() { return sourceLogId; }
        public void setSourceLogId(String sourceLogId) { this.sourceLogId = sourceLogId; }
    }

    public static class Correlation {
        private String dimension1;
        private String dimension2;
        private String type;            // "positive" or "negative"
        private double strength;        // 0.0 to 1.0
        private String note;

        public Correlation() {}

        public String getDimension1() { return dimension1; }
        public void setDimension1(String dimension1) { this.dimension1 = dimension1; }
        public String getDimension2() { return dimension2; }
        public void setDimension2(String dimension2) { this.dimension2 = dimension2; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public double getStrength() { return strength; }
        public void setStrength(double strength) { this.strength = strength; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }

    public static class WeeklyDigest {
        private String week;            // e.g. "2026-W10"
        private String summary;
        private Map<String, Double> avgScores = new HashMap<>();

        public WeeklyDigest() {}

        public String getWeek() { return week; }
        public void setWeek(String week) { this.week = week; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public Map<String, Double> getAvgScores() { return avgScores; }
        public void setAvgScores(Map<String, Double> avgScores) { this.avgScores = avgScores; }
    }

    // --- Main getters/setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getChildId() { return childId; }
    public void setChildId(String childId) { this.childId = childId; }

    public Map<String, Map<String, Double>> getAverages() { return averages; }
    public void setAverages(Map<String, Map<String, Double>> averages) { this.averages = averages; }

    public List<Pattern> getPatterns() { return patterns; }
    public void setPatterns(List<Pattern> patterns) { this.patterns = patterns; }

    public List<Milestone> getMilestones() { return milestones; }
    public void setMilestones(List<Milestone> milestones) { this.milestones = milestones; }

    public List<Correlation> getCorrelations() { return correlations; }
    public void setCorrelations(List<Correlation> correlations) { this.correlations = correlations; }

    public List<WeeklyDigest> getWeeklyDigests() { return weeklyDigests; }
    public void setWeeklyDigests(List<WeeklyDigest> weeklyDigests) { this.weeklyDigests = weeklyDigests; }

    public int getTotalLogs() { return totalLogs; }
    public void setTotalLogs(int totalLogs) { this.totalLogs = totalLogs; }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
}
