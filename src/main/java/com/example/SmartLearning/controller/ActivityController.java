package com.example.SmartLearning.controller;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import com.example.SmartLearning.DTO.ActivityDTO;
import com.example.SmartLearning.security.JwtUserPrincipal;
import com.example.SmartLearning.service.ActivityService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/learner/activities")
@PreAuthorize("hasRole('APPRENANT')")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService    activityService;

    @GetMapping
    public ResponseEntity<List<ActivityDTO>> getRecentActivities(Authentication authentication) {
        JwtUserPrincipal principal = (JwtUserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(activityService.getRecentActivities(principal.getId()));
    }
}