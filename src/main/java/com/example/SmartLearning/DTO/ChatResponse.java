package com.example.SmartLearning.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.util.List;
 
@Data @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatResponse {
    private String            reply;          // réponse textuelle du LLM
    private List<CourseCard>  recommendations; // cours extraits de la réponse
 
    @Data @Builder
    public static class CourseCard {
        private Long   courseId;
        private String title;
        private String description;
        private int    score;          // score de ton algorithme pondéré
        private String reason;         // explication du LLM pour ce cours
    }
}
