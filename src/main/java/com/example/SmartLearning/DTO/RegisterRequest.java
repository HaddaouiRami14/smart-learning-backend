package com.example.SmartLearning.DTO;

import com.example.SmartLearning.Enum.Role;

import lombok.Data;

@Data
public class RegisterRequest {

    private String username;
    private String email;
    private String password;
    private Role role;

}
