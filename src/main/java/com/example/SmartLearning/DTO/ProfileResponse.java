package com.example.SmartLearning.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String picture;
    private String role;
    private String provider;
    private String timezone;
    private List<String> loginHistory;
}