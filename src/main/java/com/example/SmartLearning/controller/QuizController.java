package com.example.SmartLearning.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.SmartLearning.DTO.CreateQuizRequest;
import com.example.SmartLearning.DTO.QuizResponse;
import com.example.SmartLearning.DTO.QuizResultResponse;
import com.example.SmartLearning.DTO.SubmitQuizRequest;
import com.example.SmartLearning.Repository.QuestionRepository;
import com.example.SmartLearning.service.QuizService;

@RestController
@RequestMapping("/api/formateur/courses/{courseId}/chapters/{chapterId}/quiz")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuizController {
    
    private final QuizService quizService;
    private final QuestionRepository questionRepository;
    
    @PostMapping
    public ResponseEntity<QuizResponse> createQuiz(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @Valid @RequestBody CreateQuizRequest request
    ) {
        QuizResponse quiz = quizService.createQuiz(courseId, chapterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(quiz);
    }
    
    @GetMapping
    public ResponseEntity<QuizResponse> getQuiz(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @RequestParam(defaultValue = "true") boolean includeAnswers
    ) {
        QuizResponse quiz = quizService.getQuizByChapterId(chapterId, includeAnswers);
        if (quiz == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(quiz);
    }
    
    @PutMapping("/{quizId}")
    public ResponseEntity<QuizResponse> updateQuiz(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @PathVariable Long quizId,
            @Valid @RequestBody CreateQuizRequest request
    ) {
        QuizResponse quiz = quizService.updateQuiz(quizId, request);
        return ResponseEntity.ok(quiz);
    }
    
    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @PathVariable Long quizId
    ) {
        quizService.deleteQuiz(quizId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{quizId}/submit")
    public ResponseEntity<QuizResultResponse> submitQuiz(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @PathVariable Long quizId,
            @Valid @RequestBody SubmitQuizRequest request
    ) {
        QuizResultResponse result = quizService.submitQuiz(quizId, request);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{quizId}/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
        @PathVariable Long courseId,
        @PathVariable Long chapterId,
        @PathVariable Long quizId,
        @PathVariable Long questionId) {
        
        questionRepository.deleteById(questionId);
        return ResponseEntity.noContent().build();
    }
}