package com.example.SmartLearning.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartLearning.Repository.BannedEmailRepository;
import com.example.SmartLearning.Repository.LoginHistoryRepository;
import com.example.SmartLearning.Repository.UserRepository;
import com.example.SmartLearning.model.BannedEmail;
import com.example.SmartLearning.model.User;


@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BannedEmailRepository bannedEmailRepository;

    @Autowired
    private LoginHistoryRepository loginHistoryRepository; 

    @Transactional
    public void banUser(Long userId , Integer durationDays) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        String email = user.getEmail();

        
        loginHistoryRepository.deleteByUser(user);

        
        if (!bannedEmailRepository.existsByEmail(email)) {
            BannedEmail bannedEmail = BannedEmail.builder()
                .email(email)
                .reason("Banned by admin")
                .build();
            bannedEmailRepository.save(bannedEmail);
        }

        user.setBanned(true);
        if (durationDays != null) {
            user.setBanExpiresAt(LocalDateTime.now().plusDays(durationDays));
        } else {
            user.setBanExpiresAt(null); 
        }
        userRepository.save(user);
    }

    @Transactional
    public void unbanUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        
        bannedEmailRepository.deleteByEmail(user.getEmail());

        
        user.setBanned(false);
        user.setBanExpiresAt(null);
        userRepository.save(user);
    }
}