package com.example.SmartLearning.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.SmartLearning.model.LoginHistory;
import com.example.SmartLearning.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    List<LoginHistory> findTop10ByUserOrderByLoginTimeDesc(User user);

    boolean existsByUserAndLoginTimeBetween(User user, LocalDateTime start, LocalDateTime end);

    void deleteByUser(User user); 

    @Query(value = "SELECT DISTINCT DATE(l.login_time) FROM login_history l WHERE l.user_id = :userId ORDER BY 1 DESC", nativeQuery = true)
    List<LocalDate> findDistinctLoginDatesByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT DISTINCT DATE(l.login_time) FROM login_history l WHERE l.user_id = :userId AND l.login_time >= :sevenDaysAgo", nativeQuery = true)
    List<LocalDate> findLoginDatesLastSevenDays(@Param("userId") Long userId, @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);
}