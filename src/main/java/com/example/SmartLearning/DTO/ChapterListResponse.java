package com.example.SmartLearning.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterListResponse {

    private List<ChapterResponse> chapters;
    private Integer totalChapters;

}
