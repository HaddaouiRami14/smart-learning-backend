package com.example.SmartLearning.controller;

import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

import com.example.SmartLearning.DTO.ChapterResourceResponse;
import com.example.SmartLearning.DTO.ChapterResponse;
import com.example.SmartLearning.DTO.CourseDTO;
import com.example.SmartLearning.DTO.ExerciseResponse;
import com.example.SmartLearning.DTO.ExerciseResultResponse;
import com.example.SmartLearning.DTO.QuizResponse;
import com.example.SmartLearning.DTO.QuizResultResponse;
import com.example.SmartLearning.DTO.SubmitExerciseRequest;
import com.example.SmartLearning.DTO.SubmitQuizRequest;
import com.example.SmartLearning.model.ChapterResource;
import com.example.SmartLearning.service.CourseService;
import com.example.SmartLearning.service.ChapterService;
import com.example.SmartLearning.service.QuizService;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;

import com.example.SmartLearning.service.ExerciseService;

@RestController
@RequestMapping("/api/learner/courses")
@PreAuthorize("hasRole('APPRENANT')")
public class LearnerCourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private QuizService quizService;

    @Autowired
    private ExerciseService exerciseService;

    // ── GET /api/learner/courses/{courseId} ──────────────────────────────────
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDTO> getCourse(@PathVariable Long courseId) {
        // reuse the existing getCourseById — no ownership check needed for learners
        return ResponseEntity.ok(courseService.getCourseById(courseId));
    }

    // ── GET /api/learner/courses/{courseId}/chapters ─────────────────────────
    @GetMapping("/{courseId}/chapters")
    public ResponseEntity<List<ChapterResponse>> getChapters(@PathVariable Long courseId) {
        return ResponseEntity.ok(chapterService.getChaptersByCourse(courseId));
    }

    // ── GET /api/learner/courses/{courseId}/chapters/{chapterId}/quiz ─────────
    @GetMapping("/{courseId}/chapters/{chapterId}/quiz")
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

    // ── POST /api/learner/courses/{courseId}/chapters/{chapterId}/quiz/{quizId}/submit
    @PostMapping("/{courseId}/chapters/{chapterId}/quiz/{quizId}/submit")
    public ResponseEntity<QuizResultResponse> submitQuiz(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @PathVariable Long quizId,
            @Valid @RequestBody SubmitQuizRequest request
    ) {
        QuizResultResponse result = quizService.submitQuiz(quizId, request);
        return ResponseEntity.ok(result);
    }

    // ── GET /api/learner/courses/{courseId}/chapters/{chapterId}/exercises ────
    @GetMapping("/{courseId}/chapters/{chapterId}/exercises")
    public ResponseEntity<List<ExerciseResponse>> getExercises(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            Authentication authentication
    ) {
        String userEmail = authentication != null ? authentication.getName() : null;
        List<ExerciseResponse> exercises = exerciseService.getExercisesByChapter(chapterId, userEmail);
        return ResponseEntity.ok(exercises);
    }

    // ── POST /api/learner/courses/{courseId}/chapters/{chapterId}/exercises/{exerciseId}/submit
    @PostMapping("/{courseId}/chapters/{chapterId}/exercises/{exerciseId}/submit")
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


    @GetMapping("/{courseId}/chapters/{chapterId}/resources")
    public ResponseEntity<List<ChapterResourceResponse>> getResources(
            @PathVariable Long courseId,
            @PathVariable Long chapterId
    ) {
        List<ChapterResourceResponse> resources = chapterService.getChapterResources(courseId, chapterId);
        return ResponseEntity.ok(resources);
    }

     @GetMapping("/{courseId}/chapters/{chapterId}/resources/{resourceId}/download")
    public ResponseEntity<Resource> downloadResource(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @PathVariable Long resourceId
    ) {
        ChapterResource resource = chapterService.downloadResource(courseId, chapterId, resourceId);
        
        ByteArrayResource byteResource = new ByteArrayResource(resource.getFileData());
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + resource.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(resource.getMimeType()))
                .contentLength(resource.getFileSize())
                .body(byteResource);
    }
}