package com.example.SmartLearning.controller;
import com.example.SmartLearning.Repository.UserRepository;
import com.example.SmartLearning.model.User;
import com.example.SmartLearning.security.JwtUserPrincipal;
import com.example.SmartLearning.service.StreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/streak")
@PreAuthorize("hasRole('APPRENANT')")
@RequiredArgsConstructor
public class StreakController {

    private final StreakService streakService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getMyStreak() {

        // ✅ Récupère l'email depuis le token JWT
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

         // ✅ Caster vers JwtUserPrincipal pour récupérer l'ID directement
        JwtUserPrincipal principal = (JwtUserPrincipal) authentication.getPrincipal();

        User currentUser = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        int streak = streakService.calculateStreak(currentUser);
        boolean[] last7Days = streakService.getLast7DaysActivity(currentUser);

        return ResponseEntity.ok(Map.of(
                "currentStreak", streak,
                "last7Days", last7Days
        ));
    }
}