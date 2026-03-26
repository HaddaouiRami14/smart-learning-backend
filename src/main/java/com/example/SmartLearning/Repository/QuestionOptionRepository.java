package com.example.SmartLearning.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.SmartLearning.model.QuestionOption;

import jakarta.transaction.Transactional;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
    
    void deleteByQuestionId(Long questionId);
@Modifying
@Transactional
@Query(value = "DELETE FROM question_options WHERE question_id IN (SELECT id FROM questions WHERE quiz_id = :quizId)", nativeQuery = true)
void deleteByQuizId(@Param("quizId") Long quizId);

}