package com.example.SmartLearning.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartLearning.DTO.InscriptionDTO;
import com.example.SmartLearning.DTO.ProgressDetailDTO;
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

    private Long getApprenantId(Long userId) {
        Apprenant apprenant = apprenantRepository.findByUser_Id(userId)
            .orElseThrow(() -> new RuntimeException("Apprenant not found for user: " + userId));
        return apprenant.getId();
    }

    public InscriptionDTO enroll(Long userId, Long courseId) {
        Long apprenantId = getApprenantId(userId);

        if (inscriptionRepository.existsByApprenantIdAndCourseId(apprenantId, courseId)) {
            return mapToDTO(inscriptionRepository
                .findByApprenantIdAndCourseId(apprenantId, courseId).get());
        }

        Apprenant apprenant = apprenantRepository.findById(apprenantId)
            .orElseThrow(() -> new RuntimeException("Apprenant not found"));
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        Inscription inscription = Inscription.builder()
            .apprenant(apprenant)
            .course(course)
            .dateInscription(LocalDate.now())
            .progression(0.0)
            .completedItems("")
            .build();

        return mapToDTO(inscriptionRepository.save(inscription));
    }

    public InscriptionDTO updateProgress(Long userId, Long courseId, Double progression) {
        Long apprenantId = getApprenantId(userId);

        Inscription inscription = inscriptionRepository
            .findByApprenantIdAndCourseId(apprenantId, courseId)
            .orElseThrow(() -> new RuntimeException("Inscription not found"));

        inscription.setProgression(Math.min(100.0, Math.max(0.0, progression)));
        return mapToDTO(inscriptionRepository.save(inscription));
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

    /**
     * Mark a quiz ("chapterId:Q") or exercise ("chapterId:E") as completed,
     * persist it, then recalculate and return full progress detail.
     */
    public ProgressDetailDTO markItemCompleted(Long userId, Long courseId, String item) {
        Long apprenantId = getApprenantId(userId);

        Inscription inscription = inscriptionRepository
            .findByApprenantIdAndCourseId(apprenantId, courseId)
            .orElseThrow(() -> new RuntimeException("Inscription not found"));

        Set<String> items = parseCompletedItems(inscription.getCompletedItems());
        items.add(item);
        inscription.setCompletedItems(String.join(",", items));
        inscriptionRepository.save(inscription);

        return getProgressDetail(userId, courseId);
    }

    /**
     * Get detailed progress — which chapters have quiz/exercise passed.
     */
    public ProgressDetailDTO getProgressDetail(Long userId, Long courseId) {
        Long apprenantId = getApprenantId(userId);

        Inscription inscription = inscriptionRepository
            .findByApprenantIdAndCourseId(apprenantId, courseId)
            .orElse(null);

        if (inscription == null) {
            return null;
        }

        Set<String> completedItems = parseCompletedItems(inscription.getCompletedItems());
        List<Chapter> chapters = chapterRepository.findByCourseIdOrderByOrderIndexAsc(courseId);

        Map<Long, ProgressDetailDTO.ChapterProgressDTO> chapterProgress = new HashMap<>();
        int completedCount = 0;

        for (Chapter chapter : chapters) {
            Optional<Quiz> quizOpt = quizRepository.findByChapterId(chapter.getId());
            boolean hasQuiz = quizOpt.isPresent();

            List<Exercise> exercises = exerciseRepository.findByChapterId(chapter.getId());
            boolean hasExercise = !exercises.isEmpty();

            boolean quizPassed = completedItems.contains(chapter.getId() + ":Q");
            boolean exercisePassed = completedItems.contains(chapter.getId() + ":E");

            boolean chapterComplete;
            if (!hasQuiz && !hasExercise) {
                chapterComplete = true;
            } else if (hasQuiz && !hasExercise) {
                chapterComplete = quizPassed;
            } else if (!hasQuiz && hasExercise) {
                chapterComplete = exercisePassed;
            } else {
                chapterComplete = quizPassed && exercisePassed;
            }

            if (chapterComplete) completedCount++;

            chapterProgress.put(chapter.getId(), ProgressDetailDTO.ChapterProgressDTO.builder()
                .quizPassed(hasQuiz ? quizPassed : null)
                .exercisePassed(hasExercise ? exercisePassed : null)
                .completed(chapterComplete)
                .build());
        }

        int totalChapters = chapters.size();
        double progress = totalChapters > 0 ? (completedCount * 100.0 / totalChapters) : 0.0;

        if (Math.abs(inscription.getProgression() - progress) > 0.1) {
            inscription.setProgression(Math.round(progress * 10.0) / 10.0);
            inscriptionRepository.save(inscription);
        }

        return ProgressDetailDTO.builder()
            .progression(Math.round(progress * 10.0) / 10.0)
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
}