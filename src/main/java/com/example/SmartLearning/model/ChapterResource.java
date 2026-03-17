package com.example.SmartLearning.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.SmartLearning.Enum.ResourceType;
import com.fasterxml.jackson.annotation.JsonBackReference;

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
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "chapter_resources")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterResource {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fileName; 
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType; 
    
    @Column(columnDefinition = "bytea")
    private byte[] fileData; 
    
    @Column(nullable = false)
    private String mimeType; 
    
    @Column(nullable = false)
    private Long fileSize; 
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    @JsonBackReference
    private Chapter chapter;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "uploaded_by")
    private Long uploadedBy; 
    
}
