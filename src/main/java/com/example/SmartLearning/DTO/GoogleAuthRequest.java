package com.example.SmartLearning.DTO;

import com.example.SmartLearning.Enum.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleAuthRequest {
    private String idToken;
    private Role role; // Le rôle souhaité (LEARNER ou TRAINER)
}