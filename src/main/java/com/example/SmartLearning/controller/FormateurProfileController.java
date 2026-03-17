package com.example.SmartLearning.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SmartLearning.DTO.ChangePasswordRequest;
import com.example.SmartLearning.DTO.FormateurProfileResponse;
import com.example.SmartLearning.DTO.UpdateFormateurProfileRequest;
import com.example.SmartLearning.Repository.UserRepository;
import com.example.SmartLearning.model.User;
import com.example.SmartLearning.security.JwtUserPrincipal;
import com.example.SmartLearning.service.FormateurProfileService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/formateur/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FormateurProfileController {

    private final FormateurProfileService formateurProfileService;
    private final UserRepository userRepository;

    private String getUserEmail(Authentication authentication) {
        JwtUserPrincipal jwtPrincipal = (JwtUserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(jwtPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getEmail();
    }

    @GetMapping
    public ResponseEntity<FormateurProfileResponse> getProfile(Authentication authentication) {
        String email = getUserEmail(authentication);
        return ResponseEntity.ok(formateurProfileService.getProfile(email));
    }

    @PutMapping
    public ResponseEntity<FormateurProfileResponse> updateProfile(
            Authentication authentication,
            @RequestBody UpdateFormateurProfileRequest request) {

        String email = getUserEmail(authentication);
        return ResponseEntity.ok(formateurProfileService.updateProfile(email, request));
    }

        @PutMapping("/change-password")
        public ResponseEntity<?> changePassword(
                Authentication authentication,
                @RequestBody ChangePasswordRequest request) {

            String email = getUserEmail(authentication);
            formateurProfileService.changePassword(email, request);
            return ResponseEntity.ok("Password changed successfully");
        }
}