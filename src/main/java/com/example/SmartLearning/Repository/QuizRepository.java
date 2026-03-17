package com.example.SmartLearning.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.SmartLearning.model.Chapter;
import com.example.SmartLearning.model.Quiz;

import jakarta.transaction.Transactional;

import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByChapter(Chapter chapter);
    Optional<Quiz> findByChapterId(Long chapterId);

/*@Modifying
@Query(value = "DELETE FROM quizzes WHERE id = :id", nativeQuery = true)
void deleteQuizDirectly(@Param("id") Long id);*/
@Modifying
@Transactional
@Query(value = "DELETE FROM quizzes WHERE id = :quizId", nativeQuery = true)
void deleteByIdNative(@Param("quizId") Long quizId);

}