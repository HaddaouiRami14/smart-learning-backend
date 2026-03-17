package com.example.SmartLearning.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterResourceResponse {

    private Long id;
    private String fileName;
    private String resourceType;
    private String mimeType;
    private Long fileSize;
    private LocalDateTime createdAt;

}
