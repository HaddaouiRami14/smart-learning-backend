package com.example.SmartLearning.security;

import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import com.example.SmartLearning.Enum.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean validateToken(String token, String Username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(Username) && !isTokenExpired(token));
    }

    
    public String generateToken(Long userId,String Username, Role role) {
        return Jwts.builder()
        .subject(Username)
                                   
        .claim("role", role)                          
        .claim("id", userId)
        .issuedAt(new Date())                         
        .expiration(new Date(System.currentTimeMillis() + expiration)) 
        .signWith(getSigningKey(), Jwts.SIG.HS256)    
        .compact();

    }

    // Méthodes utilitaires pour le contrôleur
    public String getUsernameFromToken(String token) {
        return extractUsername(token);
    }

    public String getRoleFromToken(String token) {
        return extractRole(token);
    }
    public Long extractUserId(String token) {
    return extractClaim(token, claims -> claims.get("id", Long.class));
}

}
