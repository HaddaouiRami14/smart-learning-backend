package com.example.SmartLearning.DTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuizRequest {
    
    @NotBlank(message = "Quiz title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Passing score is required")
    @Min(value = 0, message = "Passing score must be at least 0")
    @Max(value = 100, message = "Passing score must be at most 100")
    private Integer passingScore;
    
    @NotNull(message = "Questions are required")
    private List<CreateQuestionRequest> questions;
}