package com.example.SmartLearning.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.SmartLearning.Enum.Category;
import com.example.SmartLearning.model.Inscription;

import java.util.Optional;
import java.util.List;

public interface InscriptionRepository extends JpaRepository<Inscription, Long> {
    Optional<Inscription> findByApprenantIdAndCourseId(Long apprenantId, Long courseId);
    List<Inscription> findByApprenantId(Long apprenantId);
    boolean existsByApprenantIdAndCourseId(Long apprenantId, Long courseId);

    
    @Query("""
        SELECT i FROM Inscription i
        JOIN FETCH i.course c
        WHERE i.apprenant.id = :apprenantId
          AND c.category     = :category
    """)
    List<Inscription> findByApprenantIdAndCourseCategory(
        @Param("apprenantId") Long apprenantId,
        @Param("category")    Category category
    );
 
    
@Query("""
    SELECT
        c.category                                              AS category,
        SUM(i.progression) /
            (SELECT COUNT(c2) FROM Course c2
             WHERE c2.category = c.category)                   AS avgProgression,
        (SELECT COUNT(c2) FROM Course c2
         WHERE c2.category = c.category)                       AS enrolledCourses,
        SUM(CASE WHEN i.progression >= 100 THEN 1 ELSE 0 END)  AS completedCourses
    FROM Inscription i
    JOIN i.course c
    WHERE i.apprenant.id = :apprenantId
    GROUP BY c.category
    ORDER BY c.category
""")
List<CategorySkillProjection> aggregateSkillsByCategory(@Param("apprenantId") Long apprenantId);



@Query("""
    SELECT i FROM Inscription i
    JOIN FETCH i.course c
    WHERE i.apprenant.id = :apprenantId
    ORDER BY i.dateInscription DESC
""")
List<Inscription> findEnrolledCoursesByApprenant(@Param("apprenantId") Long apprenantId);
}