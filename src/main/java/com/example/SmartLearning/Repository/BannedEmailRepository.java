package com.example.SmartLearning.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SmartLearning.model.BannedEmail;

import jakarta.transaction.Transactional;

import java.util.Optional;

@Repository
public interface BannedEmailRepository extends JpaRepository<BannedEmail, Long> {
    Optional<BannedEmail> findByEmail(String email);
    boolean existsByEmail(String email);
    @Transactional 
    void deleteByEmail(String email);
}