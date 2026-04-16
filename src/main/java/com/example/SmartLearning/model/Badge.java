package com.example.SmartLearning.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "badges")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // e.g. "FIRST_STEP", "QUIZ_MASTER"

    @Column(nullable = false)
    private String name; // e.g. "First Step"

    @Column(nullable = false)
    private String description; // e.g. "Enroll in your first course"

    @Column(nullable = false)
    private String icon; // emoji or icon name e.g. "🎯"

    @Column(nullable = false)
    private String color; // e.g. "gold", "silver", "blue"
}