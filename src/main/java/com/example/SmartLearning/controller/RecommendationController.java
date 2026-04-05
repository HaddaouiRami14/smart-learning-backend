package com.example.SmartLearning.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.SmartLearning.DTO.RecommendationsResponse;
import com.example.SmartLearning.recommendation.RecommendationService;

import java.util.Set;
 
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('APPRENANT')")
public class RecommendationController {
 
    private final RecommendationService recommendationService;
 
    /**
     * GET /api/recommendations/{apprenantId}?limit=10&cursor=75
     * Retourne les cours recommandés triés par score décroissant.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<RecommendationsResponse> getRecommendations(
            @PathVariable Long   userId,
            @RequestParam(defaultValue = "10") int    limit,
            @RequestParam(required = false)    String cursor
    ) {
        return ResponseEntity.ok(
            recommendationService.recommend(userId, Math.min(limit, 20), cursor)
        );
    }
 
    /**
     * GET /api/recommendations/{apprenantId}/ids
     * Retourne uniquement les IDs des cours recommandés (score ≥ 60).
     * Utilisé par le frontend pour afficher le badge sur les cartes.
     *
     * Exemple réponse : [3, 7, 12, 18]
     */
    @GetMapping("/{userId}/ids")
    public ResponseEntity<Set<Long>> getRecommendedIds(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
            recommendationService.getRecommendedCourseIds(userId)
        );
    }
}
