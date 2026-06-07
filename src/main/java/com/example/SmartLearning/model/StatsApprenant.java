package com.example.SmartLearning.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stats_apprenant")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsApprenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The learner these stats belong to (unique — one row per apprenant). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apprenant_id", nullable = false, unique = true)
    private Apprenant apprenant;

    /** Total number of courses this learner is enrolled in. */
    @Column(name = "cours_inscrits", nullable = false)
    private Integer coursInscrits;

    /** Number of courses where progression = 100 %. */
    @Column(name = "cours_completes", nullable = false)
    private Integer coursCompletes;

    /** Average progression (0–100) across all enrollments. */
    @Column(name = "progression_moyenne", nullable = false)
    private Double progressionMoyenne;

    /** Timestamp of the last recomputation. */
    @Column(name = "date_calcul", nullable = false)
    private LocalDateTime dateCalcul;
}
