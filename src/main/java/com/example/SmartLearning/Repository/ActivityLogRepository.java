package com.example.SmartLearning.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.SmartLearning.Enum.ActivityType;
import com.example.SmartLearning.model.ActivityLog;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    @Query("""
        SELECT a FROM ActivityLog a
        WHERE a.apprenant.id = :apprenantId
        ORDER BY a.createdAt DESC
        LIMIT 10
    """)
    List<ActivityLog> findRecentByApprenant(@Param("apprenantId") Long apprenantId);

    @Query("""
    SELECT COUNT(a) > 0 FROM ActivityLog a
    WHERE a.apprenant.id = :apprenantId
      AND a.type = :type
      AND CAST(a.createdAt AS date) = CURRENT_DATE
    """)
    boolean existsTodayByType(@Param("apprenantId") Long apprenantId,
                            @Param("type") ActivityType type);
}