package com.example.SmartLearning.configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.SmartLearning.Repository.BannedEmailRepository;
import com.example.SmartLearning.Repository.LoginHistoryRepository;
import com.example.SmartLearning.Repository.UserRepository;
import com.example.SmartLearning.model.LoginHistory;
import com.example.SmartLearning.security.JwtAuthenticationFilter;
import com.example.SmartLearning.security.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired private BannedEmailRepository bannedEmailRepository;

    @Autowired 
    private LoginHistoryRepository loginHistoryRepository;


    @Bean
    public OncePerRequestFilter securityHeadersFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, 
                                          HttpServletResponse response, 
                                          FilterChain filterChain) 
                    throws ServletException, IOException {
                
                // Set headers to allow Google OAuth popups
                response.setHeader("Cross-Origin-Opener-Policy", "unsafe-none");
                response.setHeader("Cross-Origin-Embedder-Policy", "unsafe-none");
                
                filterChain.doFilter(request, response);
            }
        };
    }


    @Bean
    public AuthenticationSuccessHandler oauth2SuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            String email = oauth2Token.getPrincipal().getAttribute("email");

            if (email == null) {
                response.sendRedirect("http://localhost:5173/login?error=no_email");
                return;
            }

            userRepository.findByEmail(email).ifPresentOrElse(
                user -> {
                    try {
                        
                    if (bannedEmailRepository.existsByEmail(user.getEmail())) {
                        if (user.getBanExpiresAt() != null 
                                && user.getBanExpiresAt().isBefore(LocalDateTime.now())) {
                            bannedEmailRepository.deleteByEmail(user.getEmail());
                        } else {
                            String banMsg = user.getBanExpiresAt() != null
                                ? "banned_until_" + user.getBanExpiresAt()
                                : "permanently_banned";
                            response.sendRedirect("http://localhost:5173/login?error=" + banMsg);
                            return;
                        }
                    }

                        // ✅ Enregistre le LoginHistory à chaque login Google
                    LoginHistory loginHistory = LoginHistory.builder()
                            .user(user)
                            .loginTime(LocalDateTime.now())
                            .ipAddress(request.getRemoteAddr()) // ✅ IP réelle
                            .userAgent(request.getHeader("User-Agent")) // ✅ User-Agent réel
                            .build();
                    loginHistoryRepository.save(loginHistory);


                        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
                        response.sendRedirect("http://localhost:5173/oauth2/callback?token=" + token +
                                "&userId=" + user.getId() +
                                "&name=" + user.getUsername() +
                                "&email=" + user.getEmail() +
                                "&role=" + user.getRole());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                () -> {
                    try {
                        response.sendRedirect("http://localhost:5173/login?error=user_not_found");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            );
        };
    }





    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) 
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**","/api/oauth2/**", "/api/password/**", "/login/oauth2/**", "/oauth2/**","/api/courses/**").permitAll()
                
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            // Configuration OAuth2 Login
            .oauth2Login(oauth2 -> oauth2
                 
                .successHandler(oauth2SuccessHandler())
            );

            

         // Add security headers filter first
        http.addFilterBefore(securityHeadersFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:8080"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS" , "PATCH"));
        config.setAllowedHeaders(List.of("*")); 
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

@Bean
public HttpFirewall httpFirewall() {
    StrictHttpFirewall firewall = new StrictHttpFirewall();
    firewall.setAllowedHttpMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    firewall.setAllowSemicolon(true);
    return firewall;
}

}
