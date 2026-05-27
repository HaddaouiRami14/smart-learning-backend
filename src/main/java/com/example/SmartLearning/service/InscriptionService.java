package com.example.SmartLearning.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartLearning.DTO.InscriptionDTO;
import com.example.SmartLearning.DTO.ProgressDetailDTO;
import com.example.SmartLearning.DTO.StudentProgressDTO;
import com.example.SmartLearning.Repository.ApprenantRepository;
import com.example.SmartLearning.Repository.ChapterRepository;
import com.example.SmartLearning.Repository.CourseRepository;
import com.example.SmartLearning.Repository.ExerciseRepository;
import com.example.SmartLearning.Repository.InscriptionRepository;
import com.example.SmartLearning.Repository.QuizRepository;
import com.example.SmartLearning.model.Apprenant;
import com.example.SmartLearning.model.Chapter;
import com.example.SmartLearning.model.Course;
import com.example.SmartLearning.model.Exercise;
import com.example.SmartLearning.model.Inscription;
import com.example.SmartLearning.model.Quiz;
import com.example.SmartLearning.recommendation.RecommendationService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class InscriptionService {

    @Autowired private InscriptionRepository inscriptionRepository;
    @Autowired private ApprenantRepository apprenantRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private ChapterRepository chapterRepository;
    @Autowired private QuizRepository quizRepository;
    @Autowired private ExerciseRepository exerciseRepository;
    @Autowired private ActivityService activityService;
    @Autowired private BadgeService badgeService; 
    @Autowired private RecommendationService recommendationService;

    private Long getApprenantId(Long userId) {
        Apprenant apprenant = apprenantRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Apprenant not found for user: " + userId));
        return apprenant.getId();
    }

    public InscriptionDTO enroll(Long userId, Long courseId) {
        Long apprenantId = getApprenantId(userId);

        Optional<Inscription> existingInscription = inscriptionRepository.findByApprenantIdAndCourseId(apprenantId, courseId);
        if (existingInscription.isPresent()) {
            return mapToDTO(existingInscription.get());
        }

        Apprenant apprenant = apprenantRepository.findById(apprenantId)
            .orElseThrow(() -> new RuntimeException("Apprenant not found"));
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        Inscription newInscription = Inscription.builder()
            .apprenant(apprenant)
            .course(course)
            .dateInscription(LocalDate.now())
            .progression(0.0)
            .completedItems("")
            .build();

        try {
            InscriptionDTO dto = mapToDTO(inscriptionRepository.save(newInscription));
            recommendationService.refreshRecommendations(apprenantId);
            
            try {
                badgeService.checkAndAwardBadges(userId);
            } catch (Exception e) {
                System.err.println("Badge awarding failed on enroll: " + e.getMessage());
            }
            return dto;
        } catch (DataIntegrityViolationException ex) {
            return mapToDTO(inscriptionRepository.findByApprenantIdAndCourseId(apprenantId, courseId)
                .orElseThrow(() -> new RuntimeException("Erreur critique lors de l'inscription")));
        }
    }

    public InscriptionDTO updateProgress(Long userId, Long courseId, Double progression) {
        Long apprenantId = getApprenantId(userId);

        Inscription inscription = inscriptionRepository
            .findByApprenantIdAndCourseId(apprenantId, courseId)
            .orElseThrow(() -> new RuntimeException("Inscription not found"));

        inscription.setProgression(Math.min(100.0, Math.max(0.0, progression)));
        InscriptionDTO dto = mapToDTO(inscriptionRepository.save(inscription));

        recommendationService.refreshRecommendations(apprenantId);

        return dto;
        
    }

    public List<InscriptionDTO> getLearnerEnrollments(Long userId) {
        Long apprenantId = getApprenantId(userId);
        return inscriptionRepository.findByApprenantId(apprenantId)
            .stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    public InscriptionDTO getEnrollment(Long userId, Long courseId) {
        Long apprenantId = getApprenantId(userId);
        return inscriptionRepository
            .findByApprenantIdAndCourseId(apprenantId, courseId)
            .map(this::mapToDTO)
            .orElse(null);
    }

    public boolean isEnrolled(Long userId, Long courseId) {
        Long apprenantId = getApprenantId(userId);
        return inscriptionRepository.existsByApprenantIdAndCourseId(apprenantId, courseId);
    }

    public ProgressDetailDTO markItemCompleted(Long userId, Long courseId, String item) {
        Long apprenantId = getApprenantId(userId);

        Inscription inscription = inscriptionRepository
            .findByApprenantIdAndCourseId(apprenantId, courseId)
            .orElseThrow(() -> new RuntimeException("Inscription not found"));

        Set<String> items = parseCompletedItems(inscription.getCompletedItems());
        items.add(item);
        inscription.setCompletedItems(String.join(",", items));
        inscriptionRepository.save(inscription);
        recommendationService.refreshRecommendations(apprenantId);

        ProgressDetailDTO result = getProgressDetail(userId, courseId);

        
        try {
            
            if (item.endsWith(":E")) {
                try {
                    Long chapterId = Long.parseLong(item.replace(":E", ""));
                    badgeService.onExercisePassed(userId, chapterId);
                } catch (NumberFormatException ignored) {
                    badgeService.onExercisePassed(userId, null);
                }
            }
            
            badgeService.checkAndAwardBadges(userId);
        } catch (Exception e) {
            System.err.println("Badge awarding failed on markItemCompleted: " + e.getMessage());
        }

        return result;
    }

   public ProgressDetailDTO getProgressDetail(Long userId, Long courseId) {
    Long apprenantId = getApprenantId(userId);

    Inscription inscription = inscriptionRepository
        .findByApprenantIdAndCourseId(apprenantId, courseId)
        .orElse(null);

    if (inscription == null) return null;

    Set<String> completedItems = parseCompletedItems(inscription.getCompletedItems());
    List<Chapter> chapters = chapterRepository.findByCourseIdOrderByOrderIndexAsc(courseId);

    Map<Long, ProgressDetailDTO.ChapterProgressDTO> chapterProgress = new HashMap<>();
    
    
    int totalItems = 0;
    int completedItemsCount = 0;
    int completedChaptersCount = 0; 

    for (Chapter chapter : chapters) {
        Optional<Quiz> quizOpt = quizRepository.findByChapterId(chapter.getId());
        boolean hasQuiz = quizOpt.isPresent();

        List<Exercise> exercises = exerciseRepository.findByChapterId(chapter.getId());
        boolean hasExercise = !exercises.isEmpty();

        boolean quizPassed = completedItems.contains(chapter.getId() + ":Q");
        boolean exercisePassed = completedItems.contains(chapter.getId() + ":E");

        
        if (hasQuiz) {
            totalItems++;
            if (quizPassed) completedItemsCount++;
        }
        if (hasExercise) {
            totalItems++;
            if (exercisePassed) completedItemsCount++;
        }
        if (!hasQuiz && !hasExercise) {
            
            totalItems++;
            completedItemsCount++;
        }

        
        boolean chapterComplete;
        if (!hasQuiz && !hasExercise)       chapterComplete = true;
        else if (hasQuiz && !hasExercise)   chapterComplete = quizPassed;
        else if (!hasQuiz && hasExercise)   chapterComplete = exercisePassed;
        else                                chapterComplete = quizPassed && exercisePassed;

        if (chapterComplete) completedChaptersCount++;

        chapterProgress.put(chapter.getId(), ProgressDetailDTO.ChapterProgressDTO.builder()
            .quizPassed(hasQuiz ? quizPassed : null)
            .exercisePassed(hasExercise ? exercisePassed : null)
            .completed(chapterComplete)
            .build());
    }

    
    double progress = totalItems > 0 
        ? (completedItemsCount * 100.0 / totalItems) 
        : 0.0;
    progress = Math.round(progress * 10.0) / 10.0;

    
    if (Math.abs(inscription.getProgression() - progress) > 0.1) {
        double oldProgress = inscription.getProgression();

        Apprenant apprenant = inscription.getApprenant();
        Course course = inscription.getCourse();
        String categoryLabel = course.getCategory().getLabel();

        List<Inscription> categoryInscriptions = inscriptionRepository
            .findByApprenantId(apprenant.getId())
            .stream()
            .filter(i -> i.getCourse().getCategory().getLabel().equals(categoryLabel))
            .collect(Collectors.toList());

        double oldCategoryProg = computeAvg(categoryInscriptions);
        String oldLevel = resolveLevel(oldCategoryProg);

        inscription.setProgression(progress);
        inscriptionRepository.save(inscription);
        recommendationService.refreshRecommendations(apprenantId);
        inscriptionRepository.flush();

        double newCategoryProg = computeAvgWithOverride(
            categoryInscriptions, inscription.getId(), progress
        );
        String newLevel = resolveLevel(newCategoryProg);

        if (progress >= 100.0 && oldProgress < 100.0) {
            activityService.logCourseCompleted(apprenant, course);
        }
        if (!oldLevel.equals(newLevel)) {
            activityService.logSkillLevelUp(apprenant, categoryLabel);
        }
    }

    return ProgressDetailDTO.builder()
        .progression(progress)
        .completedItems(new ArrayList<>(completedItems))
        .chapterProgress(chapterProgress)
        .build();
}

    private Set<String> parseCompletedItems(String completedItems) {
        if (completedItems == null || completedItems.trim().isEmpty()) {
            return new HashSet<>();
        }
        return Arrays.stream(completedItems.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
    }

    private InscriptionDTO mapToDTO(Inscription i) {
        InscriptionDTO dto = new InscriptionDTO();
        dto.setId(i.getId());
        dto.setCourseId(i.getCourse().getId());
        dto.setCourseTitle(i.getCourse().getTitle());
        dto.setCourseImageUrl(i.getCourse().getImageUrl());
        dto.setCourseCategory(i.getCourse().getCategory());
        dto.setDateInscription(i.getDateInscription());
        dto.setProgression(i.getProgression());
        return dto;
    }

    private String resolveLevel(double pct) {
        if (pct <= 0)  return "Not Started";
        if (pct < 35)  return "Beginner";
        if (pct < 65)  return "Intermediate";
        if (pct < 85)  return "Advanced";
        return "Expert";
    }

    private double computeAvg(List<Inscription> inscriptions) {
        if (inscriptions.isEmpty()) return 0.0;
        double avg = inscriptions.stream()
            .mapToDouble(Inscription::getProgression)
            .average()
            .orElse(0.0);
        return Math.round(avg * 10.0) / 10.0;
    }

    private double computeAvgWithOverride(List<Inscription> inscriptions,
                                           Long inscriptionId,
                                           double newProgression) {
        if (inscriptions.isEmpty()) return 0.0;
        double sum = inscriptions.stream()
            .mapToDouble(i -> i.getId().equals(inscriptionId) ? newProgression : i.getProgression())
            .sum();
        double avg = sum / inscriptions.size();
        return Math.round(avg * 10.0) / 10.0;
    }


    public List<Map<String, Object>> getEnrollmentTrends(Long formateurId) {

    LocalDate since = LocalDate.now().minusDays(6);

    List<Object[]> rawData = inscriptionRepository
        .getEnrollmentTrends(formateurId, since);

    Map<String, Long> map = new HashMap<>();

    for (Object[] row : rawData) {
        String day = row[0].toString(); // yyyy-MM-dd
        Long count = ((Number) row[1]).longValue();
        map.put(day, count);
    }

    List<Map<String, Object>> result = new ArrayList<>();

    for (int i = 6; i >= 0; i--) {
        LocalDate date = LocalDate.now().minusDays(i);

        result.add(Map.of(
            "day", date.getDayOfWeek().toString().substring(0, 3),
            "count", map.getOrDefault(date.toString(), 0L)
        ));
    }

    return result;
}

public List<StudentProgressDTO> getStudentsByFormateur(Long formateurId) {
    List<Inscription> inscriptions = inscriptionRepository
        .findByCoursFormateurId(formateurId); 

    return inscriptions.stream()
        .collect(Collectors.groupingBy(i -> i.getApprenant()))
        .entrySet().stream()
        .map(entry -> {
            Apprenant apprenant = entry.getKey();
            List<Inscription> appInscriptions = entry.getValue();
            double avg = appInscriptions.stream()
                .mapToDouble(Inscription::getProgression)
                .average().orElse(0.0);
            String lastActive = appInscriptions.stream()
                .map(i -> i.getDateInscription().toString())
                .max(Comparator.naturalOrder()).orElse("");
            return StudentProgressDTO.builder()
                .id(apprenant.getId())
                .username(apprenant.getUsername())
                .email(apprenant.getEmail())
                .avatarUrl(null)
                .enrolledCourses(appInscriptions.size())
                .avgProgress(Math.round(avg * 10.0) / 10.0)
                .lastActive(lastActive)
                .build();
        })
        .collect(Collectors.toList());
}

public List<Object[]> getInscriptionByCourse() {
    return inscriptionRepository.countInscriptionsByCourse();
}

public Long getInscription() {
    return inscriptionRepository.countInscriptions();
}

public Long getCompletedInscriptions() {
    return inscriptionRepository.countCompletedInscriptions();
}
}