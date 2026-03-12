package com.nursery.controller;

import com.nursery.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createEnrollment(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = enrollmentService.createEnrollment(body);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return ResponseEntity.status(success ? 201 : 400).body(result);
    }

    @GetMapping("/nursery/{nurseryId}")
    public ResponseEntity<Map<String, Object>> getByNursery(@PathVariable String nurseryId) {
        return ResponseEntity.ok(enrollmentService.getByNursery(nurseryId));
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<Map<String, Object>> getByParent(@PathVariable String parentId) {
        return ResponseEntity.ok(enrollmentService.getByParent(parentId));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll() {
        return ResponseEntity.ok(enrollmentService.getAll());
    }

    @PatchMapping("/{enrollmentId}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable String enrollmentId,
                                                             @RequestBody Map<String, Object> body) {
        String status = (String) body.get("status");
        Map<String, Object> result = enrollmentService.updateStatus(enrollmentId, status);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return ResponseEntity.status(success ? 200 : 404).body(result);
    }

    @PostMapping("/{enrollmentId}/accept")
    public ResponseEntity<Map<String, Object>> accept(@PathVariable String enrollmentId) {
        Map<String, Object> result = enrollmentService.accept(enrollmentId);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return ResponseEntity.status(success ? 200 : 404).body(result);
    }

    @PostMapping("/{enrollmentId}/reject")
    public ResponseEntity<Map<String, Object>> reject(@PathVariable String enrollmentId) {
        Map<String, Object> result = enrollmentService.reject(enrollmentId);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return ResponseEntity.status(success ? 200 : 404).body(result);
    }
}
