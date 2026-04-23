package com.example.SmartLearning.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentProgressDTO {
    private Long   id;
    private String username;
    private String email;
    private String avatarUrl;
    private int    enrolledCourses;
    private double avgProgress;
    private String lastActive;
}
