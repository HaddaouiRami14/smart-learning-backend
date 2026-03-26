package com.example.SmartLearning.Repository;

import com.example.SmartLearning.Enum.Category;
 

public interface CategorySkillProjection {
 
    Category getCategory();
 
    /** Average progression (0.0–100.0) across all enrolled courses in this category */
    Double getAvgProgression();
 
    /** Number of courses the learner is enrolled in for this category */
    Long getEnrolledCourses();
 
    /** Number of courses with progression >= 100 */
    Long getCompletedCourses();
}