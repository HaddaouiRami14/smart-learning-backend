package com.example.SmartLearning.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitQuizRequest {
    // For MULTIPLE_CHOICE and TRUE_FALSE: questionId -> selectedOptionId
    private Map<Long, Long> answers;
    
    // NEW: For SHORT_ANSWER and EDITOR_ANSWER: questionId -> text answer
    private Map<Long, String> textAnswers;

    private Map<Long, List<Long>> multiAnswers;
}