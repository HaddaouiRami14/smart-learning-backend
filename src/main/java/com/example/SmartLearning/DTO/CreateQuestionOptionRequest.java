package com.example.SmartLearning.DTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionOptionRequest {
    
    @NotBlank(message = "Option text is required")
    private String optionText;
    
    @NotNull(message = "isCorrect is required")
    private Boolean isCorrect;
    
    @NotNull(message = "Order index is required")
    private Integer orderIndex;
}