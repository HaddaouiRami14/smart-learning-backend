package com.example.SmartLearning.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.example.SmartLearning.Enum.ProgrammingLanguage;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseResponse {
    private Long id;
    private String title;
    private String description;
    private ProgrammingLanguage language;
    private String starterCode;
    private String hints;
    private Integer points;
    private Integer orderIndex;
    private Integer timeLimit;
    private Long chapterId;
    private List<TestCaseResponse> testCases;
    private Boolean isCompleted; // for learners
    private Integer bestScore; // for learners
}