package com.example.SmartLearning.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.SmartLearning.DTO.BanRequest;
import com.example.SmartLearning.Enum.Role;
import com.example.SmartLearning.Repository.BannedEmailRepository;
import com.example.SmartLearning.Repository.CourseRepository;
import com.example.SmartLearning.Repository.InscriptionRepository;
import com.example.SmartLearning.Repository.StatsApprenantRepository;
import com.example.SmartLearning.Repository.StatsCoursRepository;
import com.example.SmartLearning.Repository.UserRepository;
import com.example.SmartLearning.model.User;
import com.example.SmartLearning.service.AdminService;
import com.example.SmartLearning.service.InscriptionService;
import com.example.SmartLearning.service.StatsComputationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserRepository userRepository;
    private final AdminService adminService; 
    private final BannedEmailRepository bannedEmailRepository; 
    private final InscriptionService inscriptionService;
    private final InscriptionRepository inscriptionRepository;
    private final CourseRepository courseRepository;
    private final StatsCoursRepository statsCoursRepository;
    private final StatsApprenantRepository statsApprenantRepository;
    private final StatsComputationService statsComputationService;

    @GetMapping
    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {

        if (user.isBanned() && user.getBanExpiresAt() != null
                && user.getBanExpiresAt().isBefore(LocalDateTime.now())) {
            user.setBanned(false);
            user.setBanExpiresAt(null);
            userRepository.save(user);
            bannedEmailRepository.deleteByEmail(user.getEmail());
        }
        if (!user.isBanned() && bannedEmailRepository.existsByEmail(user.getEmail())) {
            bannedEmailRepository.deleteByEmail(user.getEmail());
        }

            Map<String, Object> u = new HashMap<>();
            u.put("id", user.getId().toString());
            u.put("user_id", user.getId().toString());
            u.put("full_name", user.getUsername());          
            u.put("avatar_url", user.getPicture());   
            u.put("role", user.getRole().name());
            u.put("isBanned", user.isBanned()); 
            u.put("banExpiresAt", user.getBanExpiresAt() != null  
                ? user.getBanExpiresAt().toString()
                : null);
            return u;
        }).toList();
    }

    @PostMapping("/{userId}/ban")
    public ResponseEntity<?> banUser(@PathVariable Long userId , @RequestBody(required = false) BanRequest request) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.getRole() == Role.ADMIN) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Cannot ban an admin user"));
            }

            adminService.banUser(userId, request != null ? request.getDurationDays() : null);
            return ResponseEntity.ok()
                .body(Map.of("message", "User banned successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{userId}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unbanUser(@PathVariable Long userId) {
        adminService.unbanUser(userId);
        return ResponseEntity.ok("User unbanned successfully");
    }

    @GetMapping("/students")
    public List<Object[]> getInscriptionByCourse() {
        // Read from pre-computed StatsCours table — returns [courseId, count] for the course management table
        return statsCoursRepository.listInscriptionCountsByCourseId();
    }

    @GetMapping("/inscriptions")
    public Long getInscriptions() {
        Long fromStats = statsCoursRepository.sumTotalInscriptions();
        // Fall back to live count when StatsCours is empty or stale (all rows show 0)
        if (fromStats == null || fromStats == 0L) {
            return inscriptionRepository.countInscriptions();
        }
        return fromStats;
    }

    @GetMapping("/inscriptions/completed")
    public Long getCompletedInscriptions() {
        Long totalStats = statsCoursRepository.sumTotalInscriptions();
        // Fall back to live query when StatsCours has no meaningful data yet
        if (totalStats == null || totalStats == 0L) {
            return inscriptionRepository.countCompletedInscriptions();
        }
        Double raw = statsCoursRepository.sumTotalCompletionsRaw();
        return raw == null ? 0L : Math.round(raw);
    }
    @GetMapping("/enrollment-trends")
    public List<Object[]> getEnrollmentTrends(
        @RequestParam(defaultValue = "30") int days
    ) {
        LocalDate since = LocalDate.now().minusDays(days);
        return inscriptionRepository.getEnrollmentTrendsForAll(since);
    }
    @GetMapping("/inscription-counts")
    public List<Object[]> getInscriptionCounts() {
        // Read per-course enrollment counts from pre-computed StatsCours table
        return statsCoursRepository.listInscriptionCountsByCourse();
    }

    /**
     * Lightweight overview stats from pre-computed tables — replaces the heavy
     * /api/admin/courses full-load just to count courses.
     */
    @GetMapping("/stats/overview")
    public Map<String, Object> getStatsOverview() {
        Long totalCourses      = courseRepository.count();
        Long publishedCourses  = courseRepository.countByIsActive(true);
        Long totalEnrollments  = statsCoursRepository.sumTotalInscriptions();
        Double completionsRaw  = statsCoursRepository.sumTotalCompletionsRaw();
        Long totalLearners     = statsApprenantRepository.countApprenants();

        // Fall back to live counts when stats tables are empty
        if (totalEnrollments == null || totalEnrollments == 0L) {
            totalEnrollments = inscriptionRepository.countInscriptions();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalCourses",         totalCourses  == null ? 0L : totalCourses);
        result.put("publishedCourses",     publishedCourses == null ? 0L : publishedCourses);
        result.put("totalEnrollments",     totalEnrollments);
        result.put("completedEnrollments", completionsRaw == null ? 0L : Math.round(completionsRaw));
        result.put("totalLearners",        totalLearners == null ? 0L : totalLearners);
        return result;
    }

}