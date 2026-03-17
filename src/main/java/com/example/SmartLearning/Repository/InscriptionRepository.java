package com.example.SmartLearning.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SmartLearning.model.Inscription;

import java.util.Optional;
import java.util.List;

public interface InscriptionRepository extends JpaRepository<Inscription, Long> {
    Optional<Inscription> findByApprenantIdAndCourseId(Long apprenantId, Long courseId);
    List<Inscription> findByApprenantId(Long apprenantId);
    boolean existsByApprenantIdAndCourseId(Long apprenantId, Long courseId);
}