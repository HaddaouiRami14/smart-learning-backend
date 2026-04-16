package com.example.SmartLearning.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerStatsDTO {
    private int totalCourses;
    private int activeCourses;
    private int totalEnrollments;
    private int totalCompletions;
    private int freeCourses;
    private int paidCourses;
    private double avgProgressPercent;
}