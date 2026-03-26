package com.example.SmartLearning.DTO;
import com.example.SmartLearning.Enum.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class SkillProgressDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillCategoryDTO {
        private Category category;
        private String   categoryLabel;       // "Data Science", "Programming", ...
        private int      progressPercentage;  // 0–100, average across enrolled courses
        private String   level;               // "Not Started" | "Beginner" | "Intermediate" | "Advanced" | "Expert"
        private int      enrolledCourses;     // courses the learner is enrolled in for this category
        private int      completedCourses;    // courses with progression == 100
    }

    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillsDashboardDTO {
        private Long                   apprenantId;
        private String                 learnerName;
        private int                    overallProgressPercentage;
        private int                    totalEnrolledCourses;
        private int                    totalCompletedCourses;
        private List<SkillCategoryDTO> skills;
    }
}
