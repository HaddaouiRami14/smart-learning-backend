package com.example.SmartLearning.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.example.SmartLearning.Enum.Category;
import com.example.SmartLearning.Repository.CourseRepository;
import com.example.SmartLearning.Repository.InscriptionRepository;

import java.util.*;
 

@Component
@RequiredArgsConstructor
public class CategoryGapStrategy implements RecommendationStrategy {
 
    private final InscriptionRepository inscriptionRepo;
    private final CourseRepository      courseRepo;
 
    @Override public String getName()   { return "CATEGORY_DISCOVERY"; }
    @Override public double getWeight() { return 0.10; }
 
    @Override
    public Map<Long, Integer> score(Long apprenantId, List<Long> candidateIds) {
        var result = new HashMap<Long, Integer>();
        if (candidateIds.isEmpty()) return result;
 
        // Catégories déjà tentées par l'apprenant
        var triedCategories = new HashSet<Category>();
        inscriptionRepo.aggregateSkillsByCategory(apprenantId)
                .forEach(row -> triedCategories.add(row.getCategory()));
 
        // Pour chaque cours candidat
        var courses = courseRepo.findAllById(candidateIds);
        for (var course : courses) {
            // Catégorie jamais tentée → score 100 (découverte)
            // Catégorie déjà tentée  → score 0  (déjà couvert)
            result.put(course.getId(), triedCategories.contains(course.getCategory()) ? 0 : 100);
        }
 
        return result;
    }
 
    /** Liste des catégories non encore explorées (pour le contexte UI) */
    public List<String> getUntouchedCategoryLabels(Long apprenantId) {
        var tried = new HashSet<Category>();
        inscriptionRepo.aggregateSkillsByCategory(apprenantId)
                .forEach(r -> tried.add(r.getCategory()));
 
        return Arrays.stream(Category.values())
                .filter(c -> !tried.contains(c))
                .map(Category::getLabel)
                .toList();
    }
}
