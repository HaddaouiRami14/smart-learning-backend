package com.example.SmartLearning.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "inscription")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apprenant_id", nullable = false)
    private Apprenant apprenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private LocalDate dateInscription;

    @Column(nullable = false)
    private Double progression = 0.0;
    
    // ✅ NEW: Track completed items (format: "chapterId:Q" for quiz, "chapterId:E" for exercise)
    @Column(columnDefinition = "TEXT")
    private String completedItems = "";
}