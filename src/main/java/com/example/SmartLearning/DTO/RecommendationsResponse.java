package com.example.SmartLearning.DTO;

import lombok.Builder;
import lombok.Data;
import java.util.List;
 
@Data @Builder
public class RecommendationsResponse {
    private List<RecommendedCourseDto> items;
    private String                     nextCursor;
    private long                       total;
}
