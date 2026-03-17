package com.example.SmartLearning.DTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.example.SmartLearning.Enum.QuestionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuestionRequest {
    
    @NotBlank(message = "Question text is required")
    private String questionText;
    
    @NotNull(message = "Question type is required")
    private QuestionType questionType;
    
    @NotNull(message = "Order index is required")
    private Integer orderIndex;
    
    private Integer points = 1;
    
    // Options for MULTIPLE_CHOICE and TRUE_FALSE (can be null/empty for SHORT_ANSWER/EDITOR_ANSWER)
    private List<CreateQuestionOptionRequest> options;
    
    // NEW: Correct answer for SHORT_ANSWER and EDITOR_ANSWER
    private String correctAnswer;
}