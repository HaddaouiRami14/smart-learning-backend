package com.example.SmartLearning.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartLearning.DTO.ForgotPasswordRequest;
import com.example.SmartLearning.DTO.PasswordResetResponse;
import com.example.SmartLearning.DTO.PasswordResetToken;
import com.example.SmartLearning.DTO.ResetPasswordRequest;
import com.example.SmartLearning.Repository.PasswordResetTokenRepository;
import com.example.SmartLearning.Repository.UserRepository;
import com.example.SmartLearning.model.User;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public PasswordResetResponse forgotPassword(ForgotPasswordRequest request) {
        try {
           
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            
            if (userOptional.isEmpty()) {
               
                return new PasswordResetResponse(true, "If the email exists, a reset link has been sent.");
            }
            
            User user = userOptional.get();
            
           
            tokenRepository.deleteByUser(user);
          
            String token = UUID.randomUUID().toString();
            
        
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setUser(user);
            resetToken.setExpiryDate(LocalDateTime.now().plusHours(1)); 
            resetToken.setUsed(false);
            
            tokenRepository.save(resetToken);
            
           
            emailService.sendPasswordResetEmail(user.getEmail(), token);
            
            return new PasswordResetResponse(true, "If the email exists, a reset link has been sent.");
            
        } catch (Exception e) {
            return new PasswordResetResponse(false, "An error occurred. Please try again later.");
        }
    }
    
    @Transactional
    public PasswordResetResponse resetPassword(ResetPasswordRequest request) {
        try {
            
            Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(request.getToken());
            
            if (tokenOptional.isEmpty()) {
                return new PasswordResetResponse(false, "Invalid or expired reset token.");
            }
            
            PasswordResetToken resetToken = tokenOptional.get();
            
           
            if (resetToken.isExpired()) {
                return new PasswordResetResponse(false, "Reset token has expired. Please request a new one.");
            }
            
            
            if (resetToken.isUsed()) {
                return new PasswordResetResponse(false, "This reset token has already been used.");
            }
            
            User user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            
            
            resetToken.setUsed(true);
            tokenRepository.save(resetToken);
            
            return new PasswordResetResponse(true, "Password reset successful. You can now login with your new password.");
            
        } catch (Exception e) {
            return new PasswordResetResponse(false, "An error occurred. Please try again.");
        }
    }
}
