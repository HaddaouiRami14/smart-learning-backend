package com.example.SmartLearning.DTO;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class AchievementDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BadgeDTO {
        private Long id;
        private String code;
        private String name;
        private String description;
        private String icon;
        private String color;
        private boolean earned;
        private LocalDateTime earnedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatsDTO {
        private int coursesEnrolled;
        private int coursesCompleted;
        private int quizzesPassed;
        private int exercisesSolved;
        private int totalBadges;
        private int earnedBadges;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AchievementsPageDTO {
        private StatsDTO stats;
        private List<BadgeDTO> badges;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaderboardEntryDTO {
        private int rank;
        private Long userId;
        private String username;
        private String picture;
        private int coursesCompleted;
        private int badgesEarned;
        private int score; // coursesCompleted * 100 + badgesEarned * 10
        private Boolean currentUser; // ✅ FIXED: boolean isXxx gets serialized as "xxx" by Jackson, use Boolean instead
    }
}