package com.example.SmartLearning.Repository;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.example.SmartLearning.model.CourseRecommendation;

import jakarta.transaction.Transactional;

public interface CourseRecommendationRepository extends JpaRepository<CourseRecommendation, Long> {

    List<CourseRecommendation> findByApprenantIdOrderByScoreDesc(Long apprenantId);

    Set<CourseRecommendation> findByApprenantIdAndRecommendedTrue(Long apprenantId);

    @Modifying
    @Transactional
    void deleteByApprenantId(Long apprenantId);
}
