package com.example.SmartLearning.controller;

import com.example.SmartLearning.DTO.AchievementDTO.*;
import com.example.SmartLearning.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learner/achievements")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AchievementController {

    private final BadgeService badgeService;

    // GET /api/learner/achievements/{userId} — stats + badges
    @GetMapping("/{userId}")
    public ResponseEntity<AchievementsPageDTO> getAchievements(@PathVariable Long userId) {
        // Check and award any new badges first
        badgeService.checkAndAwardBadges(userId);
        AchievementsPageDTO result = badgeService.getAchievements(userId);
        return ResponseEntity.ok(result);
    }

    // GET /api/learner/achievements/{userId}/leaderboard — leaderboard
    @GetMapping("/{userId}/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDTO>> getLeaderboard(@PathVariable Long userId) {
        List<LeaderboardEntryDTO> leaderboard = badgeService.getLeaderboard(userId);
        return ResponseEntity.ok(leaderboard);
    }
}