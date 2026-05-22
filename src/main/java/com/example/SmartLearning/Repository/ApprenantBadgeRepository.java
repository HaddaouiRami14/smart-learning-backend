package com.example.SmartLearning.Repository;

import com.example.SmartLearning.model.ApprenantBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;

public interface ApprenantBadgeRepository extends JpaRepository<ApprenantBadge, Long> {

    List<ApprenantBadge> findByApprenantId(Long apprenantId);

    boolean existsByApprenantIdAndBadgeCode(Long apprenantId, String badgeCode);

    
    @Query("""
        SELECT ab.apprenant.id, COUNT(ab)
        FROM ApprenantBadge ab
        GROUP BY ab.apprenant.id
        ORDER BY COUNT(ab) DESC
    """)
    List<Object[]> countBadgesPerApprenant();
}