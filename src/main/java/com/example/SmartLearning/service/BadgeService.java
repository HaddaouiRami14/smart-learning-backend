package com.example.SmartLearning.service;

import com.example.SmartLearning.DTO.AchievementDTO;
import com.example.SmartLearning.DTO.AchievementDTO.*;
import com.example.SmartLearning.Repository.*;
import com.example.SmartLearning.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final ApprenantBadgeRepository apprenantBadgeRepository;
    private final InscriptionRepository inscriptionRepository;
    private final ApprenantRepository apprenantRepository;
    private final ExerciseRepository exerciseRepository;

    // ─── Initialize all badges on startup ─────────────────────────────────────
    @PostConstruct
    @Transactional
    public void initBadges() {
        createIfNotExists("FIRST_STEP",     "First Step",        "Enroll in your first course",             "🎯", "blue");
        createIfNotExists("COURSE_COMPLETE", "Course Completed",  "Complete your first course",              "🎓", "gold");
        createIfNotExists("SCHOLAR",         "Scholar",           "Complete 5 courses",                      "📚", "purple");
        createIfNotExists("QUIZ_MASTER",     "Quiz Master",       "Pass a quiz with 100%",                   "🧠", "green");
        createIfNotExists("BUG_CRUSHER",     "Bug Crusher",       "Pass 10 exercises",                       "💻", "orange");
        createIfNotExists("EXPLORER",        "Explorer",          "Enroll in 3 different categories",        "🌍", "teal");
        createIfNotExists("POLYGLOT",        "Polyglot",          "Complete exercises in 2+ languages",      "🔤", "pink");
        createIfNotExists("PERFECTIONIST",   "Perfectionist",     "Complete a course with 100% progression", "⭐", "gold");
    }

    private void createIfNotExists(String code, String name, String description, String icon, String color) {
        if (badgeRepository.findByCode(code).isEmpty()) {
            badgeRepository.save(Badge.builder()
                .code(code).name(name).description(description).icon(icon).color(color)
                .build());
        }
    }

    // ─── Check and award all general badges ───────────────────────────────────
    @Transactional
    public void checkAndAwardBadges(Long apprenantId) {
        Apprenant apprenant = findApprenant(apprenantId);
        List<Inscription> inscriptions = inscriptionRepository.findByApprenantId(apprenantId);

        int enrolled  = inscriptions.size();
        int completed = (int) inscriptions.stream().filter(i -> i.getProgression() >= 100).count();
        long categories = inscriptions.stream().map(i -> i.getCourse().getCategory()).distinct().count();

        // FIRST_STEP
        if (enrolled >= 1) award(apprenant, "FIRST_STEP");

        // COURSE_COMPLETE
        if (completed >= 1) award(apprenant, "COURSE_COMPLETE");

        // SCHOLAR
        if (completed >= 5) award(apprenant, "SCHOLAR");

        // PERFECTIONIST
        if (inscriptions.stream().anyMatch(i -> i.getProgression() >= 100))
            award(apprenant, "PERFECTIONIST");

        // EXPLORER
        if (categories >= 3) award(apprenant, "EXPLORER");

        // BUG_CRUSHER — ✅ FIXED: count from completedItems directly
        long totalExercisesPassed = countExercisesPassed(inscriptions);
        if (totalExercisesPassed >= 10) award(apprenant, "BUG_CRUSHER");

        // POLYGLOT — ✅ NEW: check languages of completed exercises
        checkPolyglot(apprenant, inscriptions);
    }

    // ─── Called when a quiz is submitted ──────────────────────────────────────
    @Transactional
    public void onQuizPassed(Long apprenantId, int score) {
        Apprenant apprenant = findApprenant(apprenantId);
        if (score == 100) award(apprenant, "QUIZ_MASTER");
    }

    // ─── Called when an exercise is passed ────────────────────────────────────
    @Transactional
    public void onExercisePassed(Long apprenantId, Long exerciseId) {
        Apprenant apprenant = findApprenant(apprenantId);
        List<Inscription> inscriptions = inscriptionRepository.findByApprenantId(apprenantId);

        // ✅ FIXED: count exercises AFTER the new one is already saved
        long totalExercisesPassed = countExercisesPassed(inscriptions);
        if (totalExercisesPassed >= 10) award(apprenant, "BUG_CRUSHER");

        // ✅ POLYGLOT: check languages
        checkPolyglot(apprenant, inscriptions);
    }

    // ─── POLYGLOT logic ───────────────────────────────────────────────────────
    private void checkPolyglot(Apprenant apprenant, List<Inscription> inscriptions) {
        // Get all chapter IDs where exercise was passed
        Set<Long> completedExerciseChapterIds = new HashSet<>();
        for (Inscription inscription : inscriptions) {
            if (inscription.getCompletedItems() == null || inscription.getCompletedItems().isEmpty()) continue;
            Arrays.stream(inscription.getCompletedItems().split(","))
                .map(String::trim)
                .filter(item -> item.endsWith(":E"))
                .forEach(item -> {
                    try {
                        Long chapterId = Long.parseLong(item.replace(":E", ""));
                        completedExerciseChapterIds.add(chapterId);
                    } catch (NumberFormatException ignored) {}
                });
        }

        if (completedExerciseChapterIds.isEmpty()) return;

        // Get distinct languages from exercises in those chapters
        Set<String> languages = new HashSet<>();
        for (Long chapterId : completedExerciseChapterIds) {
            List<Exercise> exercises = exerciseRepository.findByChapterId(chapterId);
            exercises.forEach(ex -> {
                if (ex.getLanguage() != null) {
                    languages.add(ex.getLanguage().name());
                }
            });
        }

        // POLYGLOT: 2+ different languages
        if (languages.size() >= 2) award(apprenant, "POLYGLOT");
    }

    // ─── Count total exercises passed from completedItems ─────────────────────
    private long countExercisesPassed(List<Inscription> inscriptions) {
        return inscriptions.stream()
            .mapToLong(i -> {
                if (i.getCompletedItems() == null || i.getCompletedItems().isEmpty()) return 0;
                return Arrays.stream(i.getCompletedItems().split(","))
                    .map(String::trim)
                    .filter(item -> item.endsWith(":E"))
                    .count();
            }).sum();
    }

    // ─── Award a badge if not already earned ──────────────────────────────────
    private void award(Apprenant apprenant, String badgeCode) {
        if (apprenantBadgeRepository.existsByApprenantIdAndBadgeCode(apprenant.getId(), badgeCode)) return;
        Badge badge = badgeRepository.findByCode(badgeCode).orElse(null);
        if (badge == null) return;
        apprenantBadgeRepository.save(ApprenantBadge.builder()
            .apprenant(apprenant)
            .badge(badge)
            .earnedAt(LocalDateTime.now())
            .build());
    }

    private Apprenant findApprenant(Long apprenantId) {
        return apprenantRepository.findById(apprenantId)
            .orElseThrow(() -> new NoSuchElementException("Apprenant not found: " + apprenantId));
    }

    // ─── Get achievements page data ────────────────────────────────────────────
    @Transactional(readOnly = true)
    public AchievementsPageDTO getAchievements(Long apprenantId) {
        List<Inscription> inscriptions = inscriptionRepository.findByApprenantId(apprenantId);
        List<ApprenantBadge> earnedBadges = apprenantBadgeRepository.findByApprenantId(apprenantId);
        List<Badge> allBadges = badgeRepository.findAll();

        Map<String, LocalDateTime> earnedMap = earnedBadges.stream()
            .collect(Collectors.toMap(ab -> ab.getBadge().getCode(), ApprenantBadge::getEarnedAt));

        int enrolled  = inscriptions.size();
        int completed = (int) inscriptions.stream().filter(i -> i.getProgression() >= 100).count();

        long quizzesPassed = inscriptions.stream()
            .mapToLong(i -> {
                if (i.getCompletedItems() == null || i.getCompletedItems().isEmpty()) return 0;
                return Arrays.stream(i.getCompletedItems().split(","))
                    .filter(item -> item.trim().endsWith(":Q"))
                    .count();
            }).sum();

        long exercisesSolved = countExercisesPassed(inscriptions);

        StatsDTO stats = StatsDTO.builder()
            .coursesEnrolled(enrolled)
            .coursesCompleted(completed)
            .quizzesPassed((int) quizzesPassed)
            .exercisesSolved((int) exercisesSolved)
            .totalBadges(allBadges.size())
            .earnedBadges(earnedBadges.size())
            .build();

        List<BadgeDTO> badges = allBadges.stream()
            .map(b -> BadgeDTO.builder()
                .id(b.getId())
                .code(b.getCode())
                .name(b.getName())
                .description(b.getDescription())
                .icon(b.getIcon())
                .color(b.getColor())
                .earned(earnedMap.containsKey(b.getCode()))
                .earnedAt(earnedMap.get(b.getCode()))
                .build())
            .sorted(Comparator.comparing(BadgeDTO::isEarned).reversed())
            .collect(Collectors.toList());

        return AchievementsPageDTO.builder()
            .stats(stats)
            .badges(badges)
            .build();
    }

    // ─── Leaderboard ───────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<LeaderboardEntryDTO> getLeaderboard(Long currentUserId) {
        List<Apprenant> allApprenants = apprenantRepository.findAll();

        List<LeaderboardEntryDTO> leaderboard = allApprenants.stream().map(apprenant -> {
            List<Inscription> inscriptions = inscriptionRepository.findByApprenantId(apprenant.getId());
            List<ApprenantBadge> badges = apprenantBadgeRepository.findByApprenantId(apprenant.getId());

            int completed  = (int) inscriptions.stream().filter(i -> i.getProgression() >= 100).count();
            int badgeCount = badges.size();
            int score      = (completed * 100) + (badgeCount * 10);

            return LeaderboardEntryDTO.builder()
                .userId(apprenant.getId())
                .username(apprenant.getUsername())
                .picture(apprenant.getPicture())
                .coursesCompleted(completed)
                .badgesEarned(badgeCount)
                .score(score)
                .currentUser(apprenant.getId().equals(currentUserId))
                .build();
        }).collect(Collectors.toList());

        leaderboard.sort(Comparator.comparingInt(LeaderboardEntryDTO::getScore).reversed());

        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).setRank(i + 1);
        }

        return leaderboard;
    }
}