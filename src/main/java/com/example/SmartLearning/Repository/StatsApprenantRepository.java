package com.example.SmartLearning.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.SmartLearning.model.StatsApprenant;

public interface StatsApprenantRepository extends JpaRepository<StatsApprenant, Long> {

    /** Find the pre-computed stats row for one learner. */
    Optional<StatsApprenant> findByApprenantId(Long apprenantId);

    /** Delete stats row if a learner account is removed. */
    void deleteByApprenantId(Long apprenantId);

    /** Total number of learners that have a stats row. */
    @Query("SELECT COUNT(s) FROM StatsApprenant s")
    Long countApprenants();
}
