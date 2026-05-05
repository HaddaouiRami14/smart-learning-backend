package com.example.SmartLearning.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SmartLearning.Enum.Role;
import com.example.SmartLearning.model.User;

public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByRole(Role role);
    Optional<User> findByGoogleId(String googleId);
    long countByRole(Role role);
    
}
