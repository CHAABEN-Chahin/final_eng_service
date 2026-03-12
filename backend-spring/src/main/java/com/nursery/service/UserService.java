package com.nursery.service;

import com.nursery.model.User;
import com.nursery.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Map<String, Object> getUser(String id) {
        Map<String, Object> result = new HashMap<>();
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            result.put("success", false);
            result.put("message", "User not found");
            return result;
        }
        result.put("success", true);
        result.put("user", AuthService.buildUserDto(opt.get()));
        return result;
    }

    public Map<String, Object> updateUser(String id, String name, String phone) {
        Map<String, Object> result = new HashMap<>();
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            result.put("success", false);
            result.put("message", "User not found");
            return result;
        }
        User user = opt.get();
        if (name != null) user.setName(name);
        if (phone != null) user.setPhone(phone);
        userRepository.save(user);
        result.put("success", true);
        result.put("user", AuthService.buildUserDto(user));
        return result;
    }
}
