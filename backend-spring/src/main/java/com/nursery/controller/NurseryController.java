package com.nursery.controller;

import com.nursery.service.NurseryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/nurseries")
public class NurseryController {

    private final NurseryService nurseryService;

    public NurseryController(NurseryService nurseryService) {
        this.nurseryService = nurseryService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createNursery(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = nurseryService.createNursery(body);
        return ResponseEntity.status(201).body(result);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getNurseries(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double max_price,
            @RequestParam(required = false) Double min_rating,
            @RequestParam(required = false) Integer min_spots) {
        return ResponseEntity.ok(nurseryService.getNurseries(city, max_price, min_rating, min_spots));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<Map<String, Object>> getNurseriesByOwner(@PathVariable String ownerId) {
        return ResponseEntity.ok(nurseryService.getNurseriesByOwner(ownerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getNursery(@PathVariable String id) {
        Map<String, Object> result = nurseryService.getNursery(id);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return ResponseEntity.status(success ? 200 : 404).body(result);
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<Map<String, Object>> getReviews(@PathVariable String id) {
        return ResponseEntity.ok(nurseryService.getReviews(id));
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<Map<String, Object>> createReview(@PathVariable String id,
                                                             @RequestBody Map<String, Object> body) {
        String parentId = (String) body.get("parentId");
        Integer rating = body.get("rating") instanceof Number ? ((Number) body.get("rating")).intValue() : null;
        String comment = (String) body.get("comment");
        return ResponseEntity.status(201).body(nurseryService.createReview(id, parentId, rating, comment));
    }

    @GetMapping("/{nurseryId}/stats")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable String nurseryId) {
        Map<String, Object> result = nurseryService.getStats(nurseryId);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return ResponseEntity.status(success ? 200 : 404).body(result);
    }

    @GetMapping("/{nurseryId}/enrolled-children")
    public ResponseEntity<Map<String, Object>> getEnrolledChildren(@PathVariable String nurseryId) {
        return ResponseEntity.ok(nurseryService.getEnrolledChildren(nurseryId));
    }

    @GetMapping("/{nurseryId}/schedule")
    public ResponseEntity<Map<String, Object>> getSchedule(@PathVariable String nurseryId) {
        return ResponseEntity.ok(nurseryService.getSchedule(nurseryId));
    }

    @PostMapping("/{nurseryId}/schedule")
    public ResponseEntity<Map<String, Object>> createSchedule(@PathVariable String nurseryId,
                                                               @RequestBody Map<String, Object> body) {
        String timeSlot = (String) body.get("timeSlot");
        String activityName = (String) body.get("activityName");
        String description = (String) body.get("description");
        Integer participantCount = body.get("participantCount") instanceof Number
                ? ((Number) body.get("participantCount")).intValue() : null;
        return ResponseEntity.status(201)
                .body(nurseryService.createSchedule(nurseryId, timeSlot, activityName, description, participantCount));
    }

    @GetMapping("/{nurseryId}/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(@PathVariable String nurseryId) {
        return ResponseEntity.ok(nurseryService.getStatistics(nurseryId));
    }
}
