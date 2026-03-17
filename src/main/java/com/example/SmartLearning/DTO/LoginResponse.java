package com.example.SmartLearning.DTO;

import com.example.SmartLearning.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    private String token;
    private User user;
    private String message;
    private boolean newUser;

   // Constructeur pour le succès
    public LoginResponse(String token, User user, String message) {
        this.token = token;
        this.user = user;
        this.message = message;
    }
     // Constructeur pour les erreurs
    public LoginResponse(String message) {
        this.message = message;
    }
}
