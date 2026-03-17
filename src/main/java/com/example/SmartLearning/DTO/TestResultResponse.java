package com.example.SmartLearning.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResultResponse {
    private Integer testNumber;
    private Boolean passed;
    private String input;
    private String expectedOutput;
    private String actualOutput;
    private Boolean isHidden;
    private String status; // "Accepted", "Wrong Answer", "Time Limit Exceeded", "Runtime Error"
    private String errorMessage;
}