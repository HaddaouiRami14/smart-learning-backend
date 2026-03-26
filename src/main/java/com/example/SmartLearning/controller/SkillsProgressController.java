package com.example.SmartLearning.controller;

import com.example.SmartLearning.DTO.SkillProgressDTO.SkillCategoryDTO;
import com.example.SmartLearning.DTO.SkillProgressDTO.SkillsDashboardDTO;
import com.example.SmartLearning.Enum.Category;
import com.example.SmartLearning.security.JwtUserPrincipal;
import com.example.SmartLearning.service.SkillsProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/learner/skills")
@PreAuthorize("hasRole('APPRENANT')")
@RequiredArgsConstructor
public class SkillsProgressController {

    private final SkillsProgressService skillsProgressService;

    
    @GetMapping
    public ResponseEntity<SkillsDashboardDTO> getDashboard() {
        Long userId = resolveUserId();
        return ResponseEntity.ok(skillsProgressService.getDashboard(userId));
    }

    
    @GetMapping("/{category}")
    public ResponseEntity<SkillCategoryDTO> getCategoryProgress(
        @PathVariable Category category
    ) {
        Long userId = resolveUserId();
        return ResponseEntity.ok(skillsProgressService.getCategoryProgress(userId, category));
    }

    private Long resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user in context");
        }

        JwtUserPrincipal principal = (JwtUserPrincipal) auth.getPrincipal();
        return principal.getId();
}
}