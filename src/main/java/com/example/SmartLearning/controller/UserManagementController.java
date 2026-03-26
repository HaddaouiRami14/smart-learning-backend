package com.example.SmartLearning.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SmartLearning.DTO.BanRequest;
import com.example.SmartLearning.Enum.Role;
import com.example.SmartLearning.Repository.BannedEmailRepository;
import com.example.SmartLearning.Repository.UserRepository;
import com.example.SmartLearning.model.User;
import com.example.SmartLearning.service.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserRepository userRepository;
    private final AdminService adminService; 
    private final BannedEmailRepository bannedEmailRepository; 

    @GetMapping
    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {

        if (user.isBanned() && user.getBanExpiresAt() != null
                && user.getBanExpiresAt().isBefore(LocalDateTime.now())) {
            user.setBanned(false);
            user.setBanExpiresAt(null);
            userRepository.save(user);
            bannedEmailRepository.deleteByEmail(user.getEmail());
        }
        if (!user.isBanned() && bannedEmailRepository.existsByEmail(user.getEmail())) {
            bannedEmailRepository.deleteByEmail(user.getEmail());
        }

            Map<String, Object> u = new HashMap<>();
            u.put("id", user.getId().toString());
            u.put("user_id", user.getId().toString());
            u.put("full_name", user.getUsername());          
            u.put("avatar_url", user.getPicture());   
            u.put("role", user.getRole().name());
            u.put("isBanned", user.isBanned()); 
            u.put("banExpiresAt", user.getBanExpiresAt() != null  
                ? user.getBanExpiresAt().toString()
                : null);
            return u;
        }).toList();
    }

    @PostMapping("/{userId}/ban")
    public ResponseEntity<?> banUser(@PathVariable Long userId , @RequestBody(required = false) BanRequest request) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getRole() == Role.ADMIN) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cannot ban an admin user"));
            }

            adminService.banUser(userId, request != null ? request.getDurationDays() : null);
            return ResponseEntity.ok()
                .body(Map.of("message", "User banned successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{userId}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unbanUser(@PathVariable Long userId) {
        adminService.unbanUser(userId);
        return ResponseEntity.ok("User unbanned successfully");
    }

}