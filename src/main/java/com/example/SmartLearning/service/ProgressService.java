package com.example.SmartLearning.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartLearning.Repository.ApprenantRepository;
import com.example.SmartLearning.Repository.ChapterRepository;
import com.example.SmartLearning.Repository.ExerciseRepository;
import com.example.SmartLearning.Repository.InscriptionRepository;
import com.example.SmartLearning.Repository.QuizRepository;
import com.example.SmartLearning.model.Apprenant;
import com.example.SmartLearning.model.Chapter;
import com.example.SmartLearning.model.Exercise;
import com.example.SmartLearning.model.Inscription;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final InscriptionRepository inscriptionRepository;
    private final ChapterRepository chapterRepository;
    private final QuizRepository quizRepository;
    private final ExerciseRepository exerciseRepository;
    private final ApprenantRepository apprenantRepository;

    /**
     * Get Apprenant ID from username (email)
     */
    public Long getApprenantIdFromUsername(String username) {
        return apprenantRepository.findByUserUsername(username)
            .map(Apprenant::getId)
            .orElse(null);
    }

    /**
     * Mark quiz as completed and update course progress
     */
    @Transactional
    public void markQuizCompleted(String username, Long courseId, Long chapterId) {
        Long apprenantId = getApprenantIdFromUsername(username);
        if (apprenantId == null) return;
        
        updateProgress(apprenantId, courseId, chapterId, "Q");
    }

    /**
     * Mark exercise as completed and update course progress
     */
    @Transactional
    public void markExerciseCompleted(String username, Long courseId, Long chapterId) {
        Long apprenantId = getApprenantIdFromUsername(username);
        if (apprenantId == null) return;
        
        updateProgress(apprenantId, courseId, chapterId, "E");
    }

    /**
     * Update progress when quiz or exercise is completed
     */
    private void updateProgress(Long apprenantId, Long courseId, Long chapterId, String itemType) {
        Optional<Inscription> inscriptionOpt = inscriptionRepository.findByApprenantIdAndCourseId(apprenantId, courseId);
        if (inscriptionOpt.isEmpty()) return;
        
        Inscription inscription = inscriptionOpt.get();
        
        // Add completed item
        Set<String> completedItems = parseCompletedItems(inscription.getCompletedItems());
        completedItems.add(chapterId + ":" + itemType);
        
        // Calculate how many chapters are complete
        List<Chapter> chapters = chapterRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        int completedCount = 0;
        
        for (Chapter chapter : chapters) {
            if (isChapterComplete(chapter.getId(), completedItems)) {
                completedCount++;
            }
        }
        
        // Calculate progress percentage
        int totalChapters = chapters.size();
        double progress = totalChapters > 0 ? (completedCount * 100.0 / totalChapters) : 0.0;
        
        // Save
        inscription.setCompletedItems(String.join(",", completedItems));
        inscription.setProgression(Math.round(progress * 10.0) / 10.0);
        inscriptionRepository.save(inscription);
    }

    /**
     * Check if a chapter is complete based on its quiz/exercise requirements
     */
    private boolean isChapterComplete(Long chapterId, Set<String> completedItems) {
        boolean hasQuiz = quizRepository.findByChapterId(chapterId).isPresent();
        List<Exercise> exercises = exerciseRepository.findByChapterId(chapterId);
        boolean hasExercise = !exercises.isEmpty();
        
        boolean quizDone = completedItems.contains(chapterId + ":Q");
        boolean exerciseDone = completedItems.contains(chapterId + ":E");
        
        // Chapter completion rules:
        // - No quiz + No exercise = complete (just content)
        // - Has quiz only = complete when quiz done
        // - Has exercise only = complete when exercise done
        // - Has both = complete when both done
        
        if (!hasQuiz && !hasExercise) {
            return true; // Just content, no assessment
        }
        
        if (hasQuiz && !hasExercise) {
            return quizDone;
        }
        
        if (!hasQuiz && hasExercise) {
            return exerciseDone;
        }
        
        // Has both quiz and exercise
        return quizDone && exerciseDone;
    }

    /**
     * Parse completed items from string to set
     */
    private Set<String> parseCompletedItems(String completedItems) {
        if (completedItems == null || completedItems.trim().isEmpty()) {
            return new HashSet<>();
        }
        return Arrays.stream(completedItems.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
    }
}