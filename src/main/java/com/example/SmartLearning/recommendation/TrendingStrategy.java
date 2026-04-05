package com.example.SmartLearning.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.example.SmartLearning.Repository.InscriptionRepository;

import java.time.LocalDate;
import java.util.*;
 

@Component
@RequiredArgsConstructor
public class TrendingStrategy implements RecommendationStrategy {
 
    private final InscriptionRepository inscriptionRepo;
 
    @Override public String getName()   { return "TRENDING"; }
    @Override public double getWeight() { return 0.20; }
 
    @Override
    public Map<Long, Integer> score(Long apprenantId, List<Long> candidateIds) {
        var result = new HashMap<Long, Integer>();
        if (candidateIds.isEmpty()) return result;
 
        LocalDate since = LocalDate.now().minusDays(7);
 
        var rows = inscriptionRepo.countRecentInscriptionsByCourseIds(candidateIds, since);
 
        if (rows.isEmpty()) {
            candidateIds.forEach(id -> result.put(id, 0));
            return result;
        }
 
        // Normalise : le cours le plus populaire = 100
        long max = ((Number) rows.get(0)[1]).longValue();
        if (max == 0) {
            candidateIds.forEach(id -> result.put(id, 0));
            return result;
        }
 
        for (var row : rows) {
            Long courseId = ((Number) row[0]).longValue();
            long count    = ((Number) row[1]).longValue();
            result.put(courseId, (int) Math.round(count * 100.0 / max));
        }
 
        // Cours sans inscription récente → score 0
        candidateIds.stream()
                .filter(id -> !result.containsKey(id))
                .forEach(id -> result.put(id, 0));
 
        return result;
    }
 
    /** Rang du cours parmi les tendances (pour le contexte UI) */
    public int getRank(Long courseId, List<Long> candidateIds) {
        LocalDate since = LocalDate.now().minusDays(7);
        var rows = inscriptionRepo.countRecentInscriptionsByCourseIds(candidateIds, since);
        for (int i = 0; i < rows.size(); i++) {
            if (((Number) rows.get(i)[0]).longValue() == courseId) return i + 1;
        }
        return 999;
    }
}