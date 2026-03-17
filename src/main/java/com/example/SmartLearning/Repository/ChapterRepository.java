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
    
    // Trouver tous les chapitres d'un cours
    List<Chapter> findByCourseIdOrderByOrderIndex(Long courseId);
    
    // Trouver un chapitre spécifique
    Optional<Chapter> findByIdAndCourseId(Long chapterId, Long courseId);
    
    // Compter les chapitres d'un cours
    Long countByCourseId(Long courseId);
    
    // Trouver le dernier ordre pour un cours
    @Query("SELECT MAX(c.orderIndex) FROM Chapter c WHERE c.course.id = :courseId")
    Optional<Integer> findMaxOrderIndexByCourseId(@Param("courseId") Long courseId);

    List<Chapter> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    List<Chapter> findByCourseId(Long courseId);
/*@Modifying
@Query(value = "DELETE FROM chapters WHERE course_id = :courseId", nativeQuery = true)
void deleteByCourseId(@Param("courseId") Long courseId);

@Modifying
@Query(value = "DELETE FROM chapters WHERE id = :id", nativeQuery = true)
void deleteChapterDirectly(@Param("id") Long id); */ 
void deleteByCourseId(Long courseId);

}

