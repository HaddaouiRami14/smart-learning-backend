package com.example.SmartLearning.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartLearning.DTO.RecommendationsResponse;
import com.example.SmartLearning.DTO.RecommendedCourseDto;
import com.example.SmartLearning.Enum.Category;
import com.example.SmartLearning.Repository.CourseRepository;
import com.example.SmartLearning.Repository.ExerciseSubmissionRepository;
import com.example.SmartLearning.Repository.InscriptionRepository;

import java.time.LocalDate;
import java.util.*;
 
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final CourseRepository             courseRepo;
    private final InscriptionRepository       inscriptionRepo;
    private final ExerciseSubmissionRepository submissionRepo; 

    private static final int BADGE_THRESHOLD = 40; // score ≥ 60 → badge "Recommandé" (Note: votre code disait >= 60 dans le commentaire mais le seuil est à 40 ?)

    @Cacheable(value = "recommendations", key = "#apprenantId + '-' + #limit + '-' + #cursor")
    @Transactional(readOnly = true)
    public RecommendationsResponse recommend(Long apprenantId, int limit, String cursor) {

        // 1. Cours candidats : tous sauf ceux déjà inscrits
        var enrolledIds = inscriptionRepo.findCourseIdsByApprenantId(apprenantId);
        var candidates  = enrolledIds.isEmpty()
            ? courseRepo.findAll()
            : courseRepo.findCandidateCourses(enrolledIds);

        if (candidates.isEmpty())
            return RecommendationsResponse.builder()
                    .items(List.of()).nextCursor(null).total(0).build();

        var candidateIds = candidates.stream().map(c -> c.getId()).toList();

        // 2. Calcul direct des scores (ancien Pattern Strategy)
        
        // Stratégie 1: PERFORMANCE_IMPROVEMENT (Poids 0.40)
        Map<Long, Integer> progressionScores = calculateProgressionScores(apprenantId, candidateIds);
        
        // Stratégie 2: SKILL_GAP (Poids 0.30)
        Map<Long, Integer> exerciseScores = calculateExerciseScores(apprenantId, candidateIds);

        // Stratégie 3: TRENDING (Poids 0.20)
        Map<Long, Integer> trendingScores = calculateTrendingScores(candidateIds);
        
        // Stratégie 4: CATEGORY_DISCOVERY (Poids 0.10)
        Map<Long, Integer> categoryGapScores = calculateCategoryGapScores(apprenantId, candidateIds);

        // 3. Fusion pondérée
        record Ranked(Long id, String title, String desc,
                      int score, List<String> reasons) {}

        var ranked = candidates.stream().map(course -> {
            double weighted = 0;
            var    reasons  = new ArrayList<String>();

            // Application manuelle des poids
            int rawProg = progressionScores.getOrDefault(course.getId(), 0);
            weighted += rawProg * 0.40;
            if (rawProg >= 50) reasons.add("PERFORMANCE_IMPROVEMENT");

            int rawEx = exerciseScores.getOrDefault(course.getId(), 0);
            weighted += rawEx * 0.30;
            if (rawEx >= 50) reasons.add("SKILL_GAP");

            int rawTrend = trendingScores.getOrDefault(course.getId(), 0);
            weighted += rawTrend * 0.20;
            if (rawTrend >= 50) reasons.add("TRENDING");

            int rawCat = categoryGapScores.getOrDefault(course.getId(), 0);
            weighted += rawCat * 0.10;
            if (rawCat >= 50) reasons.add("CATEGORY_DISCOVERY");

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
        var weakCategories  = getWeakCategoryLabels(apprenantId);
        var untouchedCats   = getUntouchedCategoryLabels(apprenantId);

        var items = page.stream().map(r -> {
            Integer trendRank = r.reasons().contains("TRENDING")
                    ? getTrendingRank(r.id(), candidateIds) : null;

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

    // ─── Méthodes utilitaires publiques ────────────────────────────────

    @Transactional(readOnly = true)
    public boolean isCourseRecommended(Long apprenantId, Long courseId) {
        var result = recommend(apprenantId, 20, null);
        return result.getItems().stream()
                .anyMatch(c -> c.getCourseId().equals(courseId)
                            && c.getScore() >= BADGE_THRESHOLD);
    }

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

    @CacheEvict(value = "recommendations", allEntries = true)
    public void evictCache(Long apprenantId) {}

    // ─── Implémentation directe des anciennes Stratégies (Méthodes privées) ─────

    /** Logique de ProgressionStrategy */
    private Map<Long, Integer> calculateProgressionScores(Long apprenantId, List<Long> candidateIds) {
        var result = new HashMap<Long, Integer>();
        if (candidateIds.isEmpty()) return result;

        var skillRows = inscriptionRepo.aggregateSkillsByCategory(apprenantId);
        var progressByCategory = new HashMap<Category, Integer>();
        for (var row : skillRows) {
            int avg = row.getAvgProgression() == null ? 0
                      : (int) Math.round(row.getAvgProgression());
            progressByCategory.put(row.getCategory(), avg);
        }

        var courses = courseRepo.findAllById(candidateIds);
        for (var course : courses) {
            Category cat      = course.getCategory();
            int progInCat     = progressByCategory.getOrDefault(cat, 0);
            int score = progressByCategory.containsKey(cat)
                ? Math.max(0, 100 - progInCat)
                : 100;
            result.put(course.getId(), score);
        }
        return result;
    }

    /** Logique de ExerciseStrategy */
    private Map<Long, Integer> calculateExerciseScores(Long apprenantId, List<Long> candidateIds) {
        var result = new HashMap<Long, Integer>();
        if (candidateIds.isEmpty()) return result;

        // 1. Score moyen en % par cours
        var rawScores   = submissionRepo.avgScorePercentPerCourse(apprenantId);
        var scorePerCourse = new HashMap<Long, Double>();
        for (var row : rawScores) {
            Long   courseId = ((Number) row[0]).longValue();
            Double avg      = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            scorePerCourse.put(courseId, avg);
        }

        // 2. Taux de réussite global
        Double globalRate = submissionRepo.globalSuccessRate(apprenantId);
        double fallback   = (globalRate != null) ? globalRate : 50.0;

        for (Long courseId : candidateIds) {
            int score;
            if (scorePerCourse.containsKey(courseId)) {
                double avg = scorePerCourse.get(courseId);
                score = (int) Math.round(100 - avg); // Inversé : plus on échoue, plus c'est recommandé
            } else {
                score = (int) Math.round(100 - fallback);
            }
            result.put(courseId, Math.max(0, Math.min(100, score)));
        }
        return result;
    }

    /** Logique de TrendingStrategy */
    private Map<Long, Integer> calculateTrendingScores(List<Long> candidateIds) {
        var result = new HashMap<Long, Integer>();
        if (candidateIds.isEmpty()) return result;

        LocalDate since = LocalDate.now().minusDays(7);
        var rows = inscriptionRepo.countRecentInscriptionsByCourseIds(candidateIds, since);

        if (rows.isEmpty()) {
            candidateIds.forEach(id -> result.put(id, 0));
            return result;
        }

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

        candidateIds.stream()
                .filter(id -> !result.containsKey(id))
                .forEach(id -> result.put(id, 0));

        return result;
    }

    /** Logique de CategoryGapStrategy */
    private Map<Long, Integer> calculateCategoryGapScores(Long apprenantId, List<Long> candidateIds) {
        var result = new HashMap<Long, Integer>();
        if (candidateIds.isEmpty()) return result;

        var triedCategories = new HashSet<Category>();
        inscriptionRepo.aggregateSkillsByCategory(apprenantId)
                .forEach(row -> triedCategories.add(row.getCategory()));

        var courses = courseRepo.findAllById(candidateIds);
        for (var course : courses) {
            result.put(course.getId(), triedCategories.contains(course.getCategory()) ? 0 : 100);
        }
        return result;
    }

    // ─── Helpers pour le contexte UI (provenant des anciennes stratégies) ─────

    private List<String> getWeakCategoryLabels(Long apprenantId) {
        return inscriptionRepo.aggregateSkillsByCategory(apprenantId).stream()
                .filter(r -> r.getAvgProgression() != null && r.getAvgProgression() < 50)
                .sorted(Comparator.comparingDouble(r -> r.getAvgProgression()))
                .map(r -> r.getCategory().getLabel() + " · "
                        + (int) Math.round(r.getAvgProgression()) + "%")
                .limit(3)
                .toList();
    }

    private List<String> getUntouchedCategoryLabels(Long apprenantId) {
        var tried = new HashSet<Category>();
        inscriptionRepo.aggregateSkillsByCategory(apprenantId)
                .forEach(r -> tried.add(r.getCategory()));

        return Arrays.stream(Category.values())
                .filter(c -> !tried.contains(c))
                .map(Category::getLabel)
                .toList();
    }

    private int getTrendingRank(Long courseId, List<Long> candidateIds) {
        LocalDate since = LocalDate.now().minusDays(7);
        var rows = inscriptionRepo.countRecentInscriptionsByCourseIds(candidateIds, since);
        for (int i = 0; i < rows.size(); i++) {
            if (((Number) rows.get(i)[0]).longValue() == courseId) return i + 1;
        }
        return 999;
    }
}

