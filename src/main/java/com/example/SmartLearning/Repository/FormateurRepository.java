package com.example.SmartLearning.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.SmartLearning.model.Formateur;

@Repository
public interface FormateurRepository extends JpaRepository<Formateur, Long> {

    // Changed from findByUserUsername to findByUsername
    Optional<Formateur> findByUsername(String username);

    // Changed from existsByUserUsername to existsByUsername
    boolean existsByUsername(String username);

    // Removed findByUser_Id because JpaRepository already provides findById(Long id)!
    // If you really want to keep it written out, it would just be:
    // Optional<Formateur> findById(Long id);
}