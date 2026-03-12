package com.nursery.controller;

import com.nursery.service.ParentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/parents")
public class ParentController {

    private final ParentService parentService;

    public ParentController(ParentService parentService) {
        this.parentService = parentService;
    }

    @GetMapping("/{parentId}/children")
    public ResponseEntity<Map<String, Object>> getChildren(@PathVariable String parentId) {
        return ResponseEntity.ok(parentService.getChildren(parentId));
    }

    @GetMapping("/{parentId}/nurseries")
    public ResponseEntity<Map<String, Object>> getNurseries(@PathVariable String parentId) {
        return ResponseEntity.ok(parentService.getNurseries(parentId));
    }

    @GetMapping("/{parentId}/today-program")
    public ResponseEntity<Map<String, Object>> getTodayProgram(@PathVariable String parentId) {
        return ResponseEntity.ok(parentService.getTodayProgram(parentId));
    }

    @GetMapping("/{parentId}/nursery-reviews")
    public ResponseEntity<Map<String, Object>> getNurseryReviews(@PathVariable String parentId) {
        return ResponseEntity.ok(parentService.getNurseryReviews(parentId));
    }
}
