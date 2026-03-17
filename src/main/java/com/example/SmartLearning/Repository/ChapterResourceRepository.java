package com.example.SmartLearning.Repository;

import com.example.SmartLearning.model.ChapterResource;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterResourceRepository extends JpaRepository<ChapterResource, Long> {
    
    // Trouver toutes les ressources d'un chapitre
    List<ChapterResource> findByChapterId(Long chapterId);
    
    // Trouver une ressource spécifique
    Optional<ChapterResource> findByIdAndChapterId(Long resourceId, Long chapterId);
    
    // Supprimer une ressource
    void deleteByIdAndChapterId(Long resourceId, Long chapterId);
    /*@Modifying
    @Query(value = "DELETE FROM chapter_resources WHERE chapter_id = :chapterId", nativeQuery = true)
    void deleteByChapterId(@Param("chapterId") Long chapterId);
}*/
void deleteByChapterId(Long chapterId);

}
