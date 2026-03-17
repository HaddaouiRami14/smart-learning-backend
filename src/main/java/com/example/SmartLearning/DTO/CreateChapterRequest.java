package com.example.SmartLearning.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChapterRequest {
    private String title;
    private String content; // Contenu HTML de CKEditor
    private Integer orderIndex;

}
