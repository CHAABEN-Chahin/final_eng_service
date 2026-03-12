package com.nursery.controller;

import com.nursery.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getNotifications(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getNotifications(userId));
    }

    @GetMapping("/{userId}/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, Object>> markRead(@PathVariable String notificationId) {
        Map<String, Object> result = notificationService.markRead(notificationId);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return ResponseEntity.status(success ? 200 : 404).body(result);
    }

    @PostMapping("/{userId}/read-all")
    public ResponseEntity<Map<String, Object>> markAllRead(@PathVariable String userId) {
        return ResponseEntity.ok(notificationService.markAllRead(userId));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable String notificationId) {
        Map<String, Object> result = notificationService.deleteNotification(notificationId);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return ResponseEntity.status(success ? 200 : 404).body(result);
    }
}
