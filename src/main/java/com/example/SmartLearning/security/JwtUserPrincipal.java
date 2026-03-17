package com.example.SmartLearning.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtUserPrincipal {
    private Long id;
    private Long formateurId;
    private String username;
    private String role;
}
