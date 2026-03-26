package com.example.SmartLearning.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.SmartLearning.model.Question;
import com.example.SmartLearning.model.Quiz;

import jakarta.transaction.Transactional;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Query("SELECT q FROM Question q " +
           "LEFT JOIN FETCH q.options " +
           "WHERE q.quiz = :quiz " +
           "ORDER BY q.orderIndex ASC")
    List<Question> findByQuizOrderByOrderIndexAsc(@Param("quiz") Quiz quiz);

    List<Question> findByQuizId(Long quizId);
    
@Modifying
@Transactional
@Query(value = "DELETE FROM questions WHERE quiz_id = :quizId", nativeQuery = true)
void deleteByQuizId(@Param("quizId") Long quizId);
    

}
