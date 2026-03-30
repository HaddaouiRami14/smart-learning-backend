package com.example.SmartLearning.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@Table(name = "formateurs")
@EqualsAndHashCode(callSuper = true)
public class Formateur extends User { 
 
    private String bio;
    
    private String specialization;
 
    @OneToMany(mappedBy = "formateur", cascade = CascadeType.ALL)
    private List<Course> courses;
}