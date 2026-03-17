package com.example.SmartLearning.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseResultResponse {
    private Long exerciseId;
    private Integer testsPassed;
    private Integer totalTests;
    private Integer score;
    private Boolean passed;
    private Double executionTime;
    private List<TestResultResponse> testResults;
}