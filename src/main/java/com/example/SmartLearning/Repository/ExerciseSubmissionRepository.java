package com.example.SmartLearning.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.SmartLearning.model.Apprenant;
import com.example.SmartLearning.model.Exercise;
import com.example.SmartLearning.model.ExerciseSubmission;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseSubmissionRepository extends JpaRepository<ExerciseSubmission, Long> {
    List<ExerciseSubmission> findByApprenantOrderBySubmittedAtDesc(Apprenant apprenant);
    Optional<ExerciseSubmission> findTopByExerciseAndApprenantAndPassedTrueOrderBySubmittedAtDesc(
        Exercise exercise, Apprenant apprenant
    );
    List<ExerciseSubmission> findByExerciseAndApprenant(Exercise exercise, Apprenant apprenant);
    void deleteByExercise(Exercise exercise);
    
    void deleteByExerciseId(Long exerciseId);

     // ── Nouvelles méthodes pour la recommandation ─────────────────────────────
 
    // Score moyen par cours (via exercise → chapter → course)
    @Query("""
        SELECT es.exercise.chapter.course.id, AVG(es.score * 1.0 / NULLIF(es.totalTests, 0) * 100)
        FROM ExerciseSubmission es
        WHERE es.apprenant.id = :apprenantId
        GROUP BY es.exercise.chapter.course.id
    """)
    List<Object[]> avgScorePercentPerCourse(@Param("apprenantId") Long apprenantId);
 
    // Taux de réussite global de l'apprenant (passed=true / total)
    @Query("SELECT COALESCE((COUNT(CASE WHEN e.passed = true THEN 1 END) * 100.0 / NULLIF(COUNT(e.id), 0)), 0.0) " +
       "FROM ExerciseSubmission e WHERE e.apprenant.id = :apprenantId")
    Double globalSuccessRate(@Param("apprenantId") Long apprenantId);


}