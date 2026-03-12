package com.nursery.controller;

import com.nursery.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable String id) {
        Map<String, Object> result = userService.getUser(id);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return ResponseEntity.status(success ? 200 : 404).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable String id,
                                                          @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        String phone = (String) body.get("phone");
        Map<String, Object> result = userService.updateUser(id, name, phone);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return ResponseEntity.status(success ? 200 : 404).body(result);
    }
}
