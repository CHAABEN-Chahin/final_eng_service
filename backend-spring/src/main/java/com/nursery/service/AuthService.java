package com.nursery.service;

import com.nursery.model.User;
import com.nursery.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public Map<String, Object> register(String email, String password, String name, String userType, String phone) {
        Map<String, Object> result = new HashMap<>();

        if (userRepository.existsByEmail(email)) {
            result.put("success", false);
            result.put("message", "Email already registered");
            return result;
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(hashPassword(password));
        user.setName(name);
        user.setUserType(userType);
        user.setPhone(phone);

        User saved = userRepository.save(user);

        result.put("success", true);
        result.put("user", buildUserDto(saved));
        return result;
    }

    public Map<String, Object> login(String email, String password) {
        Map<String, Object> result = new HashMap<>();

        Optional<User> optUser = userRepository.findByEmail(email);
        if (optUser.isEmpty()) {
            result.put("success", false);
            result.put("message", "Invalid credentials");
            return result;
        }

        User user = optUser.get();
        if (!user.getPasswordHash().equals(hashPassword(password))) {
            result.put("success", false);
            result.put("message", "Invalid credentials");
            return result;
        }

        result.put("success", true);
        result.put("user", buildUserDto(user));
        return result;
    }

    public static Map<String, Object> buildUserDto(User user) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", user.getId());
        dto.put("email", user.getEmail());
        dto.put("type", user.getUserType());
        dto.put("name", user.getName());
        dto.put("phone", user.getPhone());
        dto.put("createdAt", user.getCreatedAt());
        return dto;
    }
}
