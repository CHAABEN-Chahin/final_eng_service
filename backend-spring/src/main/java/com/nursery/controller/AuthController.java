package com.nursery.controller;

import com.nursery.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> body) {
        String email = (String) body.get("email");
        String password = (String) body.get("password");
        String name = (String) body.get("name");
        String userType = (String) body.get("user_type");
        String phone = (String) body.get("phone");
        Map<String, Object> result = authService.register(email, password, name, userType, phone);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return ResponseEntity.status(success ? 201 : 400).body(result);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> body) {
        String email = (String) body.get("email");
        String password = (String) body.get("password");
        Map<String, Object> result = authService.login(email, password);
        boolean success = Boolean.TRUE.equals(result.get("success"));
        return ResponseEntity.status(success ? 200 : 401).body(result);
    }
}
