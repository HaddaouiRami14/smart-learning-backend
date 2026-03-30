package com.example.SmartLearning.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartLearning.DTO.ActivityDTO;
import com.example.SmartLearning.DTO.SkillProgressDTO.SkillCategoryDTO;
import com.example.SmartLearning.Enum.ActivityType;
import com.example.SmartLearning.Enum.Category; 
import com.example.SmartLearning.Repository.ActivityLogRepository;
import com.example.SmartLearning.Repository.ApprenantRepository;
import com.example.SmartLearning.model.ActivityLog;
import com.example.SmartLearning.model.Apprenant;
import com.example.SmartLearning.model.Course;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityService {

    private final ActivityLogRepository activityLogRepository;
    private final ApprenantRepository   apprenantRepository;
    private final SkillsProgressService skillsProgressService;


    @Transactional(readOnly = true)
    public List<ActivityDTO> getRecentActivities(Long userId) {
        // FIX 1: Changed from findByUser_Id(userId) to just findById(userId)
        // Because Apprenant IS a User, the Apprenant's ID is the User's ID.
        Apprenant apprenant = apprenantRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("Apprenant not found"));

        return activityLogRepository
            .findRecentByApprenant(apprenant.getId())
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }


    public void logCourseCompleted(Apprenant apprenant, Course course) {
        save(ActivityLog.builder()
            .apprenant(apprenant)
            .type(ActivityType.COURSE_COMPLETED)
            .title("Course Completed")
            .description("You completed: " + course.getTitle())
            .course(course)
            .build());
    }

    public void logAchievement(Apprenant apprenant, String badgeName) {
        save(ActivityLog.builder()
            .apprenant(apprenant)
            .type(ActivityType.ACHIEVEMENT)
            .title("Achievement Unlocked!")
            .description("Earned \"" + badgeName + "\" badge")
            .build());
    }

    public void logStreakMilestone(Apprenant apprenant, int days) {
        save(ActivityLog.builder()
            .apprenant(apprenant)
            .type(ActivityType.STREAK_MILESTONE)
            .title("Streak Milestone")
            .description("You're on a " + days + "-day learning streak!")
            .build());
    }


   public void logSkillLevelUp(Apprenant apprenant, String categoryLabel) {
    try {
        Category category = Arrays.stream(Category.values())
            .filter(c -> c.getLabel().equalsIgnoreCase(categoryLabel))
            .findFirst()
            .orElse(null);

        if (category != null) {
            // FIX 2: Changed apprenant.getUser().getId() to just apprenant.getId()
            SkillCategoryDTO realStats = skillsProgressService.getCategoryProgress(
                apprenant.getId(), 
                category
            );

            save(ActivityLog.builder()
                .apprenant(apprenant)
                .type(ActivityType.SKILL_LEVELUP)
                .title("Skill Level Up")
                .description(categoryLabel + " reached " + realStats.getLevel() + " level")
                .build());
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    private void save(ActivityLog log) {
        activityLogRepository.save(log);
    }

    private ActivityDTO toDTO(ActivityLog log) {
        return ActivityDTO.builder()
            .id(log.getId())
            .type(log.getType())
            .title(log.getTitle())
            .description(log.getDescription())
            .createdAt(log.getCreatedAt())
            .timeAgo(computeTimeAgo(log.getCreatedAt()))
            .build();
    }

    private String computeTimeAgo(LocalDateTime dateTime) {
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        if (duration.toMinutes() < 60)
            return duration.toMinutes() + " minutes ago";
        if (duration.toHours() < 24)
            return duration.toHours() + " hours ago";
        if (duration.toDays() < 7)
            return duration.toDays() + " days ago";
        return dateTime.toLocalDate().toString();
    }
}