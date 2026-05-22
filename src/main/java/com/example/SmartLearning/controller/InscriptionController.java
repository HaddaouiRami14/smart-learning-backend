package com.example.SmartLearning.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.SmartLearning.DTO.InscriptionDTO;
import com.example.SmartLearning.DTO.ProgressDetailDTO;
import com.example.SmartLearning.service.InscriptionService;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/learner/enrollments")
@PreAuthorize("hasRole('APPRENANT')")
public class InscriptionController {

    @Autowired private InscriptionService inscriptionService;

    // Enroll in a course
    @PostMapping("/{userId}/enroll/{courseId}")
    public ResponseEntity<InscriptionDTO> enroll(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        return ResponseEntity.ok(inscriptionService.enroll(userId, courseId));
    }

    @PutMapping("/{userId}/progress/{courseId}")
    public ResponseEntity<InscriptionDTO> updateProgress(
            @PathVariable Long userId,
            @PathVariable Long courseId,
            @RequestBody Map<String, Double> body) {
        Double progression = body.get("progression");
        return ResponseEntity.ok(inscriptionService.updateProgress(userId, courseId, progression));
    }

    
    @PostMapping("/{userId}/complete/{courseId}")
    public ResponseEntity<ProgressDetailDTO> markCompleted(
            @PathVariable Long userId,
            @PathVariable Long courseId,
            @RequestBody Map<String, String> body) {
        String item = body.get("item");
        return ResponseEntity.ok(inscriptionService.markItemCompleted(userId, courseId, item));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<InscriptionDTO>> getEnrollments(
            @PathVariable Long userId) {
        return ResponseEntity.ok(inscriptionService.getLearnerEnrollments(userId));
    }

    @GetMapping("/{userId}/course/{courseId}")
    public ResponseEntity<InscriptionDTO> getEnrollment(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        InscriptionDTO dto = inscriptionService.getEnrollment(userId, courseId);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{userId}/progress/{courseId}")
    public ResponseEntity<ProgressDetailDTO> getProgressDetail(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        ProgressDetailDTO progress = inscriptionService.getProgressDetail(userId, courseId);
        if (progress == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/{userId}/enrolled/{courseId}")
    public ResponseEntity<Map<String, Boolean>> isEnrolled(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        return ResponseEntity.ok(Map.of("enrolled",
            inscriptionService.isEnrolled(userId, courseId)));
    }
   
    
}