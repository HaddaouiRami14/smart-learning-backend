package com.example.SmartLearning.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.SmartLearning.Repository.BannedEmailRepository;
import com.example.SmartLearning.Repository.FormateurRepository;
import com.example.SmartLearning.Repository.UserRepository; 
import com.example.SmartLearning.model.Formateur;
import com.example.SmartLearning.model.User; 

import java.io.IOException;
import java.time.LocalDateTime; 
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final FormateurRepository formateurRepository;
    private final UserRepository userRepository;
    private final BannedEmailRepository bannedEmailRepository; // ✅ Ajouter

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        username = jwtUtil.extractUsername(jwt);

        //if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        if (username != null ) {

            if (jwtUtil.validateToken(jwt, username)) {
                Long userId = jwtUtil.extractUserId(jwt);
                String role = jwtUtil.extractRole(jwt);

                User user = userRepository.findById(userId).orElse(null);

                if (user != null && user.isBanned()) {
                    if (user.getBanExpiresAt() != null
                            && user.getBanExpiresAt().isBefore(LocalDateTime.now())) {
                        // ✅ Ban expiré → lever le ban ET supprimer l'email de banned_email
                        user.setBanned(false);
                        user.setBanExpiresAt(null);
                        userRepository.save(user);
                        bannedEmailRepository.deleteByEmail(user.getEmail()); // ✅ Ajouter
                    } else {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write(
                            user.getBanExpiresAt() != null
                                ? "{\"error\": \"Account banned until " + user.getBanExpiresAt() + "\"}"
                                : "{\"error\": \"Account permanently banned\"}"
                        );
                        return;
                    }
                }

                Long formateurId = formateurRepository.findByUser_Id(userId)
                        .map(Formateur::getId)
                        .orElseGet(() -> formateurRepository
                                .findByUserUsername(username)
                                .map(Formateur::getId)
                                .orElse(null));

                JwtUserPrincipal principal = new JwtUserPrincipal(userId, formateurId, username, role);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
    