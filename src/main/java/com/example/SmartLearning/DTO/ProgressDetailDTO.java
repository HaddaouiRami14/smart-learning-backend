package com.example.SmartLearning.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressDetailDTO {
    private Double progression;
    private List<String> completedItems;
    private Map<Long, ChapterProgressDTO> chapterProgress;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChapterProgressDTO {
        private Boolean quizPassed;
        private Boolean exercisePassed;
        private Boolean completed;
    }
}
