package com.example.SmartLearning.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.SmartLearning.model.Chapter;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    
    List<Chapter> findByCourseIdOrderByOrderIndex(Long courseId);
    
    Optional<Chapter> findByIdAndCourseId(Long chapterId, Long courseId);
    
    Long countByCourseId(Long courseId);
    
    @Query("SELECT MAX(c.orderIndex) FROM Chapter c WHERE c.course.id = :courseId")
    Optional<Integer> findMaxOrderIndexByCourseId(@Param("courseId") Long courseId);

    List<Chapter> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    List<Chapter> findByCourseId(Long courseId);

void deleteByCourseId(Long courseId);

}

