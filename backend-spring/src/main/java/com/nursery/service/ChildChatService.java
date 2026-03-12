package com.nursery.service;

import com.nursery.model.*;
import com.nursery.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChildChatService {

    private static final Logger log = LoggerFactory.getLogger(ChildChatService.class);
    private static final String[] DIMENSIONS = {
            "mood", "appetite", "energy", "sociability",
            "concentration", "autonomy", "sleep", "motorSkills"
    };
    private static final Map<String, String> DIM_LABELS = Map.of(
            "mood", "Humeur", "appetite", "Appétit", "energy", "Énergie",
            "sociability", "Sociabilité", "concentration", "Concentration",
            "autonomy", "Autonomie", "sleep", "Sommeil", "motorSkills", "Motricité"
    );

    private final ChatMessageRepository chatMessageRepository;
    private final DailyLogRepository dailyLogRepository;
    private final ChildProfileRepository childProfileRepository;
    private final ChildRepository childRepository;
    private final EmbeddingService embeddingService;
    private final DailyLogVectorRepository dailyLogVectorRepository;

    @org.springframework.beans.factory.annotation.Value("${groq.api.key:}")
    private String groqApiKey;

    @org.springframework.beans.factory.annotation.Value("${groq.api.model:llama-3.1-8b-instant}")
    private String groqModel;

    public ChildChatService(ChatMessageRepository chatMessageRepository,
                            DailyLogRepository dailyLogRepository,
                            ChildProfileRepository childProfileRepository,
                            ChildRepository childRepository,
                            EmbeddingService embeddingService,
                            DailyLogVectorRepository dailyLogVectorRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.dailyLogRepository = dailyLogRepository;
        this.childProfileRepository = childProfileRepository;
        this.childRepository = childRepository;
        this.embeddingService = embeddingService;
        this.dailyLogVectorRepository = dailyLogVectorRepository;
    }

    // ====================================================================
    //  MAIN CHAT ENTRY POINT
    // ====================================================================

    public Map<String, Object> chat(String childId, String userId, String userMessage) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (groqApiKey == null || groqApiKey.isBlank()) {
                response.put("success", false);
                response.put("message", "Groq API key not configured");
                return response;
            }

            // 1. Get child name
            String childName = childRepository.findById(childId)
                    .map(Child::getName).orElse("l'enfant");

            // 2. Classify the query
            QueryClassification classification = classifyQuery(userMessage);
            log.info("Query classified: type={}, dimensions={}, window={}",
                    classification.type, classification.dimensions, classification.timeWindow);

            // 3. Retrieve context based on classification
            String retrievedContext = retrieveContext(childId, childName, classification, userMessage);

            // 4. Get conversation history (last 10 messages for continuity)
            List<ChatMessage> history = chatMessageRepository
                    .findTop20ByChildIdAndUserIdOrderByCreatedAtDesc(childId, userId);
            Collections.reverse(history); // chronological order

            // 5. Build the prompt and call Groq
            String assistantReply = callGroqChat(childName, userMessage, retrievedContext, history);

            if (assistantReply == null || assistantReply.isBlank()) {
                response.put("success", false);
                response.put("message", "Le chatbot n'a pas pu générer de réponse");
                return response;
            }

            // 6. Save both messages to history
            ChatMessage userMsg = new ChatMessage();
            userMsg.setChildId(childId);
            userMsg.setUserId(userId);
            userMsg.setRole("user");
            userMsg.setContent(userMessage);
            userMsg.setQueryType(classification.type);
            chatMessageRepository.save(userMsg);

            ChatMessage assistantMsg = new ChatMessage();
            assistantMsg.setChildId(childId);
            assistantMsg.setUserId(userId);
            assistantMsg.setRole("assistant");
            assistantMsg.setContent(assistantReply);
            chatMessageRepository.save(assistantMsg);

            response.put("success", true);
            response.put("reply", assistantReply);
            response.put("queryType", classification.type);

        } catch (Exception e) {
            log.error("Error in child chat", e);
            response.put("success", false);
            response.put("message", "Erreur lors de la génération de la réponse");
        }

        return response;
    }

    // ====================================================================
    //  GET CHAT HISTORY
    // ====================================================================

    public Map<String, Object> getHistory(String childId, String userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ChatMessage> messages = chatMessageRepository
                    .findByChildIdAndUserIdOrderByCreatedAtDesc(childId, userId);
            Collections.reverse(messages);

            List<Map<String, Object>> messageDtos = messages.stream()
                    .map(m -> {
                        Map<String, Object> dto = new LinkedHashMap<>();
                        dto.put("id", m.getId());
                        dto.put("role", m.getRole());
                        dto.put("content", m.getContent());
                        dto.put("createdAt", m.getCreatedAt() != null ? m.getCreatedAt().toString() : null);
                        return dto;
                    })
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("messages", messageDtos);
        } catch (Exception e) {
            log.error("Error getting chat history", e);
            response.put("success", false);
            response.put("message", "Erreur lors du chargement de l'historique");
        }
        return response;
    }

    // ====================================================================
    //  QUERY CLASSIFICATION (via Groq)
    // ====================================================================

    private QueryClassification classifyQuery(String userMessage) {
        try {
            String systemPrompt = """
                You classify parent questions about their child at nursery.
                Return ONLY valid JSON, no explanation:
                {
                  "type": "trend|event|synthesis|specific|open",
                  "dimensions": ["mood","appetite","energy","sociability","concentration","autonomy","sleep","motorSkills"],
                  "timeWindow": "today|last_week|last_2_weeks|last_month|all"
                }
                Rules:
                - type "trend": questions about evolution, progress, change over time
                - type "event": questions about specific days or events
                - type "synthesis": general questions about how the child is doing overall
                - type "specific": questions about a specific score or dimension on a date
                - type "open": open-ended, anecdotal, or emotional questions
                - dimensions: include ALL relevant dimensions. If unsure, include all 8.
                - timeWindow: the period the question is about. Default to "last_2_weeks" if unclear.
                """;

            String json = callGroqSimple(systemPrompt, userMessage);
            return parseClassification(json);
        } catch (Exception e) {
            log.warn("Query classification failed, using defaults", e);
            return new QueryClassification("synthesis",
                    List.of(DIMENSIONS), "last_2_weeks");
        }
    }

    private QueryClassification parseClassification(String json) {
        try {
            // Simple manual parsing to avoid adding a JSON library dependency
            String type = extractJsonField(json, "type");
            String timeWindow = extractJsonField(json, "timeWindow");

            List<String> dims = new ArrayList<>();
            int arrStart = json.indexOf("\"dimensions\"");
            if (arrStart >= 0) {
                int bracketStart = json.indexOf('[', arrStart);
                int bracketEnd = json.indexOf(']', bracketStart);
                if (bracketStart >= 0 && bracketEnd > bracketStart) {
                    String arrContent = json.substring(bracketStart + 1, bracketEnd);
                    for (String part : arrContent.split(",")) {
                        String dim = part.replaceAll("[\"\\s]", "");
                        if (!dim.isEmpty()) dims.add(dim);
                    }
                }
            }
            if (dims.isEmpty()) dims = List.of(DIMENSIONS);
            if (type == null || type.isEmpty()) type = "synthesis";
            if (timeWindow == null || timeWindow.isEmpty()) timeWindow = "last_2_weeks";

            return new QueryClassification(type, dims, timeWindow);
        } catch (Exception e) {
            return new QueryClassification("synthesis", List.of(DIMENSIONS), "last_2_weeks");
        }
    }

    private String extractJsonField(String json, String field) {
        int idx = json.indexOf("\"" + field + "\"");
        if (idx < 0) return null;
        int colonIdx = json.indexOf(':', idx);
        if (colonIdx < 0) return null;
        int quoteStart = json.indexOf('"', colonIdx + 1);
        if (quoteStart < 0) return null;
        int quoteEnd = json.indexOf('"', quoteStart + 1);
        if (quoteEnd < 0) return null;
        return json.substring(quoteStart + 1, quoteEnd);
    }

    // ====================================================================
    //  CONTEXT RETRIEVAL (Structured + Graph)
    // ====================================================================

    private String retrieveContext(String childId, String childName,
                                   QueryClassification classification, String userMessage) {
        StringBuilder ctx = new StringBuilder();
        ctx.append("=== INFORMATIONS SUR ").append(childName.toUpperCase()).append(" ===\n\n");

        // --- Strategy 1: Structured retrieval from daily_logs ---
        LocalDate cutoff = getCutoffDate(classification.timeWindow);
        List<DailyLog> logs = dailyLogRepository.findByChildIdOrderByDateDesc(childId);

        // Filter by date window and exclude internal notes (parent context only)
        List<DailyLog> filteredLogs = logs.stream()
                .filter(l -> {
                    try {
                        return !LocalDate.parse(l.getDate()).isBefore(cutoff);
                    } catch (Exception e) { return true; }
                })
                .filter(l -> !Boolean.TRUE.equals(l.getIsInternal()))
                .collect(Collectors.toList());

        if (!filteredLogs.isEmpty()) {
            ctx.append("--- OBSERVATIONS RÉCENTES (").append(filteredLogs.size()).append(" jours) ---\n");

            for (DailyLog dl : filteredLogs) {
                ctx.append("\n📅 ").append(dl.getDate()).append(":\n");
                for (String dim : classification.dimensions) {
                    Integer score = getScore(dl, dim);
                    if (score != null) {
                        String label = DIM_LABELS.getOrDefault(dim, dim);
                        ctx.append("  • ").append(label).append(": ").append(score).append("/5\n");
                    }
                }
                if (dl.getTags() != null && !dl.getTags().isEmpty()) {
                    ctx.append("  • Tags: ").append(String.join(", ", dl.getTags())).append("\n");
                }
                if (dl.getRemarks() != null && !dl.getRemarks().isEmpty()) {
                    ctx.append("  • Remarque: ").append(dl.getRemarks()).append("\n");
                }
                if (dl.getAiSummary() != null && !dl.getAiSummary().isEmpty()) {
                    ctx.append("  • Résumé IA: ").append(dl.getAiSummary()).append("\n");
                }
            }

            // Compute averages for the requested dimensions
            ctx.append("\n--- MOYENNES SUR LA PÉRIODE ---\n");
            for (String dim : classification.dimensions) {
                OptionalDouble avg = filteredLogs.stream()
                        .map(dl -> getScore(dl, dim))
                        .filter(Objects::nonNull)
                        .mapToInt(Integer::intValue)
                        .average();
                if (avg.isPresent()) {
                    ctx.append("  • ").append(DIM_LABELS.getOrDefault(dim, dim))
                            .append(": ").append(String.format("%.1f", avg.getAsDouble())).append("/5\n");
                }
            }

            // Trend detection (compare first half vs second half)
            if (filteredLogs.size() >= 4) {
                ctx.append("\n--- TENDANCES DÉTECTÉES ---\n");
                int mid = filteredLogs.size() / 2;
                List<DailyLog> recent = filteredLogs.subList(0, mid);
                List<DailyLog> older = filteredLogs.subList(mid, filteredLogs.size());

                for (String dim : classification.dimensions) {
                    OptionalDouble recentAvg = recent.stream()
                            .map(dl -> getScore(dl, dim)).filter(Objects::nonNull)
                            .mapToInt(Integer::intValue).average();
                    OptionalDouble olderAvg = older.stream()
                            .map(dl -> getScore(dl, dim)).filter(Objects::nonNull)
                            .mapToInt(Integer::intValue).average();

                    if (recentAvg.isPresent() && olderAvg.isPresent()) {
                        double diff = recentAvg.getAsDouble() - olderAvg.getAsDouble();
                        String trend;
                        if (diff > 0.5) trend = "📈 en amélioration";
                        else if (diff < -0.5) trend = "📉 en baisse";
                        else trend = "➡️ stable";

                        ctx.append("  • ").append(DIM_LABELS.getOrDefault(dim, dim))
                                .append(": ").append(trend)
                                .append(" (").append(String.format("%.1f", olderAvg.getAsDouble()))
                                .append(" → ").append(String.format("%.1f", recentAvg.getAsDouble()))
                                .append(")\n");
                    }
                }
            }
        } else {
            ctx.append("Aucune observation disponible pour cette période.\n");
        }

        // --- Strategy 2: Knowledge Graph context ---
        Optional<ChildProfile> profileOpt = childProfileRepository.findByChildId(childId);
        if (profileOpt.isPresent()) {
            ChildProfile profile = profileOpt.get();

            if (!profile.getPatterns().isEmpty()) {
                ctx.append("\n--- PATTERNS IDENTIFIÉS ---\n");
                for (ChildProfile.Pattern p : profile.getPatterns()) {
                    ctx.append("  • ").append(DIM_LABELS.getOrDefault(p.getDimension(), p.getDimension()))
                            .append(" (").append(p.getPeriod()).append("): ")
                            .append(p.getSummary()).append("\n");
                }
            }

            if (!profile.getMilestones().isEmpty()) {
                ctx.append("\n--- ÉTAPES MARQUANTES ---\n");
                // Show last 5 milestones
                List<ChildProfile.Milestone> recentMilestones = profile.getMilestones();
                int from = Math.max(0, recentMilestones.size() - 5);
                for (int i = from; i < recentMilestones.size(); i++) {
                    ChildProfile.Milestone m = recentMilestones.get(i);
                    ctx.append("  • ").append(m.getDate()).append(": ").append(m.getDescription()).append("\n");
                }
            }

            if (!profile.getCorrelations().isEmpty()) {
                ctx.append("\n--- CORRÉLATIONS ---\n");
                for (ChildProfile.Correlation c : profile.getCorrelations()) {
                    ctx.append("  • ").append(DIM_LABELS.getOrDefault(c.getDimension1(), c.getDimension1()))
                            .append(" ↔ ").append(DIM_LABELS.getOrDefault(c.getDimension2(), c.getDimension2()))
                            .append(": ").append(c.getNote()).append("\n");
                }
            }

            if (!profile.getWeeklyDigests().isEmpty()) {
                ctx.append("\n--- RÉSUMÉS HEBDOMADAIRES RÉCENTS ---\n");
                int from = Math.max(0, profile.getWeeklyDigests().size() - 3);
                for (int i = from; i < profile.getWeeklyDigests().size(); i++) {
                    ChildProfile.WeeklyDigest wd = profile.getWeeklyDigests().get(i);
                    ctx.append("  Semaine ").append(wd.getWeek()).append(": ").append(wd.getSummary()).append("\n");
                }
            }
        }

        // --- Strategy 3: Vector Search (semantic similarity) ---
        try {
            List<Double> queryEmbedding = embeddingService.embed(userMessage);
            if (queryEmbedding != null) {
                List<DailyLog> similarLogs = dailyLogVectorRepository.vectorSearch(
                        queryEmbedding, childId, 5);

                // Filter out logs already included in structured retrieval
                Set<String> structuredLogIds = filteredLogs.stream()
                        .map(DailyLog::getId).collect(Collectors.toSet());

                List<DailyLog> newLogs = similarLogs.stream()
                        .filter(l -> !structuredLogIds.contains(l.getId()))
                        .filter(l -> !Boolean.TRUE.equals(l.getIsInternal()))
                        .toList();

                if (!newLogs.isEmpty()) {
                    ctx.append("\n--- OBSERVATIONS SIMILAIRES (RECHERCHE SÉMANTIQUE) ---\n");
                    for (DailyLog dl : newLogs) {
                        ctx.append("\n🔍 ").append(dl.getDate()).append(":\n");
                        for (String dim : DIMENSIONS) {
                            Integer score = getScore(dl, dim);
                            if (score != null) {
                                ctx.append("  • ").append(DIM_LABELS.getOrDefault(dim, dim))
                                        .append(": ").append(score).append("/5\n");
                            }
                        }
                        if (dl.getTags() != null && !dl.getTags().isEmpty()) {
                            ctx.append("  • Tags: ").append(String.join(", ", dl.getTags())).append("\n");
                        }
                        if (dl.getRemarks() != null && !dl.getRemarks().isEmpty()) {
                            ctx.append("  • Remarque: ").append(dl.getRemarks()).append("\n");
                        }
                        if (dl.getAiSummary() != null && !dl.getAiSummary().isEmpty()) {
                            ctx.append("  • Résumé: ").append(dl.getAiSummary()).append("\n");
                        }
                    }
                }
                log.info("Vector search added {} new logs to context", newLogs.size());
            }
        } catch (Exception e) {
            log.warn("Vector search unavailable: {}", e.getMessage());
        }

        return ctx.toString();
    }

    // ====================================================================
    //  GROQ LLM CALLS
    // ====================================================================

    @SuppressWarnings("unchecked")
    private String callGroqChat(String childName, String userMessage,
                                String context, List<ChatMessage> history) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        // Build messages array
        List<Map<String, String>> messages = new ArrayList<>();

        // System prompt
        messages.add(Map.of("role", "system", "content",
                "Tu es un assistant bienveillant spécialisé en développement de l'enfant, " +
                "rattaché à une garderie (crèche). Ton rôle est de répondre aux questions des parents " +
                "à propos de leur enfant " + childName + " en te basant UNIQUEMENT sur les données " +
                "d'observation fournies ci-dessous.\n\n" +
                "RÈGLES STRICTES:\n" +
                "1. Réponds TOUJOURS en français, avec un ton chaleureux et rassurant.\n" +
                "2. Base tes réponses UNIQUEMENT sur les données fournies. Ne les invente jamais.\n" +
                "3. Si tu n'as pas assez de données, dis-le honnêtement.\n" +
                "4. Ne mentionne jamais les scores bruts (ex: 4/5). Transforme-les en langage naturel " +
                "(ex: 'Très bon appétit', 'Il était très concentré').\n" +
                "5. Quand tu détectes des tendances, explique-les simplement.\n" +
                "6. Donne des réponses concises (3-5 phrases) sauf si le parent demande plus de détails.\n" +
                "7. Tu peux suggérer de poser des questions à l'éducateur si la donnée manque.\n\n" +
                "DONNÉES D'OBSERVATION:\n" + context
        ));

        // Conversation history (last 10 messages for continuity)
        int histStart = Math.max(0, history.size() - 10);
        for (int i = histStart; i < history.size(); i++) {
            ChatMessage m = history.get(i);
            messages.add(Map.of("role", m.getRole(), "content", m.getContent()));
        }

        // Current user message
        messages.add(Map.of("role", "user", "content", userMessage));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", groqModel);
        body.put("messages", messages);
        body.put("max_tokens", 500);
        body.put("temperature", 0.6);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> resp = restTemplate.exchange(
                "https://api.groq.com/openai/v1/chat/completions",
                HttpMethod.POST, entity, Map.class);

        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> msgObj = (Map<String, Object>) choices.get(0).get("message");
                if (msgObj != null) return (String) msgObj.get("content");
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private String callGroqSimple(String systemPrompt, String userMessage) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", "llama-3.1-8b-instant"); // fast model for classification
        body.put("messages", messages);
        body.put("max_tokens", 150);
        body.put("temperature", 0.1);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> resp = restTemplate.exchange(
                "https://api.groq.com/openai/v1/chat/completions",
                HttpMethod.POST, entity, Map.class);

        if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> msgObj = (Map<String, Object>) choices.get(0).get("message");
                if (msgObj != null) return (String) msgObj.get("content");
            }
        }

        return "{}";
    }

    // ====================================================================
    //  CHILD PROFILE UPDATE (called after each new daily log)
    // ====================================================================

    public void updateChildProfile(String childId) {
        try {
            List<DailyLog> allLogs = dailyLogRepository.findByChildIdOrderByDateDesc(childId);
            if (allLogs.isEmpty()) return;

            ChildProfile profile = childProfileRepository.findByChildId(childId)
                    .orElseGet(() -> {
                        ChildProfile p = new ChildProfile();
                        p.setChildId(childId);
                        return p;
                    });

            profile.setTotalLogs(allLogs.size());

            // Compute running averages
            Map<String, Map<String, Double>> averages = new HashMap<>();
            for (String dim : DIMENSIONS) {
                Map<String, Double> dimAvg = new HashMap<>();
                for (int window : new int[]{7, 14, 30}) {
                    String cutoff = LocalDate.now().minusDays(window).toString();
                    OptionalDouble avg = allLogs.stream()
                            .filter(l -> l.getDate() != null && l.getDate().compareTo(cutoff) >= 0)
                            .map(l -> getScore(l, dim))
                            .filter(Objects::nonNull)
                            .mapToInt(Integer::intValue)
                            .average();
                    avg.ifPresent(v -> dimAvg.put(window + "d", Math.round(v * 10.0) / 10.0));
                }
                if (!dimAvg.isEmpty()) averages.put(dim, dimAvg);
            }
            profile.setAverages(averages);

            // Detect patterns (compare 7d avg to 30d avg)
            List<ChildProfile.Pattern> patterns = new ArrayList<>();
            for (String dim : DIMENSIONS) {
                Map<String, Double> dimAvg = averages.get(dim);
                if (dimAvg != null && dimAvg.containsKey("7d") && dimAvg.containsKey("30d")) {
                    double recent = dimAvg.get("7d");
                    double longer = dimAvg.get("30d");
                    double diff = recent - longer;

                    if (Math.abs(diff) > 0.4) {
                        ChildProfile.Pattern pattern = new ChildProfile.Pattern();
                        pattern.setDimension(dim);
                        pattern.setTrend(diff > 0 ? "improving" : "declining");
                        pattern.setPeriod("derniers 7 jours vs 30 jours");
                        pattern.setSummary(DIM_LABELS.getOrDefault(dim, dim) + " " +
                                (diff > 0 ? "en amélioration" : "en baisse") +
                                " (" + String.format("%.1f", longer) + " → " +
                                String.format("%.1f", recent) + ")");
                        patterns.add(pattern);
                    }
                }
            }
            profile.setPatterns(patterns);

            // Detect milestones (score of 5 when previous avg was < 3.5, or score 1 when avg > 3.5)
            DailyLog latest = allLogs.get(0);
            List<ChildProfile.Milestone> milestones = new ArrayList<>(profile.getMilestones());
            for (String dim : DIMENSIONS) {
                Integer score = getScore(latest, dim);
                Map<String, Double> dimAvg = averages.get(dim);
                if (score != null && dimAvg != null && dimAvg.containsKey("14d")) {
                    double avg14 = dimAvg.get("14d");
                    if (score == 5 && avg14 < 3.5) {
                        ChildProfile.Milestone m = new ChildProfile.Milestone();
                        m.setDate(latest.getDate());
                        m.setType("improvement");
                        m.setDescription("Score exceptionnel en " +
                                DIM_LABELS.getOrDefault(dim, dim).toLowerCase() +
                                " (5/5, moyenne habituelle: " + String.format("%.1f", avg14) + ")");
                        m.setSourceLogId(latest.getId());
                        milestones.add(m);
                    } else if (score <= 1 && avg14 > 3.5) {
                        ChildProfile.Milestone m = new ChildProfile.Milestone();
                        m.setDate(latest.getDate());
                        m.setType("concern");
                        m.setDescription("Score très bas en " +
                                DIM_LABELS.getOrDefault(dim, dim).toLowerCase() +
                                " (1/5, moyenne habituelle: " + String.format("%.1f", avg14) + ")");
                        m.setSourceLogId(latest.getId());
                        milestones.add(m);
                    }
                }
            }
            // Keep last 20 milestones
            if (milestones.size() > 20) {
                milestones = milestones.subList(milestones.size() - 20, milestones.size());
            }
            profile.setMilestones(milestones);

            // Simple correlation detection (Pearson-like on last 14 logs)
            List<DailyLog> recentLogs = allLogs.stream().limit(14).collect(Collectors.toList());
            if (recentLogs.size() >= 5) {
                List<ChildProfile.Correlation> correlations = new ArrayList<>();
                for (int i = 0; i < DIMENSIONS.length; i++) {
                    for (int j = i + 1; j < DIMENSIONS.length; j++) {
                        double corr = computeCorrelation(recentLogs, DIMENSIONS[i], DIMENSIONS[j]);
                        if (Math.abs(corr) > 0.6) {
                            ChildProfile.Correlation c = new ChildProfile.Correlation();
                            c.setDimension1(DIMENSIONS[i]);
                            c.setDimension2(DIMENSIONS[j]);
                            c.setType(corr > 0 ? "positive" : "negative");
                            c.setStrength(Math.abs(corr));
                            c.setNote(DIM_LABELS.getOrDefault(DIMENSIONS[i], DIMENSIONS[i]) + " et " +
                                    DIM_LABELS.getOrDefault(DIMENSIONS[j], DIMENSIONS[j]) +
                                    (corr > 0 ? " évoluent ensemble" : " évoluent en sens inverse") +
                                    " (corrélation: " + String.format("%.2f", corr) + ")");
                            correlations.add(c);
                        }
                    }
                }
                profile.setCorrelations(correlations);
            }

            profile.setLastUpdated(Instant.now());
            childProfileRepository.save(profile);
            log.info("Updated child profile for {}", childId);

        } catch (Exception e) {
            log.error("Error updating child profile for {}", childId, e);
        }
    }

    // ====================================================================
    //  HELPERS
    // ====================================================================

    private Integer getScore(DailyLog log, String dimension) {
        return switch (dimension) {
            case "mood" -> log.getMood();
            case "appetite" -> log.getAppetite();
            case "energy" -> log.getEnergy();
            case "sociability" -> log.getSociability();
            case "concentration" -> log.getConcentration();
            case "autonomy" -> log.getAutonomy();
            case "sleep" -> log.getSleep();
            case "motorSkills" -> log.getMotorSkills();
            default -> null;
        };
    }

    private LocalDate getCutoffDate(String timeWindow) {
        return switch (timeWindow) {
            case "today" -> LocalDate.now();
            case "last_week" -> LocalDate.now().minusDays(7);
            case "last_2_weeks" -> LocalDate.now().minusDays(14);
            case "last_month" -> LocalDate.now().minusDays(30);
            default -> LocalDate.now().minusDays(60);
        };
    }

    private double computeCorrelation(List<DailyLog> logs, String dim1, String dim2) {
        List<double[]> pairs = new ArrayList<>();
        for (DailyLog log : logs) {
            Integer s1 = getScore(log, dim1);
            Integer s2 = getScore(log, dim2);
            if (s1 != null && s2 != null) {
                pairs.add(new double[]{s1, s2});
            }
        }
        if (pairs.size() < 3) return 0;

        double mean1 = pairs.stream().mapToDouble(p -> p[0]).average().orElse(0);
        double mean2 = pairs.stream().mapToDouble(p -> p[1]).average().orElse(0);

        double num = 0, den1 = 0, den2 = 0;
        for (double[] p : pairs) {
            double d1 = p[0] - mean1;
            double d2 = p[1] - mean2;
            num += d1 * d2;
            den1 += d1 * d1;
            den2 += d2 * d2;
        }

        double den = Math.sqrt(den1 * den2);
        return den == 0 ? 0 : num / den;
    }

    // Inner class for query classification
    private static class QueryClassification {
        final String type;
        final List<String> dimensions;
        final String timeWindow;

        QueryClassification(String type, List<String> dimensions, String timeWindow) {
            this.type = type;
            this.dimensions = dimensions;
            this.timeWindow = timeWindow;
        }
    }
}
