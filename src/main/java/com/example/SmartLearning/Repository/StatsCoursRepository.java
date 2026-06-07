package com.example.SmartLearning.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.SmartLearning.model.StatsCours;

public interface StatsCoursRepository extends JpaRepository<StatsCours, Long> {

    /** Find the pre-computed stats row for one course. */
    Optional<StatsCours> findByCourseId(Long courseId);

    /** All stats rows owned by a specific trainer — for the Trainer dashboard. */
    List<StatsCours> findByFormateurId(Long formateurId);

    /** Delete stats row when a course is hard-deleted. */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByCourseId(Long courseId);


    /** Total enrollments across every course. */
    @Query("SELECT COALESCE(SUM(s.nombreInscriptions), 0) FROM StatsCours s")
    Long sumTotalInscriptions();

    /**
     * Total completed enrollments across every course.
     * Returns the raw Double sum — callers should Math.round() it.
     * completions ≈ tauxCompletionMoyen (0–100 %) × nombreInscriptions ÷ 100
     */
    @Query("""
        SELECT COALESCE(SUM(s.tauxCompletionMoyen * s.nombreInscriptions / 100.0), 0.0)
        FROM StatsCours s
    """)
    Double sumTotalCompletionsRaw();

    /** [{courseTitle, count}] list for the "Enrollments per Course" bar chart (AdminAnalytics). */
    @Query("SELECT s.course.title, s.nombreInscriptions FROM StatsCours s ORDER BY s.nombreInscriptions DESC")
    List<Object[]> listInscriptionCountsByCourse();

    /** [{courseId, count}] list for the admin course management table student column. */
    @Query("SELECT s.course.id, s.nombreInscriptions FROM StatsCours s")
    List<Object[]> listInscriptionCountsByCourseId();

    /** Total number of courses that have a stats row (= all tracked courses). */
    @Query("SELECT COUNT(s) FROM StatsCours s")
    Long countCours();

    /** Total number of published (active) courses that have a stats row. */
    @Query("SELECT COUNT(s) FROM StatsCours s WHERE s.course.isActive = true")
    Long countPublishedCours();
}
