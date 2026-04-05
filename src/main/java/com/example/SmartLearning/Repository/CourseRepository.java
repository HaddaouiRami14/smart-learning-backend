package com.example.SmartLearning.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import com.example.SmartLearning.model.Course;
import com.example.SmartLearning.model.Formateur;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
 List<Course> findByFormateur(Formateur formateur);
 List<Course> findByFormateurAndIsActive(Formateur formateur, Boolean isActive);
 Optional<Course> findByIdAndFormateur(Long id, Formateur formateur);
 long countByFormateur(Formateur formateur);
 
 List<Course> findAllByOrderByCreatedAtDesc();
 List<Course> findByIsActive(Boolean isActive);
 long countByIsActive(Boolean isActive);
 
 
 Optional<Course> findByTitle(String title);
 List<Course> findByCategory(String category);

  // Cours non encore inscrits (candidats à la recommandation)
    @Query("""
        SELECT c FROM Course c
        WHERE c.id NOT IN :enrolledIds
        ORDER BY c.createdAt DESC
    """)
    List<Course> findCandidateCourses(@Param("enrolledIds") List<Long> enrolledIds);

}
