package com.example.SmartLearning.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import com.example.SmartLearning.Enum.AuthProvider;
import com.example.SmartLearning.Enum.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    
    @Column(unique = true, nullable = false, length = 150)
    private String email;
    
    @Column(nullable = true)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;
    
    private String googleId;
    private String picture;

    @Column(length = 50)
    private String timezone;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "banned", nullable = false)
    private boolean banned = false;

    @Column(name = "ban_expires_at")
    private LocalDateTime banExpiresAt;
}