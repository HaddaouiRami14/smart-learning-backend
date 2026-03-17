package com.example.SmartLearning.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.example.SmartLearning.model.Exercise;
import com.example.SmartLearning.model.TestCase;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    void deleteByExercise(Exercise exercise);
    /*@Modifying
    @Query(value = "DELETE FROM test_cases WHERE exercise_id = :exerciseId", nativeQuery = true)
    void deleteByExerciseId(@Param("exerciseId") Long exerciseId);*/
    void deleteByExerciseId(Long exerciseId);

}
