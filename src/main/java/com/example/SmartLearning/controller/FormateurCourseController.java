package com.example.SmartLearning.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SmartLearning.DTO.CourseDTO;
import com.example.SmartLearning.DTO.StudentProgressDTO;
import com.example.SmartLearning.DTO.TrainerStatsDTO;
import com.example.SmartLearning.security.JwtUserPrincipal;
import com.example.SmartLearning.service.CourseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/formateur/courses")
@PreAuthorize("hasRole('FORMATEUR')")
public class FormateurCourseController {

    @Autowired
    private CourseService courseService;

    private Long getTrainerId() {
        JwtUserPrincipal principal = (JwtUserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal.getFormateurId() == null) {
            throw new AccessDeniedException("User is not a trainer");
        }

        return principal.getFormateurId();
    }

    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
        Long trainerId = getTrainerId();
        CourseDTO createdCourse = courseService.createCourse(trainerId, courseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
    }

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getTrainerCourses() {
        Long trainerId = getTrainerId();
        List<CourseDTO> courses = courseService.getTrainerCourses(trainerId);
        return ResponseEntity.ok(courses);
    }

    
    @GetMapping("/stats")
    public ResponseEntity<TrainerStatsDTO> getTrainerStats() {
        Long trainerId = getTrainerId();
        TrainerStatsDTO stats = courseService.getTrainerStats(trainerId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourse(@PathVariable Long id) {
        Long trainerId = getTrainerId();
        CourseDTO course = courseService.getTrainerCourse(trainerId, id);
        return ResponseEntity.ok(course);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseDTO courseDTO) {
        Long trainerId = getTrainerId();
        CourseDTO updatedCourse = courseService.updateTrainerCourse(trainerId, id, courseDTO);
        return ResponseEntity.ok(updatedCourse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        Long trainerId = getTrainerId();
        courseService.deleteTrainerCourse(trainerId, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<CourseDTO> activateCourse(@PathVariable Long id) {
        CourseDTO activatedCourse = courseService.activateCourse(id);
        return ResponseEntity.ok(activatedCourse);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<CourseDTO> deactivateCourse(@PathVariable Long id) {
        CourseDTO deactivatedCourse = courseService.deactivateCourse(id);
        return ResponseEntity.ok(deactivatedCourse);
    }

    @Autowired
private com.example.SmartLearning.service.InscriptionService inscriptionService;

@GetMapping("/enrollment-trends")
public ResponseEntity<List<Map<String, Object>>> getEnrollmentTrends() {
    Long trainerId = getTrainerId();

    return ResponseEntity.ok(
        inscriptionService.getEnrollmentTrends(trainerId)
    );
}

@GetMapping("/students")
 public ResponseEntity<List<StudentProgressDTO>> getMyStudents(
        @RequestHeader("Authorization") String authHeader) {
    Long formateurId = getTrainerId();
    return ResponseEntity.ok(inscriptionService.getStudentsByFormateur(formateurId));
}
@GetMapping("/studentss")
    public List<Object[]> getInscriptionByCourse() {
        return inscriptionService.getInscriptionByCourse();
    }
}