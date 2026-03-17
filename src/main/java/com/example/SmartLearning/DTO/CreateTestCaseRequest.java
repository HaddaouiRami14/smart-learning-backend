package com.example.SmartLearning.DTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTestCaseRequest {
    
    @NotBlank(message = "Input is required")
    private String input;
    
    @NotBlank(message = "Expected output is required")
    private String expectedOutput;
    
    @NotNull(message = "isHidden flag is required")
    private Boolean isHidden;
    
    @NotNull(message = "Order index is required")
    private Integer orderIndex;
}