package com.example.SmartLearning.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillItemDTO {
    private String categoryLabel;
    private String level; // Ou "Integer" selon votre modèle
    private Double progressPercentage;
}