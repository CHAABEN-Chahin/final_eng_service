package com.nursery.controller;

import com.nursery.service.ChildChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/child-chat")
public class ChildChatController {

    private final ChildChatService childChatService;

    public ChildChatController(ChildChatService childChatService) {
        this.childChatService = childChatService;
    }

    /**
     * POST /api/child-chat
     * Body: { childId, userId, message }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> body) {
        String childId = (String) body.get("childId");
        String userId = (String) body.get("userId");
        String message = (String) body.get("message");

        if (childId == null || userId == null || message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "childId, userId, and message are required"
            ));
        }

        Map<String, Object> result = childChatService.chat(childId, userId, message);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return ResponseEntity.status(success ? 200 : 500).body(result);
    }

    /**
     * GET /api/child-chat/{childId}/{userId}/history
     */
    @GetMapping("/{childId}/{userId}/history")
    public ResponseEntity<Map<String, Object>> getHistory(
            @PathVariable String childId,
            @PathVariable String userId) {
        return ResponseEntity.ok(childChatService.getHistory(childId, userId));
    }
}
