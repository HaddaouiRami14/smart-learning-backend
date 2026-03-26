package com.example.SmartLearning.DTO;
import lombok.Data;
import java.time.LocalDate;

import com.example.SmartLearning.Enum.Category;

@Data
public class InscriptionDTO {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String courseImageUrl;
    private Category courseCategory;
    private LocalDate dateInscription;
    private Double progression;
}