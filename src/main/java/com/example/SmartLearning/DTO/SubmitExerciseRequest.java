package com.example.SmartLearning.DTO;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitExerciseRequest {
    
    @NotBlank(message = "Code is required")
    private String code;
}