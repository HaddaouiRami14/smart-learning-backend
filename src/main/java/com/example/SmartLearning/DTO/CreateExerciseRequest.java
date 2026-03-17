package com.example.SmartLearning.DTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.example.SmartLearning.Enum.ProgrammingLanguage;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateExerciseRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Language is required")
    private ProgrammingLanguage language;
    
    @NotBlank(message = "Starter code is required")
    private String starterCode;
    
    private String hints;
    
    @NotNull(message = "Points are required")
    @Min(value = 1, message = "Points must be at least 1")
    private Integer points;
    
    @NotNull(message = "Order index is required")
    private Integer orderIndex;
    
    private Integer timeLimit = 10;
    
    @NotNull(message = "Test cases are required")
    private List<CreateTestCaseRequest> testCases;
}