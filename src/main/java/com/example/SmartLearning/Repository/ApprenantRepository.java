package com.example.SmartLearning.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.SmartLearning.model.Apprenant;

public interface ApprenantRepository extends JpaRepository<Apprenant, Long> {

    Optional<Apprenant> findByUserUsername(String username);

    boolean existsByUserUsername(String username);

    Optional<Apprenant> findByUser_Id(Long userId);
}
