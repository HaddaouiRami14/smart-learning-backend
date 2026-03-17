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
public class QuizResponse {
    private Long id;
    private String title;
    private String description;
    private Integer passingScore;
    private Long chapterId;
    private List<QuestionResponse> questions;
}
