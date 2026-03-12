package com.nursery.service;

import com.nursery.model.Child;
import com.nursery.model.DailyLog;
import com.nursery.model.User;
import com.nursery.repository.ChildRepository;
import com.nursery.repository.DailyLogRepository;
import com.nursery.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DailyLogService {

    private static final Logger log = LoggerFactory.getLogger(DailyLogService.class);

    private final DailyLogRepository dailyLogRepository;
    private final ChildRepository childRepository;
    private final UserRepository userRepository;
    private final EmbeddingService embeddingService;
    private ChildChatService childChatService;

    @org.springframework.beans.factory.annotation.Value("${groq.api.key:}")
    private String groqApiKey;

    @org.springframework.beans.factory.annotation.Value("${groq.api.model:llama-3.1-8b-instant}")
    private String groqModel;

    public DailyLogService(DailyLogRepository dailyLogRepository,
                           ChildRepository childRepository,
                           UserRepository userRepository,
                           EmbeddingService embeddingService) {
        this.dailyLogRepository = dailyLogRepository;
        this.childRepository = childRepository;
        this.userRepository = userRepository;
        this.embeddingService = embeddingService;
    }

    @org.springframework.beans.factory.annotation.Autowired
    public void setChildChatService(ChildChatService childChatService) {
        this.childChatService = childChatService;
    }

    /**
     * Create a daily observation log (educator action)
     */
    public Map<String, Object> createLog(Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            String childId = (String) body.get("childId");
            String nurseryId = (String) body.get("nurseryId");
            String educatorId = (String) body.get("educatorId");

            if (childId == null || nurseryId == null || educatorId == null) {
                response.put("success", false);
                response.put("message", "childId, nurseryId, and educatorId are required");
                return response;
            }

            Optional<Child> childOpt = childRepository.findById(childId);
            if (childOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Child not found");
                return response;
            }

            Optional<User> educatorOpt = userRepository.findById(educatorId);
            String educatorName = educatorOpt.map(User::getName).orElse("Éducateur");

            DailyLog dailyLog = new DailyLog();
            dailyLog.setChildId(childId);
            dailyLog.setNurseryId(nurseryId);
            dailyLog.setEducatorId(educatorId);
            dailyLog.setEducatorName(educatorName);
            dailyLog.setDate((String) body.getOrDefault("date", LocalDate.now().toString()));
            dailyLog.setMood(parseInteger(body.get("mood")));
            dailyLog.setAppetite(parseInteger(body.get("appetite")));
            dailyLog.setEnergy(parseInteger(body.get("energy")));
            dailyLog.setSociability(parseInteger(body.get("sociability")));
            dailyLog.setConcentration(parseInteger(body.get("concentration")));
            dailyLog.setAutonomy(parseInteger(body.get("autonomy")));
            dailyLog.setSleep(parseInteger(body.get("sleep")));
            dailyLog.setMotorSkills(parseInteger(body.get("motorSkills")));
            dailyLog.setRemarks((String) body.get("remarks"));
            dailyLog.setIsInternal(Boolean.TRUE.equals(body.get("isInternal")));

            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) body.get("tags");
            dailyLog.setTags(tags != null ? tags : new ArrayList<>());

            DailyLog saved = dailyLogRepository.save(dailyLog);

            // Trigger AI summary generation asynchronously
            generateDailySummary(saved);

            // Update child knowledge graph
            if (childChatService != null) {
                childChatService.updateChildProfile(saved.getChildId());
            }

            response.put("success", true);
            response.put("log", buildLogDto(saved));
        } catch (Exception e) {
            log.error("Error creating daily log", e);
            response.put("success", false);
            response.put("message", "Error creating daily log");
        }
        return response;
    }

    /**
     * Get all logs for a child (educator view — includes internal)
     */
    public Map<String, Object> getLogsByChild(String childId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<DailyLog> logs = dailyLogRepository.findByChildIdOrderByDateDesc(childId);
            response.put("success", true);
            response.put("logs", logs.stream().map(this::buildLogDto).collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("Error fetching logs for child {}", childId, e);
            response.put("success", false);
            response.put("message", "Error fetching logs");
        }
        return response;
    }

    /**
     * Get logs for a child visible to parents (excludes internal notes)
     */
    public Map<String, Object> getLogsByChildForParent(String childId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<DailyLog> logs = dailyLogRepository.findByChildIdAndIsInternalFalseOrderByDateDesc(childId);
            response.put("success", true);
            response.put("logs", logs.stream().map(this::buildParentLogDto).collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("Error fetching parent logs for child {}", childId, e);
            response.put("success", false);
            response.put("message", "Error fetching logs");
        }
        return response;
    }

    /**
     * Get today's log for a specific child
     */
    public Map<String, Object> getTodayLog(String childId) {
        Map<String, Object> response = new HashMap<>();
        try {
            String today = LocalDate.now().toString();
            List<DailyLog> logs = dailyLogRepository.findByChildIdAndDate(childId, today);
            if (!logs.isEmpty()) {
                response.put("success", true);
                response.put("log", buildParentLogDto(logs.get(0)));
            } else {
                response.put("success", true);
                response.put("log", null);
            }
        } catch (Exception e) {
            log.error("Error fetching today's log for child {}", childId, e);
            response.put("success", false);
            response.put("message", "Error fetching today's log");
        }
        return response;
    }

    /**
     * LLM Bridge: Generate a warm parent-friendly summary from log data
     */
    public void generateDailySummary(DailyLog logData) {
        try {
            Optional<Child> childOpt = childRepository.findById(logData.getChildId());
            String childName = childOpt.map(Child::getName).orElse("votre enfant");

            String prompt = buildPrompt(logData, childName);
            String aiSummary = callLlmApi(prompt);

            boolean changed = false;

            if (aiSummary != null && !aiSummary.isEmpty()) {
                logData.setAiSummary(aiSummary);
                changed = true;
                log.info("AI summary generated for log {}", logData.getId());
            }

            // Generate vector embedding from log content
            String embeddingText = buildEmbeddingText(logData);
            List<Double> embedding = embeddingService.embed(embeddingText);
            if (embedding != null) {
                logData.setEmbedding(embedding);
                changed = true;
                log.info("Embedding generated for log {} ({} dims)", logData.getId(), embedding.size());
            }

            if (changed) {
                logData.setUpdatedAt(Instant.now());
                dailyLogRepository.save(logData);
            }
        } catch (Exception e) {
            log.error("Error generating AI summary for log {}", logData.getId(), e);
        }
    }

    /**
     * Build the text to embed for vector search.
     * Combines scores as natural language + tags + remarks + AI summary.
     */
    private String buildEmbeddingText(DailyLog logData) {
        StringBuilder sb = new StringBuilder();
        if (logData.getMood() != null) sb.append("Humeur: ").append(logData.getMood()).append("/5. ");
        if (logData.getAppetite() != null) sb.append("Appétit: ").append(logData.getAppetite()).append("/5. ");
        if (logData.getEnergy() != null) sb.append("Énergie: ").append(logData.getEnergy()).append("/5. ");
        if (logData.getSociability() != null) sb.append("Sociabilité: ").append(logData.getSociability()).append("/5. ");
        if (logData.getConcentration() != null) sb.append("Concentration: ").append(logData.getConcentration()).append("/5. ");
        if (logData.getAutonomy() != null) sb.append("Autonomie: ").append(logData.getAutonomy()).append("/5. ");
        if (logData.getSleep() != null) sb.append("Sommeil: ").append(logData.getSleep()).append("/5. ");
        if (logData.getMotorSkills() != null) sb.append("Motricité: ").append(logData.getMotorSkills()).append("/5. ");
        if (logData.getTags() != null && !logData.getTags().isEmpty()) {
            sb.append("Activités: ").append(String.join(", ", logData.getTags())).append(". ");
        }
        if (logData.getRemarks() != null && !logData.getRemarks().isEmpty()) {
            sb.append("Remarques: ").append(logData.getRemarks()).append(". ");
        }
        if (logData.getAiSummary() != null && !logData.getAiSummary().isEmpty()) {
            sb.append(logData.getAiSummary());
        }
        return sb.toString();
    }

    /**
     * Backfill embeddings for all logs that don't have one yet.
     */
    public Map<String, Object> backfillEmbeddings() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<DailyLog> allLogs = dailyLogRepository.findAll();
            int count = 0;
            for (DailyLog logData : allLogs) {
                if (logData.getEmbedding() == null || logData.getEmbedding().isEmpty()) {
                    String text = buildEmbeddingText(logData);
                    List<Double> embedding = embeddingService.embed(text);
                    if (embedding != null) {
                        logData.setEmbedding(embedding);
                        logData.setUpdatedAt(Instant.now());
                        dailyLogRepository.save(logData);
                        count++;
                    }
                }
            }
            response.put("success", true);
            response.put("message", "Backfilled " + count + " logs with embeddings");
            response.put("total", allLogs.size());
            response.put("updated", count);
            log.info("Backfill complete: {} / {} logs updated", count, allLogs.size());
        } catch (Exception e) {
            log.error("Error during embedding backfill", e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    private String buildPrompt(DailyLog logData, String childName) {
        StringBuilder sb = new StringBuilder();
        sb.append("Translate these nursery observations into a warm, 3-sentence narrative for a parent. ");
        sb.append("The child's name is ").append(childName).append(".\n\n");
        sb.append("Raw Data:\n");
        sb.append("- Mood score: ").append(logData.getMood()).append("/5\n");
        sb.append("- Appetite score: ").append(logData.getAppetite()).append("/5\n");
        sb.append("- Energy score: ").append(logData.getEnergy()).append("/5\n");
        sb.append("- Sociability score: ").append(logData.getSociability()).append("/5\n");
        sb.append("- Concentration score: ").append(logData.getConcentration()).append("/5\n");
        sb.append("- Autonomy score: ").append(logData.getAutonomy()).append("/5\n");
        sb.append("- Sleep/nap quality score: ").append(logData.getSleep()).append("/5\n");
        sb.append("- Motor skills score: ").append(logData.getMotorSkills()).append("/5\n");

        if (logData.getTags() != null && !logData.getTags().isEmpty()) {
            sb.append("- Activity tags: ").append(String.join(", ", logData.getTags())).append("\n");
        }
        if (logData.getRemarks() != null && !logData.getRemarks().isEmpty()) {
            sb.append("- Educator remarks: ").append(logData.getRemarks()).append("\n");
        }

        sb.append("\nTone: Reassuring, joyful, and professional.\n");
        sb.append("Language: French.\n");
        sb.append("Constraint: Do not mention specific scores (like 4/5). ");
        sb.append("Instead say things like 'Il a eu un très bon appétit aujourd'hui' ");
        sb.append("or 'Il était très concentré pendant les activités'.");

        return sb.toString();
    }

    /**
     * Call Groq LLM API (OpenAI-compatible endpoint).
     */
    @SuppressWarnings("unchecked")
    private String callLlmApi(String prompt) {
        if (groqApiKey == null || groqApiKey.isBlank()) {
            log.warn("Groq API key not configured — skipping AI summary");
            return null;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            Map<String, Object> message = new LinkedHashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", groqModel);
            requestBody.put("messages", List.of(message));
            requestBody.put("max_tokens", 300);
            requestBody.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.groq.com/openai/v1/chat/completions",
                    HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> msgObj = (Map<String, Object>) choices.get(0).get("message");
                    if (msgObj != null) {
                        return (String) msgObj.get("content");
                    }
                }
            }

            log.warn("Groq API returned no content");
            return null;
        } catch (Exception e) {
            log.error("Error calling Groq API", e);
            return null;
        }
    }

    // --- DTO builders ---

    private Map<String, Object> buildLogDto(DailyLog log) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", log.getId());
        dto.put("childId", log.getChildId());
        dto.put("nurseryId", log.getNurseryId());
        dto.put("educatorId", log.getEducatorId());
        dto.put("educatorName", log.getEducatorName());
        dto.put("date", log.getDate());
        dto.put("mood", log.getMood());
        dto.put("appetite", log.getAppetite());
        dto.put("energy", log.getEnergy());
        dto.put("sociability", log.getSociability());
        dto.put("concentration", log.getConcentration());
        dto.put("autonomy", log.getAutonomy());
        dto.put("sleep", log.getSleep());
        dto.put("motorSkills", log.getMotorSkills());
        dto.put("tags", log.getTags());
        dto.put("remarks", log.getRemarks());
        dto.put("isInternal", log.getIsInternal());
        dto.put("aiSummary", log.getAiSummary());
        dto.put("createdAt", log.getCreatedAt() != null ? log.getCreatedAt().toString() : null);
        return dto;
    }

    private Map<String, Object> buildParentLogDto(DailyLog log) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", log.getId());
        dto.put("childId", log.getChildId());
        dto.put("date", log.getDate());
        dto.put("mood", log.getMood());
        dto.put("appetite", log.getAppetite());
        dto.put("energy", log.getEnergy());
        dto.put("sociability", log.getSociability());
        dto.put("concentration", log.getConcentration());
        dto.put("autonomy", log.getAutonomy());
        dto.put("sleep", log.getSleep());
        dto.put("motorSkills", log.getMotorSkills());
        dto.put("tags", log.getTags());
        dto.put("remarks", log.getRemarks());
        dto.put("aiSummary", log.getAiSummary());
        dto.put("educatorName", log.getEducatorName());
        dto.put("createdAt", log.getCreatedAt() != null ? log.getCreatedAt().toString() : null);
        return dto;
    }

    private Integer parseInteger(Object value) {
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try { return Integer.parseInt((String) value); } catch (NumberFormatException e) { return 3; }
        }
        return 3; // default middle value
    }
}
