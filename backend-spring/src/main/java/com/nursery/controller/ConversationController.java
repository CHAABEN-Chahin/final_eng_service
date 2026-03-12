package com.nursery.controller;

import com.nursery.service.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping("/get-or-create")
    public ResponseEntity<Map<String, Object>> getOrCreate(@RequestBody Map<String, Object> body) {
        String parentId = (String) body.get("parentId");
        String nurseryId = (String) body.get("nurseryId");
        return ResponseEntity.ok(conversationService.getOrCreate(parentId, nurseryId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserConversations(@PathVariable String userId) {
        return ResponseEntity.ok(conversationService.getUserConversations(userId));
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<Map<String, Object>> getMessages(@PathVariable String conversationId) {
        return ResponseEntity.ok(conversationService.getMessages(conversationId));
    }

    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<Map<String, Object>> sendMessage(@PathVariable String conversationId,
                                                            @RequestBody Map<String, Object> body) {
        String senderId = (String) body.get("senderId");
        String recipientId = (String) body.get("recipientId");
        String content = (String) body.get("content");
        return ResponseEntity.status(201)
                .body(conversationService.sendMessage(conversationId, senderId, recipientId, content));
    }

    @PostMapping("/{conversationId}/mark-read")
    public ResponseEntity<Map<String, Object>> markRead(@PathVariable String conversationId,
                                                         @RequestBody Map<String, Object> body) {
        String userId = (String) body.get("userId");
        return ResponseEntity.ok(conversationService.markRead(conversationId, userId));
    }
}
