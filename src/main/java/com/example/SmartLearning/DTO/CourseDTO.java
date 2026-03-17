package com.example.SmartLearning.DTO;

import java.time.LocalDateTime;

import com.example.SmartLearning.Enum.Level;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDTO {

private Long id;
 
 @NotBlank(message = "Title is required")
 @Size(min = 3, max = 200)
 private String title;
 
 @NotBlank(message = "Category is required")
 private String category;
 
 @NotNull(message = "Price is required")
 @DecimalMin(value = "0.0", inclusive = false)
 private Double price;
 
 @Size(max = 1000)
 private String description;
 
 
 private String imageUrl;
 
 //@Size(max = 500)
 //private String pdfUrl;
 
 private Long formateurId;
 private String formateurName;
 private Level level;
 
 private Boolean isActive;
 
 private LocalDateTime createdAt;
 private LocalDateTime updatedAt;

}
