package com.example.SmartLearning.Repository;

import com.example.SmartLearning.model.ChapterResource;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterResourceRepository extends JpaRepository<ChapterResource, Long> {
    
    List<ChapterResource> findByChapterId(Long chapterId);
    
    Optional<ChapterResource> findByIdAndChapterId(Long resourceId, Long chapterId);
    
    void deleteByIdAndChapterId(Long resourceId, Long chapterId);
    
void deleteByChapterId(Long chapterId);

}
