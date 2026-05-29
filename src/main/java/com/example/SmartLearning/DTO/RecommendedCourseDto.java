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
    private int          score;           
    private boolean      isRecommended;   
    private List<String> reasons;         
 
    private Context context;
 
    @Data @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Context {
        private List<String> weakCategories;  
        private List<String> newCategories;   
        private Integer      trendingRank;    
    }
}
