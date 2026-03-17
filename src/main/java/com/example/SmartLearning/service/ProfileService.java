package com.example.SmartLearning.service;

import lombok.RequiredArgsConstructor;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartLearning.DTO.ChangePasswordRequest;
import com.example.SmartLearning.DTO.ProfileResponse;
import com.example.SmartLearning.DTO.UpdateProfileRequest;
import com.example.SmartLearning.Enum.Role;
import com.example.SmartLearning.Repository.LoginHistoryRepository;
import com.example.SmartLearning.Repository.UserRepository;
import com.example.SmartLearning.model.LoginHistory;
import com.example.SmartLearning.model.User;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {
    
    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    
    public ProfileResponse getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return convertToDTO(user);
    }
    
    @Transactional
    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setUsername(request.getUsername());
        
        if (request.getPicture() != null && !request.getPicture().isEmpty()) {
            user.setPicture(request.getPicture());
        }
        
        if (request.getTimezone() != null && !request.getTimezone().isEmpty()) {
            user.setTimezone(request.getTimezone());
        }
        
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }
    
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getPassword() == null) {
            throw new RuntimeException("Cannot change password for OAuth users");
        }
        
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New passwords do not match");
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        sendPasswordChangedEmail(user.getEmail(), user.getUsername());
    }
    
    private ProfileResponse convertToDTO(User user) {
        // Get last 10 login history entries
        List<LoginHistory> history = loginHistoryRepository
            .findTop10ByUserOrderByLoginTimeDesc(user);
        
        List<String> loginHistoryStrings = history.stream()
            .map(h -> h.getLoginTime().format(DATE_FORMATTER) + 
                     " - " + (h.getIpAddress() != null ? h.getIpAddress() : "Unknown IP"))
            .collect(Collectors.toList());
        
        return ProfileResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .picture(user.getPicture())
            .role(mapRoleToFrontend(user.getRole()))
            .provider(user.getProvider() != null ? user.getProvider().name() : "LOCAL")
            .timezone(user.getTimezone() != null ? user.getTimezone() : "UTC")
            .loginHistory(loginHistoryStrings)
            .build();
    }
    
    private String mapRoleToFrontend(Role role) {
        switch (role) {
            case ADMIN: return "admin";
            case FORMATEUR: return "trainer";
            case APPRENANT: return "learner";
            default: return role.name().toLowerCase();
        }
    }

    private void sendPasswordChangedEmail(String email, String username) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(email);
    message.setSubject("Password Changed Successfully - SkillPath");
    message.setText(
        "Hello " + username + ",\n\n" +
        "Your password has been changed successfully.\n\n" +
        "If you did not make this change, please contact support immediately.\n\n" +
        "Best regards,\nSkillPath Team"
    );
    mailSender.send(message);
}
}
