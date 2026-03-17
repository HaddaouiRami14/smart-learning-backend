package com.example.SmartLearning.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.SmartLearning.model.Formateur;

@Repository
public interface FormateurRepository extends JpaRepository<Formateur, Long> {

    Optional<Formateur> findByUserUsername(String username);

    boolean existsByUserUsername(String username);

    Optional<Formateur> findByUser_Id(Long userId);
}
