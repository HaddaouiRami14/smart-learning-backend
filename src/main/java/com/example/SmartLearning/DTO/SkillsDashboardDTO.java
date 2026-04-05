package com.example.SmartLearning.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillsDashboardDTO {
    private String learnerName;
    private Double overallProgressPercentage;
    private Integer totalEnrolledCourses;
    private Integer totalCompletedCourses;
    private List<SkillItemDTO> skills;
}