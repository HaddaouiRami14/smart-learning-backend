package com.example.SmartLearning.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.SmartLearning.DTO.CreateExerciseRequest;
import com.example.SmartLearning.DTO.ExerciseResponse;
import com.example.SmartLearning.DTO.ExerciseResultResponse;
import com.example.SmartLearning.DTO.SubmitExerciseRequest;
import com.example.SmartLearning.service.ExerciseService;

import java.util.List;

@RestController
@RequestMapping("/api/formateur/courses/{courseId}/chapters/{chapterId}/exercises")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExerciseController {
    
    private final ExerciseService exerciseService;
    
    @PostMapping
    public ResponseEntity<ExerciseResponse> createExercise(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @Valid @RequestBody CreateExerciseRequest request
    ) {
        ExerciseResponse exercise = exerciseService.createExercise(courseId, chapterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(exercise);
    }
    
    @GetMapping
    public ResponseEntity<List<ExerciseResponse>> getExercises(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : null;
        List<ExerciseResponse> exercises = exerciseService.getExercisesByChapter(chapterId, userEmail);
        return ResponseEntity.ok(exercises);
    }
    
    @GetMapping("/{exerciseId}")
    public ResponseEntity<ExerciseResponse> getExercise(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @PathVariable Long exerciseId,
            @RequestParam(defaultValue = "false") boolean includeHiddenTests,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : null;
        ExerciseResponse exercise = exerciseService.getExerciseById(exerciseId, userEmail, includeHiddenTests);
        return ResponseEntity.ok(exercise);
    }
    
    @PutMapping("/{exerciseId}")
    public ResponseEntity<ExerciseResponse> updateExercise(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @PathVariable Long exerciseId,
            @Valid @RequestBody CreateExerciseRequest request
    ) {
        ExerciseResponse exercise = exerciseService.updateExercise(exerciseId, request);
        return ResponseEntity.ok(exercise);
    }
    
    @DeleteMapping("/{exerciseId}")
    public ResponseEntity<Void> deleteExercise(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @PathVariable Long exerciseId
    ) {
        exerciseService.deleteExercise(exerciseId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{exerciseId}/submit")
    public ResponseEntity<ExerciseResultResponse> submitExercise(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @PathVariable Long exerciseId,
            @Valid @RequestBody SubmitExerciseRequest request,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        ExerciseResultResponse result = exerciseService.submitExercise(exerciseId, userEmail, request);
        return ResponseEntity.ok(result);
    }
}
