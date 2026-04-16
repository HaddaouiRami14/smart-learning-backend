package com.example.SmartLearning.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.SmartLearning.Enum.Category;
import com.example.SmartLearning.model.Inscription;

import java.util.Optional;
import java.time.LocalDate;
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

// ── Nouvelles méthodes pour la recommandation ─────────────────────────────
 
    @Query("SELECT i.course.id FROM Inscription i WHERE i.apprenant.id = :apprenantId")
    List<Long> findCourseIdsByApprenantId(@Param("apprenantId") Long apprenantId);
 
    // Trending : nb d'inscriptions par cours dans les 7 derniers jours
    @Query("""
        SELECT i.course.id, COUNT(i)
        FROM Inscription i
        WHERE i.course.id IN :courseIds
          AND i.dateInscription >= :since
        GROUP BY i.course.id
        ORDER BY COUNT(i) DESC
    """)
    List<Object[]> countRecentInscriptionsByCourseIds(
        @Param("courseIds") List<Long> courseIds,
        @Param("since")     LocalDate since
    );
 
    // Apprenants ayant les mêmes cours inscrits (collaboratif)
    @Query("""
        SELECT DISTINCT i.apprenant.id
        FROM Inscription i
        WHERE i.course.id IN :courseIds
          AND i.apprenant.id != :apprenantId
    """)
    List<Long> findSimilarApprenantIds(
        @Param("apprenantId") Long apprenantId,
        @Param("courseIds")   List<Long> courseIds
    );
 
    @Query("""
    SELECT 
        FUNCTION('DATE', i.dateInscription) AS day,
        COUNT(i) AS count
    FROM Inscription i
    JOIN i.course c
    WHERE c.formateur.id = :formateurId
      AND i.dateInscription >= :since
    GROUP BY FUNCTION('DATE', i.dateInscription)
    ORDER BY FUNCTION('DATE', i.dateInscription)
""")
List<Object[]> getEnrollmentTrends(
    @Param("formateurId") Long formateurId,
    @Param("since") LocalDate since
);

}