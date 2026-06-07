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
@Table(name = "stats_cours")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsCours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The course these stats belong to (unique — one row per course). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false, unique = true)
    private Course course;

    /**
     * The trainer (formateur) these stats concern.
     * Maps to: StatsCours "0..*" --> "1" Formateur : concerne
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formateur_id", nullable = false)
    private Formateur formateur;

    /** Total number of active enrollments for this course. */
    @Column(name = "nombre_inscriptions", nullable = false)
    private Integer nombreInscriptions;

    /** Percentage of enrolled learners who reached progression = 100 %. */
    @Column(name = "taux_completion_moyen", nullable = false)
    private Double tauxCompletionMoyen;

    /** Average progression (0–100) across all enrollments. */
    @Column(name = "progression_moyenne", nullable = false)
    private Double progressionMoyenne;

    /** Timestamp of the last recomputation. */
    @Column(name = "date_calcul", nullable = false)
    private LocalDateTime dateCalcul;
}
