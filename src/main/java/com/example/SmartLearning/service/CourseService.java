package com.example.SmartLearning.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.SmartLearning.DTO.CourseDTO;
import com.example.SmartLearning.DTO.TrainerStatsDTO;
import com.example.SmartLearning.Enum.Level;
import com.example.SmartLearning.Repository.CourseRepository;
import com.example.SmartLearning.model.Course;
import com.example.SmartLearning.model.Formateur;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private FormateurService formateurService;

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
        List<Course> courses = courseRepository.findByFormateur(formateur);

        int totalCourses    = courses.size();
        int activeCourses   = (int) courses.stream().filter(c -> Boolean.TRUE.equals(c.getIsActive())).count();
        int freeCourses     = (int) courses.stream().filter(c -> c.getPrice() == 0).count();
        int paidCourses     = (int) courses.stream().filter(c -> c.getPrice() > 0).count();

        long totalEnrollments = courseRepository.countEnrollmentsByFormateurId(formateurId);
        long totalCompletions = courseRepository.countCompletionsByFormateurId(formateurId);
        double avgProgress    = courseRepository.avgProgressByFormateurId(formateurId);

        return TrainerStatsDTO.builder()
                .totalCourses(totalCourses)
                .activeCourses(activeCourses)
                .freeCourses(freeCourses)
                .paidCourses(paidCourses)
                .totalEnrollments((int) totalEnrollments)
                .totalCompletions((int) totalCompletions)
                .avgProgressPercent(Math.round(avgProgress * 10.0) / 10.0)
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