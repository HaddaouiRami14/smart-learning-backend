package com.example.SmartLearning.controller;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.example.SmartLearning.DTO.GoogleAuthRequest;
import com.example.SmartLearning.DTO.LoginRequest;
import com.example.SmartLearning.DTO.LoginResponse;
import com.example.SmartLearning.DTO.RegisterRequest;
import com.example.SmartLearning.Enum.Role;
import com.example.SmartLearning.Repository.UserRepository;
import com.example.SmartLearning.model.User;
import com.example.SmartLearning.security.JwtUtil;
import com.example.SmartLearning.service.AuthService;
import com.example.SmartLearning.service.GoogleAuthService;


import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {


    @Autowired
    private AuthService authService;

    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/google")
    public ResponseEntity<?> authenticateGoogle(@RequestBody GoogleAuthRequest request ) {
        System.out.println("idToken reçu : " + request.getIdToken());
        System.out.println("role reçu : " + request.getRole());
       try {
        LoginResponse response = googleAuthService.authenticateGoogleUser(
            request.getIdToken(),
            request.getRole()   
        );
        return ResponseEntity.ok(response);

    } catch (RuntimeException e) {
        String message = e.getMessage() != null
                ? e.getMessage().replace("Google authentication failed: ", "")
                : "Authentication failed";

        boolean isBanned = message.contains("banned");
        return ResponseEntity
                .status(isBanned ? HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", message));

    } catch (Exception e) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", "An error occurred during authentication"));
    }
    }


    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @RequestBody RegisterRequest request
    ) {
        try {
            Role role = Role.valueOf(request.getRole().name());
            Map<String, Object> response = authService.register(request.getUsername(),request.getEmail(),request.getPassword(),role);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);

        if (response.getToken() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.extractUserId(token); // selon ton JwtUtil

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(Map.of(
            "id",    user.getId(),
            "name",  user.getUsername(),
            "email", user.getEmail(),
            "role",  user.getRole()
        ));
    }


  

}
