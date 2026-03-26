package com.example.SmartLearning.service;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.SmartLearning.DTO.LoginRequest;
import com.example.SmartLearning.DTO.LoginResponse;
import com.example.SmartLearning.Enum.Role;
import com.example.SmartLearning.Repository.ApprenantRepository;
import com.example.SmartLearning.Repository.BannedEmailRepository;
import com.example.SmartLearning.Repository.FormateurRepository;
import com.example.SmartLearning.Repository.LoginHistoryRepository;
import com.example.SmartLearning.Repository.UserRepository;
import com.example.SmartLearning.model.Apprenant;
import com.example.SmartLearning.model.Formateur;
import com.example.SmartLearning.model.LoginHistory;
import com.example.SmartLearning.model.User;
import com.example.SmartLearning.security.JwtUtil;

@Service
public class AuthService {
    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private  BannedEmailRepository bannedEmailRepository;

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    @Autowired
    private  PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private FormateurRepository formateurRepository;

    @Autowired
    private ApprenantRepository apprenantRepository;

    @Autowired
    private StreakService streakService; 


 public LoginResponse login(LoginRequest loginRequest) {
        try {

            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("Invalid username or password"));


            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return new LoginResponse("Invalid username or password");
            }

            if (bannedEmailRepository.existsByEmail(user.getEmail())) {
            
            if (user.getBanExpiresAt() != null
                    && user.getBanExpiresAt().isBefore(LocalDateTime.now())) {
                
                bannedEmailRepository.deleteByEmail(user.getEmail());
            } else if (user.isBanned()) {
                
                String message = user.getBanExpiresAt() != null
                        ? "Account banned until " + user.getBanExpiresAt()
                        : "Account permanently banned";
                return new LoginResponse(message);
            } else {
                
                bannedEmailRepository.deleteByEmail(user.getEmail());
            }
        }

            
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);


            LoginHistory loginHistory = LoginHistory.builder()
                .user(user)
                .loginTime(LocalDateTime.now())
                .ipAddress("Web Login") 
                .userAgent("Browser") 
                .build();
            loginHistoryRepository.save(loginHistory);
            streakService.checkAndLogStreakMilestone(user);
            

            String token = jwtUtil.generateToken(user.getId(),user.getUsername(), user.getRole());


            User userDto  = User.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

            return new LoginResponse(token, userDto, "Login successful");

        } catch (Exception e) {
            return new LoginResponse("Invalid username or password");
        }
    }


    public Map<String, Object> register(String username, String email, String password , Role role) {
    if (userRepository.findByEmail(email).isPresent()) {
        throw new RuntimeException("Un utilisateur avec cet email existe déjà");
    }

    User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setTimezone("UTC"); 
        if (bannedEmailRepository.existsByEmail(email)) {
            throw new RuntimeException("Email banned");
        }
        userRepository.save(user);
        if (role == Role.FORMATEUR) {
            Formateur formateur = new Formateur();
            formateur.setUser(user);
            formateur.setBio("");
            formateur.setSpecialization("");
            formateurRepository.save(formateur);
        }
        if (role == Role.APPRENANT) {
            Apprenant apprenant = new Apprenant();
            apprenant.setUser(user);
            apprenantRepository.save(apprenant);
        }

        
        LoginHistory loginHistory = LoginHistory.builder()
            .user(user)
            .loginTime(LocalDateTime.now())
            .ipAddress("Registration")
            .userAgent("Browser")
            .build();
        loginHistoryRepository.save(loginHistory);

    String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

    
    Map<String, Object> userMap = Map.of(
        "id", user.getId(),
        "username", user.getUsername(),
        "email", user.getEmail(),
        "role", user.getRole().name()
    );

    return Map.of(
        "message", "Inscription réussie",
        "token", token,
        "user", userMap
    );
}





}
