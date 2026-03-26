package com.example.SmartLearning.service;
import java.time.LocalDateTime;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.SmartLearning.DTO.LoginResponse;
import com.example.SmartLearning.Enum.AuthProvider;
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
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;

@Service
public class GoogleAuthService {
    
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired private LoginHistoryRepository loginHistoryRepository;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService; 

    @Autowired
    private  FormateurRepository formateurRepository;

    @Autowired
    private ApprenantRepository apprenantRepository;

     @Autowired private BannedEmailRepository bannedEmailRepository;
    
     public LoginResponse authenticateGoogleUser(String idToken, Role requestedRole) {
        System.out.println("googleClientId : " + googleClientId); 
        System.out.println("idToken reçu dans service : " + idToken);
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), 
                new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
            
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            
            if (googleIdToken != null) {
                GoogleIdToken.Payload payload = googleIdToken.getPayload();
                
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String googleId = payload.getSubject();
                String picture = (String) payload.get("picture");

                boolean userExists = userRepository.findByEmail(email).isPresent();
                
                User user = userRepository.findByEmail(email)
                    .orElseGet(() -> createGoogleUser(email, name, googleId, picture, requestedRole));

                        
                    if (bannedEmailRepository.existsByEmail(user.getEmail())) {
                    if (user.getBanExpiresAt() != null && user.getBanExpiresAt().isBefore(LocalDateTime.now())) {
                        bannedEmailRepository.deleteByEmail(user.getEmail());
                        user.setBanned(false);
                        userRepository.save(user);
                    } else{
                        String message = user.getBanExpiresAt() != null
                                ? "Account banned until " + user.getBanExpiresAt()
                                : "Account permanently banned";
                        throw new RuntimeException(message);
                    }
                }
                
                if (user.getProvider() == null || user.getProvider() == AuthProvider.LOCAL) {
                    user.setProvider(AuthProvider.GOOGLE);
                    user.setGoogleId(googleId);
                    user.setPicture(picture);
                    user = userRepository.save(user);
                }

                user.setLastLogin(LocalDateTime.now());
                user = userRepository.save(user);
                
                
                
                LoginHistory loginHistory = LoginHistory.builder()
                    .user(user)
                    .loginTime(LocalDateTime.now())
                    .ipAddress("Google OAuth")
                    .userAgent("Browser")
                    .build();
                loginHistoryRepository.save(loginHistory);
                

                    
                if (!userExists) {
                    emailService.sendWelcomeEmailHTML(user );
                    
                }
                
                String jwt = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
                
                User userResponse = User.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .provider(user.getProvider())
                    .picture(user.getPicture())
                    .build();
                
                return LoginResponse.builder()
                    .token(jwt)
                    .user(userResponse)
                    .newUser(!userExists) 
                    .build();
            }
            
            throw new RuntimeException("Invalid Google token");
            
        } catch (Exception e) {
            throw new RuntimeException("Google authentication failed: " + e.getMessage(), e);
        }
    }
    
    private User createGoogleUser(String email, String name, String googleId, String picture, Role requestedRole) {
        Role userRole = determineUserRole(requestedRole);
        
        User newUser = User.builder()
            .email(email)
            .username(name != null ? name : email.split("@")[0])
            .googleId(googleId)
            .picture(picture)
            .provider(AuthProvider.GOOGLE)
            .role(userRole)
            .password(null)
            .timezone("UTC") 
            .build();
            if (bannedEmailRepository.existsByEmail(email)) {
            throw new RuntimeException("Email banned");
        }
        
        User savedUser = userRepository.save(newUser); 
        

        if (userRole == Role.FORMATEUR) {              
            
            Formateur formateur = new Formateur();
            formateur.setUser(savedUser);
            formateur.setBio("");
            formateur.setSpecialization("");
            formateurRepository.save(formateur);
        }
        if (userRole == Role.APPRENANT) {              
            Apprenant apprenant = new Apprenant();
            apprenant.setUser(savedUser);
            apprenantRepository.save(apprenant);
        }

        return savedUser; 

       

    }
    
    private Role determineUserRole(Role requestedRole) {
        if (requestedRole == Role.APPRENANT || requestedRole == Role.FORMATEUR) {
            return requestedRole;
        }
        
        
        return Role.APPRENANT;
    }
}