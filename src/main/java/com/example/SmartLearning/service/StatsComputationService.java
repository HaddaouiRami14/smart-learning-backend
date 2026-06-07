package com.example.SmartLearning.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartLearning.Repository.ApprenantRepository;
import com.example.SmartLearning.Repository.CourseRepository;
import com.example.SmartLearning.Repository.InscriptionRepository;
import com.example.SmartLearning.Repository.StatsApprenantRepository;
import com.example.SmartLearning.Repository.StatsCoursRepository;
import com.example.SmartLearning.model.Apprenant;
import com.example.SmartLearning.model.Course;
import com.example.SmartLearning.model.Inscription;
import com.example.SmartLearning.model.StatsApprenant;
import com.example.SmartLearning.model.StatsCours;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Computes and upserts pre-aggregated stats rows whenever an enrollment or
 * progress event occurs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsComputationService {

    private final InscriptionRepository    inscriptionRepository;
    private final CourseRepository         courseRepository;
    private final ApprenantRepository      apprenantRepository;
    private final StatsCoursRepository     statsCoursRepository;
    private final StatsApprenantRepository statsApprenantRepository;

  
    /**
     * Recalculates and upserts the StatsCours row for the given course.
     */
    @Async
    @Transactional
    public void recomputeStatsCours(Long courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) return;

        List<Inscription> inscriptions = inscriptionRepository.findByCourseId(courseId);

        int total = inscriptions.size();

        double avgProgression = total == 0 ? 0.0
                : inscriptions.stream().mapToDouble(i -> i.getProgression() == null ? 0.0 : i.getProgression()).average().orElse(0.0);

        double completionRate = total == 0 ? 0.0
                : inscriptions.stream().filter(i -> i.getProgression() != null && i.getProgression() >= 100.0).count() * 100.0 / total;

        avgProgression = Math.round(avgProgression * 10.0) / 10.0;
        completionRate = Math.round(completionRate * 10.0) / 10.0;

        StatsCours stats = statsCoursRepository.findByCourseId(courseId)
                .orElse(StatsCours.builder().course(course).build());

        stats.setFormateur(course.getFormateur());
        stats.setNombreInscriptions(total);
        stats.setProgressionMoyenne(avgProgression);
        stats.setTauxCompletionMoyen(completionRate);
        stats.setDateCalcul(LocalDateTime.now());

        statsCoursRepository.save(stats);
        log.debug("StatsCours updated — courseId={} total={} avg={}% completion={}%",
                courseId, total, avgProgression, completionRate);
    }

  
    /**
     * Recalculates and upserts the StatsApprenant row for the given learner.
     */
    @Async
    @Transactional
    public void recomputeStatsApprenant(Long apprenantId) {
        Apprenant apprenant = apprenantRepository.findById(apprenantId).orElse(null);
        if (apprenant == null) return;

        List<Inscription> inscriptions = inscriptionRepository.findByApprenantId(apprenantId);

        int total     = inscriptions.size();
        int completed = (int) inscriptions.stream().filter(i -> i.getProgression() != null && i.getProgression() >= 100.0).count();
        double avg    = total == 0 ? 0.0
                : inscriptions.stream().mapToDouble(i -> i.getProgression() == null ? 0.0 : i.getProgression()).average().orElse(0.0);
        avg = Math.round(avg * 10.0) / 10.0;

        StatsApprenant stats = statsApprenantRepository.findByApprenantId(apprenantId)
                .orElse(StatsApprenant.builder().apprenant(apprenant).build());

        stats.setCoursInscrits(total);
        stats.setCoursCompletes(completed);
        stats.setProgressionMoyenne(avg);
        stats.setDateCalcul(LocalDateTime.now());

        statsApprenantRepository.save(stats);
        log.debug("StatsApprenant updated — apprenantId={} total={} completed={} avg={}",
                apprenantId, total, completed, avg);
    }
}
