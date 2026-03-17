package com.example.SmartLearning.model;

    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import com.fasterxml.jackson.annotation.JsonBackReference;

    import java.time.LocalDateTime;
    import java.util.ArrayList;
    import java.util.List;

    @Entity
    @Table(name = "quizzes")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class Quiz {
        
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        
        @Column(nullable = false)
        private String title;
        
        @Column(columnDefinition = "TEXT")
        private String description;
        
        @Column(nullable = false)
        private Integer passingScore = 70;
        
        @OneToOne
        @JoinColumn(name = "chapter_id", nullable = false, unique = true)
        @JsonBackReference
        private Chapter chapter;
        
        @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Question> questions = new ArrayList<>();
        
        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt = LocalDateTime.now();
        
        @Column(name = "updated_at")
        private LocalDateTime updatedAt = LocalDateTime.now();
        
        @PreUpdate
        public void preUpdate() {
            this.updatedAt = LocalDateTime.now();
        }
    }