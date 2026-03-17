package com.example.SmartLearning.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.example.SmartLearning.Enum.QuestionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse {
    private Long id;
    private String questionText;
    private QuestionType questionType;
    private Integer orderIndex;
    private Integer points;
    private List<QuestionOptionResponse> options;
    // NEW: For SHORT_ANSWER and EDITOR_ANSWER
    private String correctAnswer;
}