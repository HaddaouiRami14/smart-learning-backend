package com.example.SmartLearning.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.util.List;
 
@Data @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecommendedCourseDto {
 
    private Long         courseId;
    private String       title;
    private String       description;
    private int          score;           // 0-100 score fusionné
    private boolean      isRecommended;   // true si score ≥ 60 → affiche le badge
    private List<String> reasons;         // stratégies actives
 
    private Context context;
 
    @Data @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Context {
        private List<String> weakCategories;  // "Web · 24%", "Java · 10%"
        private List<String> newCategories;   // catégories jamais explorées
        private Integer      trendingRank;    // top N cette semaine
    }
}
