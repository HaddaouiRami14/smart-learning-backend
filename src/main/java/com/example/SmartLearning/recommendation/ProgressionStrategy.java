package com.example.SmartLearning.recommendation;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.example.SmartLearning.Enum.Category;
import com.example.SmartLearning.Repository.CourseRepository;
import com.example.SmartLearning.Repository.InscriptionRepository;

import java.util.*;
 

@Component
@RequiredArgsConstructor
public class ProgressionStrategy implements RecommendationStrategy {
 
    private final InscriptionRepository inscriptionRepo;
    private final CourseRepository      courseRepo;
 
    @Override public String getName()   { return "PERFORMANCE_IMPROVEMENT"; }
    @Override public double getWeight() { return 0.40; }
 
    @Override
    public Map<Long, Integer> score(Long apprenantId, List<Long> candidateIds) {
        var result = new HashMap<Long, Integer>();
        if (candidateIds.isEmpty()) return result;
 
        // 1. Progression par catégorie via la méthode existante du repo
        var skillRows = inscriptionRepo.aggregateSkillsByCategory(apprenantId);
 
        // Map : Category → progression moyenne (0-100)
        var progressByCategory = new HashMap<Category, Integer>();
        for (var row : skillRows) {
            int avg = row.getAvgProgression() == null ? 0
                      : (int) Math.round(row.getAvgProgression());
            progressByCategory.put(row.getCategory(), avg);
        }
 
        // 2. Pour chaque cours candidat, récupère sa catégorie
        var courses = courseRepo.findAllById(candidateIds);
 
        for (var course : courses) {
            Category cat      = course.getCategory();
            int progInCat     = progressByCategory.getOrDefault(cat, 0);
 
            // Catégorie jamais tentée → score maximum (100)
            // Catégorie avec 0% → score 95
            // Catégorie avec 50% → score 50
            // Catégorie avec 100% → score 0
            int score = progressByCategory.containsKey(cat)
                ? Math.max(0, 100 - progInCat)
                : 100;
 
            result.put(course.getId(), score);
        }
 
        return result;
    }
 
    /** Retourne les catégories faibles (< 50%) pour le contexte UI */
    public List<String> getWeakCategoryLabels(Long apprenantId) {
        return inscriptionRepo.aggregateSkillsByCategory(apprenantId).stream()
                .filter(r -> r.getAvgProgression() != null && r.getAvgProgression() < 50)
                .sorted(Comparator.comparingDouble(r -> r.getAvgProgression()))
                .map(r -> r.getCategory().getLabel() + " · "
                        + (int) Math.round(r.getAvgProgression()) + "%")
                .limit(3)
                .toList();
    }
}