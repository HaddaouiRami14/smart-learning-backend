package com.example.SmartLearning.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartLearning.DTO.RecommendationsResponse;
import com.example.SmartLearning.DTO.RecommendedCourseDto;
import com.example.SmartLearning.Repository.CourseRepository;
import com.example.SmartLearning.Repository.InscriptionRepository;

import java.util.*;
 
@Service
@RequiredArgsConstructor
public class RecommendationService {
 
    private final CourseRepository      courseRepo;
    private final InscriptionRepository inscriptionRepo;
 
    // Les 4 stratégies
    private final ProgressionStrategy   progressionStrategy;
    private final ExerciseStrategy      exerciseStrategy;
    private final TrendingStrategy      trendingStrategy;
    private final CategoryGapStrategy   categoryGapStrategy;
 
    private static final int BADGE_THRESHOLD = 40; // score ≥ 60 → badge "Recommandé"
 
    
 
    @Cacheable(value = "recommendations", key = "#apprenantId + '-' + #limit + '-' + #cursor")
    @Transactional(readOnly = true)
    public RecommendationsResponse recommend(Long apprenantId, int limit, String cursor) {
 
        // 1. Cours candidats : tous sauf ceux déjà inscrits
        var enrolledIds  = inscriptionRepo.findCourseIdsByApprenantId(apprenantId);
        var candidates   = enrolledIds.isEmpty()
            ? courseRepo.findAll()
            : courseRepo.findCandidateCourses(enrolledIds);
 
        if (candidates.isEmpty())
            return RecommendationsResponse.builder()
                    .items(List.of()).nextCursor(null).total(0).build();
 
        var candidateIds = candidates.stream().map(c -> c.getId()).toList();
 
        // 2. Calcul des 4 stratégies
        List<RecommendationStrategy> strategies = List.of(
                progressionStrategy,
                exerciseStrategy,
                trendingStrategy,
                categoryGapStrategy
        );
 
        var allScores = new HashMap<String, Map<Long, Integer>>();
        for (var s : strategies)
            allScores.put(s.getName(), s.score(apprenantId, candidateIds));
 
        // 3. Fusion pondérée
        //    score = S1×0.40 + S2×0.30 + S3×0.20 + S4×0.10
        record Ranked(Long id, String title, String desc,
                      int score, List<String> reasons) {}
 
        var ranked = candidates.stream().map(course -> {
            double weighted = 0;
            var    reasons  = new ArrayList<String>();
 
            for (var s : strategies) {
                int raw  = allScores.get(s.getName()).getOrDefault(course.getId(), 0);
                weighted += raw * s.getWeight();
                if (raw >= 50) reasons.add(s.getName()); // raison active si contribution notable
            }
 
            return new Ranked(
                    course.getId(),
                    course.getTitle(),
                    course.getDescription(),
                    (int) Math.round(weighted),
                    reasons
            );
        })
        .filter(r -> r.score() > 0)
        .sorted(Comparator.comparingInt(Ranked::score).reversed())
        .toList();
 
        // 4. Pagination par curseur (score du dernier item)
        var paginated = (cursor != null)
            ? ranked.stream().filter(r -> r.score() < Integer.parseInt(cursor)).toList()
            : ranked;
 
        var page = paginated.stream().limit(limit).toList();
 
        // 5. Contexte UI enrichi
        var weakCategories  = progressionStrategy.getWeakCategoryLabels(apprenantId);
        var untouchedCats   = categoryGapStrategy.getUntouchedCategoryLabels(apprenantId);
 
        var items = page.stream().map(r -> {
            Integer trendRank = r.reasons().contains("TRENDING")
                    ? trendingStrategy.getRank(r.id(), candidateIds) : null;
 
            return RecommendedCourseDto.builder()
                    .courseId(r.id())
                    .title(r.title())
                    .description(r.desc())
                    .score(r.score())
                    .isRecommended(r.score() >= BADGE_THRESHOLD)
                    .reasons(r.reasons())
                    .context(RecommendedCourseDto.Context.builder()
                            .weakCategories(weakCategories.isEmpty() ? null : weakCategories)
                            .newCategories(untouchedCats.isEmpty() ? null : untouchedCats)
                            .trendingRank(trendRank)
                            .build())
                    .build();
        }).toList();
 
        var last       = page.isEmpty() ? null : page.get(page.size() - 1);
        var nextCursor = (page.size() == limit && last != null)
                ? String.valueOf(last.score()) : null;
 
        return RecommendationsResponse.builder()
                .items(items)
                .nextCursor(nextCursor)
                .total(paginated.size())
                .build();
    }
 
    // ─── Vérifie si UN cours est recommandé (pour le badge sur la carte) ─────
 
    @Transactional(readOnly = true)
    public boolean isCourseRecommended(Long apprenantId, Long courseId) {
        var result = recommend(apprenantId, 20, null);
        return result.getItems().stream()
                .anyMatch(c -> c.getCourseId().equals(courseId)
                            && c.getScore() >= BADGE_THRESHOLD);
    }
 
    // ─── IDs de tous les cours recommandés (pour affichage en masse) ──────────
 
    @Transactional(readOnly = true)
    public Set<Long> getRecommendedCourseIds(Long apprenantId) {
        var result = recommend(apprenantId, 20, null);
        var ids    = new HashSet<Long>();
        for (var item : result.getItems()) {
            if (item.getScore() >= BADGE_THRESHOLD)
                ids.add(item.getCourseId());
        }
        return ids;
    }
 
    // ─── Invalide le cache après tout changement de profil ───────────────────
 
    @CacheEvict(value = "recommendations", allEntries = true)
    public void evictCache(Long apprenantId) {}
}
