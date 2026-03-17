package com.example.SmartLearning.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormateurProfileResponse {

    private Long id;
    private String username;
    private String email;
    private String picture;
    private String bio;
    private String specialization;
    private String provider;
}