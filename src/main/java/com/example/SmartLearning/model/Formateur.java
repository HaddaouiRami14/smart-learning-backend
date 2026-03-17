package com.example.SmartLearning.model;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "formateurs")
public class Formateur {

@Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 
 @OneToOne
 @JoinColumn(name = "user_id", nullable = false)
 private User user;
 
 private String bio;
 private String specialization;
 
 @OneToMany(mappedBy = "formateur", cascade = CascadeType.ALL)
 private List<Course> courses;

}
