package com.example.SmartLearning.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SmartLearning.model.Course;
import com.example.SmartLearning.model.Formateur;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
 // TRAINER queries
 List<Course> findByFormateur(Formateur formateur);
 List<Course> findByFormateurAndIsActive(Formateur formateur, Boolean isActive);
 Optional<Course> findByIdAndFormateur(Long id, Formateur formateur);
 long countByFormateur(Formateur formateur);
 
 // ADMIN queries
 List<Course> findAllByOrderByCreatedAtDesc();
 List<Course> findByIsActive(Boolean isActive);
 long countByIsActive(Boolean isActive);
 
 // General
 Optional<Course> findByTitle(String title);
 List<Course> findByCategory(String category);
}
