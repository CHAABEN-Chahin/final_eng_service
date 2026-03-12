package com.nursery.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Embedding service using HuggingFace Inference API
 * with the open-source model: sentence-transformers/all-MiniLM-L6-v2 (384 dimensions)
 */
@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);
    private static final String HF_API_URL =
            "https://api-inference.huggingface.co/pipeline/feature-extraction/sentence-transformers/all-MiniLM-L6-v2";
    public static final int EMBEDDING_DIM = 384;

    @org.springframework.beans.factory.annotation.Value("${huggingface.api.token:}")
    private String hfToken;

    /**
     * Generate a 384-dimensional embedding for the given text.
     * Returns null if the API call fails.
     */
    @SuppressWarnings("unchecked")
    public List<Double> embed(String text) {
        if (text == null || text.isBlank()) return null;
        if (hfToken == null || hfToken.isBlank()) {
            log.warn("HuggingFace token not configured — skipping embedding");
            return null;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(hfToken);

            // HuggingFace feature-extraction expects {"inputs": "..."} and returns [[...]]
            Map<String, Object> body = Map.of(
                    "inputs", truncate(text, 512),
                    "options", Map.of("wait_for_model", true)
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<List> resp = restTemplate.exchange(
                    HF_API_URL, HttpMethod.POST, entity, List.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                List<?> outer = resp.getBody();
                if (!outer.isEmpty() && outer.get(0) instanceof List) {
                    List<Number> rawEmbedding = (List<Number>) outer.get(0);
                    if (rawEmbedding.size() == EMBEDDING_DIM) {
                        return rawEmbedding.stream()
                                .map(Number::doubleValue)
                                .toList();
                    }
                }
            }

            log.warn("HuggingFace API returned unexpected shape");
            return null;

        } catch (Exception e) {
            log.error("Error calling HuggingFace embedding API", e);
            return null;
        }
    }

    /**
     * Truncate text to a maximum number of whitespace-separated tokens
     * (MiniLM max input is ~256 tokens, we keep it generous)
     */
    private String truncate(String text, int maxChars) {
        if (text.length() <= maxChars) return text;
        return text.substring(0, maxChars);
    }
}
