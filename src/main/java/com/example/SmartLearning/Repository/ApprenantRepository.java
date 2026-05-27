package com.example.SmartLearning.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.SmartLearning.model.Apprenant;

public interface ApprenantRepository extends JpaRepository<Apprenant, Long> {

    // Changed from findByUserUsername to findByUsername
    Optional<Apprenant> findByUsername(String username);

    // Changed from existsByUserUsername to existsByUsername
    boolean existsByUsername(String username);


    Optional<Apprenant> findByEmail(String email);
}