package com.example.SmartLearning.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.SmartLearning.DTO.ChangePasswordRequest;
import com.example.SmartLearning.DTO.ProfileResponse;
import com.example.SmartLearning.DTO.UpdateProfileRequest;
import com.example.SmartLearning.Repository.UserRepository;
import com.example.SmartLearning.model.User;
import com.example.SmartLearning.security.JwtUserPrincipal;
import com.example.SmartLearning.service.ProfileService;


@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProfileController {

    private final ProfileService profileService;
    private final UserRepository userRepository;

    private String getUserEmail(Authentication authentication) {
         JwtUserPrincipal jwtPrincipal = (JwtUserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(jwtPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + jwtPrincipal.getId()));
        return user.getEmail();
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getCurrentUserProfile(Authentication authentication) {
        String email = getUserEmail(authentication);
        ProfileResponse profile = profileService.getCurrentUserProfile(email);
    
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        String email = getUserEmail(authentication);
        ProfileResponse updatedProfile = profileService.updateProfile(email, request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/password")
        public ResponseEntity<?> changePassword(
                Authentication authentication,
                @RequestBody ChangePasswordRequest request) {

            String email = getUserEmail(authentication);
            profileService.changePassword(email, request);
            return ResponseEntity.ok("Password changed successfully");
        }
}