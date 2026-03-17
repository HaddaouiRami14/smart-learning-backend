package com.example.SmartLearning.service;
import com.example.SmartLearning.Repository.LoginHistoryRepository;
import com.example.SmartLearning.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StreakService {

    private final LoginHistoryRepository loginHistoryRepository;
    private static final ZoneId ZONE = ZoneId.of("Africa/Tunis");

    public int calculateStreak(User user) {
        List<LocalDate> dates = loginHistoryRepository
                .findDistinctLoginDatesByUserId(user.getId())
                .stream()
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        System.out.println("=== STREAK DEBUG ===");
        System.out.println("User ID: " + user.getId());
        System.out.println("Dates found: " + dates);
        System.out.println("Today: " + LocalDate.now(ZONE));

        if (dates.isEmpty()) return 0;

        LocalDate today = LocalDate.now(ZONE);
        LocalDate yesterday = today.minusDays(1);

        if (!dates.get(0).equals(today) && !dates.get(0).equals(yesterday)) {
            return 0;
        }

        LocalDate expected = dates.get(0);
        int streak = 0;

        for (LocalDate date : dates) {
            if (date.equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    public boolean[] getLast7DaysActivity(User user) {
        List<LocalDate> activeDates = loginHistoryRepository
                .findLoginDatesLastSevenDays(
                        user.getId(),
                        LocalDateTime.now(ZONE).minusDays(7)
                )
                .stream()
                .collect(Collectors.toList());

        boolean[] result = new boolean[7];
        LocalDate today = LocalDate.now(ZONE);

        for (int i = 0; i < 7; i++) {
            result[i] = activeDates.contains(today.minusDays(i));
        }
        return result;
    }
}