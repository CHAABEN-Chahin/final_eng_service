package com.nursery.controller;

import com.nursery.service.DailyLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/daily-logs")
public class DailyLogController {

    private final DailyLogService dailyLogService;

    public DailyLogController(DailyLogService dailyLogService) {
        this.dailyLogService = dailyLogService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createLog(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = dailyLogService.createLog(body);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return ResponseEntity.status(success ? 201 : 400).body(result);
    }

    @GetMapping("/child/{childId}")
    public ResponseEntity<Map<String, Object>> getLogsByChild(@PathVariable String childId) {
        return ResponseEntity.ok(dailyLogService.getLogsByChild(childId));
    }

    @GetMapping("/child/{childId}/parent")
    public ResponseEntity<Map<String, Object>> getLogsByChildForParent(@PathVariable String childId) {
        return ResponseEntity.ok(dailyLogService.getLogsByChildForParent(childId));
    }

    @GetMapping("/child/{childId}/today")
    public ResponseEntity<Map<String, Object>> getTodayLog(@PathVariable String childId) {
        return ResponseEntity.ok(dailyLogService.getTodayLog(childId));
    }

    /**
     * Backfill embeddings for all logs that don't have one yet.
     * Call once after setting up the HuggingFace token.
     */
    @PostMapping("/backfill-embeddings")
    public ResponseEntity<Map<String, Object>> backfillEmbeddings() {
        return ResponseEntity.ok(dailyLogService.backfillEmbeddings());
    }
}
