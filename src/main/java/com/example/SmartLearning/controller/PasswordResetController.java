package com.example.SmartLearning.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SmartLearning.DTO.ForgotPasswordRequest;
import com.example.SmartLearning.DTO.PasswordResetResponse;
import com.example.SmartLearning.DTO.ResetPasswordRequest;
import com.example.SmartLearning.service.PasswordResetService;



@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
@CrossOrigin(origins = "")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot")
    public ResponseEntity<PasswordResetResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        PasswordResetResponse response = passwordResetService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    public ResponseEntity<PasswordResetResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        PasswordResetResponse response = passwordResetService.resetPassword(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}