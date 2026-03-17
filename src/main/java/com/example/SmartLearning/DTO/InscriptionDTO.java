package com.example.SmartLearning.DTO;
import lombok.Data;
import java.time.LocalDate;

@Data
public class InscriptionDTO {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String courseImageUrl;
    private String courseCategory;
    private LocalDate dateInscription;
    private Double progression;
}