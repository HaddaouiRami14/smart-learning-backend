package com.example.SmartLearning.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionOptionResponse {
    private Long id;
    private String optionText;
    private Boolean isCorrect; // Only sent to trainers, not learners
    private Integer orderIndex;
}