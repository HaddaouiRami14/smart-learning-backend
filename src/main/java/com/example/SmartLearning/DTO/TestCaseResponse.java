package com.example.SmartLearning.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseResponse {
    private Long id;
    private String input;
    private String expectedOutput;
    private Boolean isHidden;
    private Integer orderIndex;
}