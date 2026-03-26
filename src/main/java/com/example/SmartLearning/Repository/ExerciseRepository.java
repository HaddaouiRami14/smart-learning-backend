package com.example.SmartLearning.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.example.SmartLearning.model.Chapter;
import com.example.SmartLearning.model.Exercise;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByChapterOrderByOrderIndexAsc(Chapter chapter);
    List<Exercise> findByChapterIdOrderByOrderIndexAsc(Long chapterId);

    List<Exercise> findByChapterId(Long chapterId);
    
void deleteByChapterId(Long chapterId);

}