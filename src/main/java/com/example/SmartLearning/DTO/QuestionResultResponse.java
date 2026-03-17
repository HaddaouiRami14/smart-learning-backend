package com.example.SmartLearning.DTO;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResultResponse {
    private Long questionId;
    private String questionText;
    // For MULTIPLE_CHOICE and TRUE_FALSE
    private Long selectedOptionId;
    private List<Long> selectedOptionIds;
    private Long correctOptionId;
    private List<Long> correctOptionIds; 
    // NEW: For SHORT_ANSWER and EDITOR_ANSWER
    private String textAnswer;
    private String correctAnswer;
    // Result
    private Boolean isCorrect;
}