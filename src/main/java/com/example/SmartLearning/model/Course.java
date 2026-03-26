package com.example.SmartLearning.model;

import java.time.LocalDateTime;
import java.util.List;

import com.example.SmartLearning.Enum.Category;
import com.example.SmartLearning.Enum.Level;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "courses")
public class Course {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 
 @Column(nullable = false)
 private String title;
 
 @Column(columnDefinition = "TEXT")
 private String description;
 
 @Enumerated(EnumType.STRING)
 @Column(nullable = false)
 private Category category;
 
 @Column(nullable = false)
 private Double price;

@Enumerated(EnumType.STRING)
@Column(nullable = false)
private Level level = Level.BEGINNER;

@OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
@JsonIgnore // IMPORTANT : Empêche la boucle infinie (Course -> Inscription -> Course) lors de la sérialisation JSON
private List<Inscription> inscriptions; 
 
 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "formateur_id", nullable = false)
 private Formateur formateur;

 @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true , fetch = FetchType.EAGER)
@JsonManagedReference
private List<Chapter> chapters;

 
 @Column(name = "image_url", columnDefinition = "TEXT")
 private String imageUrl;
 
 @Column(name = "is_active", nullable = false)
 private Boolean isActive = true;
 
 @Column(name = "created_at", nullable = false, updatable = false)
 private LocalDateTime createdAt;
 
 @Column(name = "updated_at")
 private LocalDateTime updatedAt;
 
 @PrePersist
 protected void onCreate() {
 createdAt = LocalDateTime.now();
 updatedAt = LocalDateTime.now();
 }
 
 @PreUpdate
 protected void onUpdate() {
 updatedAt = LocalDateTime.now();
 }
}