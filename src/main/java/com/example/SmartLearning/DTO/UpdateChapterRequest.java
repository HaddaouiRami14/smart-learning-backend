package com.example.SmartLearning.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateChapterRequest {
    private String title;
    private String content;
    private Integer orderIndex;

}
