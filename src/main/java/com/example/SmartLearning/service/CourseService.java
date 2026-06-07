package com.example.SmartLearning.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.SmartLearning.DTO.CourseDTO;
import com.example.SmartLearning.DTO.TrainerStatsDTO;
import com.example.SmartLearning.Enum.Level;
import com.example.SmartLearning.Repository.ActivityLogRepository;
import com.example.SmartLearning.Repository.CourseRepository;
import com.example.SmartLearning.Repository.CourseRecommendationRepository;
import com.example.SmartLearning.Repository.InscriptionRepository;
import com.example.SmartLearning.model.Course;
import com.example.SmartLearning.model.Formateur;
import com.example.SmartLearning.Repository.StatsCoursRepository;
import com.example.SmartLearning.model.StatsCours;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private FormateurService formateurService;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private CourseRecommendationRepository courseRecommendationRepository;

    @Autowired
    private StatsCoursRepository statsCoursRepository;

    @Autowired
    private InscriptionRepository inscriptionRepository;

    public CourseDTO createCourse(Long formateurId, CourseDTO courseDTO) {
        Formateur formateur = formateurService.getFormateurById(formateurId);

        if (courseRepository.findByTitle(courseDTO.getTitle()).isPresent()) {
            throw new IllegalArgumentException("Course with this title already exists");
        }

        Course course = new Course();
        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setCategory(courseDTO.getCategory());
        course.setPrice(courseDTO.getPrice());
        course.setImageUrl(courseDTO.getImageUrl());
        course.setLevel(courseDTO.getLevel() != null ? courseDTO.getLevel() : Level.BEGINNER);
        course.setFormateur(formateur);
        course.setIsActive(false);

        Course savedCourse = courseRepository.save(course);
        return mapToDTO(savedCourse);
    }

    public List<CourseDTO> getTrainerCourses(Long formateurId) {
        Formateur formateur = formateurService.getFormateurById(formateurId);
        return courseRepository.findByFormateur(formateur)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CourseDTO getTrainerCourse(Long formateurId, Long courseId) {
        Formateur formateur = formateurService.getFormateurById(formateurId);
        Course course = courseRepository.findByIdAndFormateur(courseId, formateur)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));
        return mapToDTO(course);
    }

    public CourseDTO updateTrainerCourse(Long formateurId, Long courseId, CourseDTO courseDTO) {
        Formateur formateur = formateurService.getFormateurById(formateurId);
        Course course = courseRepository.findByIdAndFormateur(courseId, formateur)
                .orElseThrow(() -> new SecurityException("You don't have permission to update this course"));

        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setCategory(courseDTO.getCategory());
        course.setPrice(courseDTO.getPrice());
        course.setImageUrl(courseDTO.getImageUrl());
        course.setLevel(courseDTO.getLevel() != null ? courseDTO.getLevel() : course.getLevel());

        Course updatedCourse = courseRepository.save(course);
        return mapToDTO(updatedCourse);
    }

    public void deleteTrainerCourse(Long formateurId, Long courseId) {
        Formateur formateur = formateurService.getFormateurById(formateurId);
        Course course = courseRepository.findByIdAndFormateur(courseId, formateur)
                .orElseThrow(() -> new SecurityException("You don't have permission to delete this course"));
                
        if (course.getIsActive() != null && course.getIsActive()) {
            throw new IllegalStateException("Cannot delete an active course. Please deactivate it first.");
        }
        
        deleteCourseWithDependencies(course);
    }

    public CourseDTO requestPublish(Long formateurId, Long courseId) {
        Formateur formateur = formateurService.getFormateurById(formateurId);
        Course course = courseRepository.findByIdAndFormateur(courseId, formateur)
                .orElseThrow(() -> new SecurityException("You don't have permission to modify this course"));
        return mapToDTO(course);
    }

    // ✅ NEW: Trainer stats
    public TrainerStatsDTO getTrainerStats(Long formateurId) {
        Formateur formateur = formateurService.getFormateurById(formateurId);

        int totalCourses    = (int) courseRepository.countByFormateur(formateur);
        int activeCourses   = (int) courseRepository.countByFormateurAndIsActive(formateur, true);
        // Note: freeCourses and paidCourses are omitted as we don't need them right now 
        // (but we can set them to 0 or leave them in the DTO for future use)

        Long distinctStudentsCount = inscriptionRepository.countDistinctStudentsByFormateurId(formateurId);
        int totalStudents = distinctStudentsCount != null ? distinctStudentsCount.intValue() : 0;

        List<StatsCours> statsList = statsCoursRepository.findByFormateurId(formateurId);

        int totalEnrollments = 0;
        int totalCompletions = 0;
        double totalWeightedProgress = 0.0;

        for (StatsCours stats : statsList) {
            int count = stats.getNombreInscriptions();
            totalEnrollments += count;
            totalCompletions += (int) Math.round(stats.getTauxCompletionMoyen() * count / 100.0);
            totalWeightedProgress += stats.getProgressionMoyenne() * count;
        }

        double avgProgressPercent = totalEnrollments == 0 ? 0.0 : (totalWeightedProgress / totalEnrollments);
        avgProgressPercent = Math.round(avgProgressPercent * 10.0) / 10.0;

        return TrainerStatsDTO.builder()
                .totalCourses(totalCourses)
                .activeCourses(activeCourses)
                .totalStudents(totalStudents)
                .freeCourses(0)
                .paidCourses(0)
                .totalEnrollments(totalEnrollments)
                .totalCompletions(totalCompletions)
                .avgProgressPercent(avgProgressPercent)
                .build();
    }

    // ADMIN OPERATIONS
    public List<CourseDTO> getAllCoursesForAdmin() {
        return courseRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CourseDTO getCourseById(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));
        return mapToDTO(course);
    }

    public CourseDTO activateCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));
        course.setIsActive(true);
        return mapToDTO(courseRepository.save(course));
    }

    public CourseDTO deactivateCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));
        course.setIsActive(false);
        return mapToDTO(courseRepository.save(course));
    }

    public void adminDeleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));
        deleteCourseWithDependencies(course);
    }

    public CourseDTO adminUpdateCourse(Long courseId, CourseDTO courseDTO) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));

        course.setTitle(courseDTO.getTitle());
        course.setDescription(courseDTO.getDescription());
        course.setCategory(courseDTO.getCategory());
        course.setPrice(courseDTO.getPrice());
        course.setImageUrl(courseDTO.getImageUrl());
        course.setLevel(courseDTO.getLevel() != null ? courseDTO.getLevel() : course.getLevel());

        return mapToDTO(courseRepository.save(course));
    }

    private void deleteCourseWithDependencies(Course course) {
        // Delete all FK-dependent rows before removing the course
        activityLogRepository.deleteByCourseId(course.getId());
        courseRecommendationRepository.deleteByCoursId(course.getId()); // ← fixes FK violation
        statsCoursRepository.deleteByCourseId(course.getId());
        courseRepository.deleteById(course.getId());
    }

    private CourseDTO mapToDTO(Course course) {
        return CourseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .category(course.getCategory())
                .price(course.getPrice())
                .level(course.getLevel())
                .formateurName(course.getFormateur().getUsername())
                .imageUrl(course.getImageUrl())
                .isActive(course.getIsActive())
                .formateurId(course.getFormateur().getId())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}