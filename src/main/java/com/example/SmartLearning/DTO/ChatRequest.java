package com.example.SmartLearning.DTO;

import lombok.Data;
 
@Data
public class ChatRequest {
    private String message;           // question de l'apprenant
    private Long   apprenantId;
}