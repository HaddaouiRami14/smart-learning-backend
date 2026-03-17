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
public class QuizResultResponse {
    private Long quizId;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer score; // Percentage
    private Boolean passed;
    private Integer passingScore;
    private List<QuestionResultResponse> questionResults;
}