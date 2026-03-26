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

    
    public Long getApprenantIdFromUsername(String username) {
        return apprenantRepository.findByUserUsername(username)
            .map(Apprenant::getId)
            .orElse(null);
    }

    
    @Transactional
    public void markQuizCompleted(String username, Long courseId, Long chapterId) {
        Long apprenantId = getApprenantIdFromUsername(username);
        if (apprenantId == null) return;
        
        updateProgress(apprenantId, courseId, chapterId, "Q");
    }

    
    @Transactional
    public void markExerciseCompleted(String username, Long courseId, Long chapterId) {
        Long apprenantId = getApprenantIdFromUsername(username);
        if (apprenantId == null) return;
        
        updateProgress(apprenantId, courseId, chapterId, "E");
    }

    
    private void updateProgress(Long apprenantId, Long courseId, Long chapterId, String itemType) {
        Optional<Inscription> inscriptionOpt = inscriptionRepository.findByApprenantIdAndCourseId(apprenantId, courseId);
        if (inscriptionOpt.isEmpty()) return;
        
        Inscription inscription = inscriptionOpt.get();
        
        
        Set<String> completedItems = parseCompletedItems(inscription.getCompletedItems());
        completedItems.add(chapterId + ":" + itemType);
        
        List<Chapter> chapters = chapterRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        int completedCount = 0;
        
        for (Chapter chapter : chapters) {
            if (isChapterComplete(chapter.getId(), completedItems)) {
                completedCount++;
            }
        }
        
        int totalChapters = chapters.size();
        double progress = totalChapters > 0 ? (completedCount * 100.0 / totalChapters) : 0.0;
        
        inscription.setCompletedItems(String.join(",", completedItems));
        inscription.setProgression(Math.round(progress * 10.0) / 10.0);
        inscriptionRepository.save(inscription);
    }

    
    private boolean isChapterComplete(Long chapterId, Set<String> completedItems) {
        boolean hasQuiz = quizRepository.findByChapterId(chapterId).isPresent();
        List<Exercise> exercises = exerciseRepository.findByChapterId(chapterId);
        boolean hasExercise = !exercises.isEmpty();
        
        boolean quizDone = completedItems.contains(chapterId + ":Q");
        boolean exerciseDone = completedItems.contains(chapterId + ":E");
        
        
        if (!hasQuiz && !hasExercise) {
            return true; 
        }
        
        if (hasQuiz && !hasExercise) {
            return quizDone;
        }
        
        if (!hasQuiz && hasExercise) {
            return exerciseDone;
        }
        
        return quizDone && exerciseDone;
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
}