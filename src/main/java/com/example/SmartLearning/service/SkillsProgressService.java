package com.example.SmartLearning.service;
import com.example.SmartLearning.DTO.EnrolledCourseDTO;
import com.example.SmartLearning.DTO.SkillProgressDTO.SkillCategoryDTO;
import com.example.SmartLearning.DTO.SkillProgressDTO.SkillsDashboardDTO;
import com.example.SmartLearning.Enum.Category;
import com.example.SmartLearning.Repository.ApprenantRepository;
import com.example.SmartLearning.Repository.CategorySkillProjection;
import com.example.SmartLearning.Repository.InscriptionRepository;
import com.example.SmartLearning.model.Apprenant;
import com.example.SmartLearning.model.Inscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillsProgressService {

    private final InscriptionRepository inscriptionRepository;
    private final ApprenantRepository   apprenantRepository;
    

   
    public SkillsDashboardDTO getDashboard(Long userId) {
        Apprenant apprenant = findApprenant(userId);
        Long apprenantId = apprenant.getId();

        List<CategorySkillProjection> rows =
            inscriptionRepository.aggregateSkillsByCategory(apprenantId);

        List<SkillCategoryDTO> skills = rows.stream()
            .map(this::projectionToCard)
            .sorted(Comparator.comparing(SkillCategoryDTO::getCategoryLabel))
            .collect(Collectors.toList());

        List<Inscription> all = inscriptionRepository.findByApprenantId(apprenantId);
        int overallPct   = computeOverall(all);
        int totalEnrolled  = all.size();
        int totalCompleted = (int) all.stream().filter(i -> i.getProgression() >= 100.0).count();

        return SkillsDashboardDTO.builder()
            .apprenantId(apprenantId)
            .learnerName(apprenant.getUser().getUsername())
            .overallProgressPercentage(overallPct)
            .totalEnrolledCourses(totalEnrolled)
            .totalCompletedCourses(totalCompleted)
            .skills(skills)
            .build();
    }

    
   public SkillCategoryDTO getCategoryProgress(Long userId, Category category) {
    Apprenant apprenant = findApprenant(userId);
    Long apprenantId = apprenant.getId();

    List<CategorySkillProjection> rows =
        inscriptionRepository.aggregateSkillsByCategory(apprenantId);

    return rows.stream()
        .filter(row -> row.getCategory() == category)
        .findFirst()
        .map(this::projectionToCard)
        .orElse(emptyCard(category));
}

    
    private SkillCategoryDTO projectionToCard(CategorySkillProjection row) {
        int avgPct = row.getAvgProgression() == null ? 0
                     : (int) Math.round(row.getAvgProgression());
        return SkillCategoryDTO.builder()
            .category(row.getCategory())
            .categoryLabel(row.getCategory().getLabel())
            .progressPercentage(avgPct)
            .level(resolveLevel(avgPct))
            .enrolledCourses(row.getEnrolledCourses() == null ? 0 : row.getEnrolledCourses().intValue())
            .completedCourses(row.getCompletedCourses() == null ? 0 : row.getCompletedCourses().intValue())
            .build();
    }


    
    private int computeOverall(List<Inscription> inscriptions) {
        if (inscriptions.isEmpty()) return 0;
        return (int) Math.round(
            inscriptions.stream()
                .mapToDouble(Inscription::getProgression)
                .average()
                .orElse(0.0)
        );
    }

    private SkillCategoryDTO emptyCard(Category category) {
        return SkillCategoryDTO.builder()
            .category(category)
            .categoryLabel(category.getLabel())
            .progressPercentage(0)
            .level(resolveLevel(0))
            .enrolledCourses(0)
            .completedCourses(0)
            .build();
    }

    

    private Apprenant findApprenant(Long userId) {
        return apprenantRepository.findByUser_Id(userId)
            .orElseThrow(() -> new NoSuchElementException("Apprenant not found for userId: " + userId));
    }

    
    private String resolveLevel(int pct) {
        if (pct <= 0)  return "Not Started";
        if (pct < 35)  return "Beginner";
        if (pct < 65)  return "Intermediate";
        if (pct < 85)  return "Advanced";
        return "Expert";
    }

     public List<EnrolledCourseDTO> getEnrolledCourses(Long userId) {
    Apprenant apprenant = findApprenant(userId);

    return inscriptionRepository
        .findEnrolledCoursesByApprenant(apprenant.getId())
        .stream()
        .map(i -> EnrolledCourseDTO.builder()
            .courseId(i.getCourse().getId())
            .title(i.getCourse().getTitle())
            .description(i.getCourse().getDescription())
            .category(i.getCourse().getCategory().getLabel())
            .level(i.getCourse().getLevel().name())
            .price(i.getCourse().getPrice())
            .courseImageUrl(i.getCourse().getImageUrl())
            .progression((int) Math.round(i.getProgression()))
            .build()
        )
        .collect(Collectors.toList());
}

   
}