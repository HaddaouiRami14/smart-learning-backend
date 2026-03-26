package com.example.SmartLearning.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnrolledCourseDTO {
    private Long    courseId;
    private String  title;
    private String  description;
    private String  category;
    private String  level;
    private Double  price;
    private String courseImageUrl;
    private int     progression;      // 0–100
}
