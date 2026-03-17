package com.example.SmartLearning.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

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
    /*@Modifying
    @Query(value = "DELETE FROM exercise_submissions WHERE exercise_id = :exerciseId", nativeQuery = true)
    void deleteByExerciseId(@Param("exerciseId") Long exerciseId);*/
    void deleteByExerciseId(Long exerciseId);
}