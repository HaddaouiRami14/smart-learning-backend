package com.example.SmartLearning.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseSubmission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apprenant_id", nullable = false)
    private Apprenant apprenant;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String submittedCode;
    
    @Column(nullable = false)
    private Integer testsPassed;
    
    @Column(nullable = false)
    private Integer totalTests;
    
    @Column(nullable = false)
    private Integer score; // points earned
    
    @Column(nullable = false)
    private Boolean passed;
    
    @Column
    private Double executionTime; // in seconds
    
    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();
}