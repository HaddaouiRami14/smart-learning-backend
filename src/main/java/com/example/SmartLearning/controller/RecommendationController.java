package com.example.SmartLearning.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.SmartLearning.DTO.RecommendationsResponse;
import com.example.SmartLearning.recommendation.RecommendationService;
import com.example.SmartLearning.security.JwtUserPrincipal;
import org.springframework.security.core.Authentication;
import java.util.Set; 
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('APPRENANT')")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<RecommendationsResponse> getRecommendations(
            @RequestParam(defaultValue = "10") int    limit,
            @RequestParam(required = false)    String cursor
    ) {
        Long apprenantId = extractApprenantId();

        var stored = recommendationService.getStoredRecommendations(apprenantId);

        if (stored.getItems().isEmpty()) {
            recommendationService.refreshRecommendations(apprenantId);
            stored = recommendationService.getStoredRecommendations(apprenantId);
        }

        return ResponseEntity.ok(stored);
    }

    @GetMapping("/ids")
    public ResponseEntity<Set<Long>> getRecommendedIds() {
        Long apprenantId = extractApprenantId();

        var stored = recommendationService.getStoredRecommendations(apprenantId);
        if (stored.getItems().isEmpty()) {
            recommendationService.refreshRecommendations(apprenantId);
        }

        return ResponseEntity.ok(
            recommendationService.getRecommendedCourseIds(apprenantId)
        );
    }


    private Long extractApprenantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof JwtUserPrincipal principal) {
            return principal.getId();
        }

        throw new ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Utilisateur non authentifié"
        );
    }
}
