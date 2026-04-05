package com.example.SmartLearning.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.example.SmartLearning.Repository.CourseRepository;
import com.example.SmartLearning.Repository.ExerciseSubmissionRepository;

import java.util.*;
 

@Component
@RequiredArgsConstructor
public class ExerciseStrategy implements RecommendationStrategy {
 
    private final ExerciseSubmissionRepository submissionRepo;
    private final CourseRepository             courseRepo;
 
    @Override public String getName()   { return "SKILL_GAP"; }
    @Override public double getWeight() { return 0.30; }
 
    @Override
    public Map<Long, Integer> score(Long apprenantId, List<Long> candidateIds) {
        var result = new HashMap<Long, Integer>();
        if (candidateIds.isEmpty()) return result;
 
        // 1. Score moyen en % par cours (testsPassed/totalTests * 100)
        var rawScores   = submissionRepo.avgScorePercentPerCourse(apprenantId);
        var scorePerCourse = new HashMap<Long, Double>();
        for (var row : rawScores) {
            Long   courseId = ((Number) row[0]).longValue();
            Double avg      = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            scorePerCourse.put(courseId, avg);
        }
 
        // 2. Taux de réussite global (fallback si pas de données par cours)
        Double globalRate = submissionRepo.globalSuccessRate(apprenantId);
        double fallback   = (globalRate != null) ? globalRate : 50.0;
 
        for (Long courseId : candidateIds) {
            int score;
            if (scorePerCourse.containsKey(courseId)) {
                double avg = scorePerCourse.get(courseId);
                // Exercices échoués → cours très pertinent
                // avg 0%  → score 100
                // avg 50% → score 50
                // avg 100%→ score 0
                score = (int) Math.round(100 - avg);
            } else {
                // Cours jamais tenté : basé sur le taux global inversé
                // Apprenant fort (90%) → score 20 (peu besoin)
                // Apprenant faible (30%) → score 70 (besoin de renforcement)
                score = (int) Math.round(100 - fallback);
            }
            result.put(courseId, Math.max(0, Math.min(100, score)));
        }
 
        return result;
    }
}
