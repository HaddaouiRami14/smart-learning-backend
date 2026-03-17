package com.example.SmartLearning.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "banned_emails")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannedEmail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "banned_at", nullable = false)
    private LocalDateTime bannedAt;

    @Column(name = "reason")
    private String reason;

    @PrePersist
    protected void onCreate() {
        if (bannedAt == null) {
            bannedAt = LocalDateTime.now();
        }
    }
}