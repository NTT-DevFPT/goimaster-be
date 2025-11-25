package com.goimaster.service;

import com.goimaster.model.StudyStreak;
import com.goimaster.repository.StudyStreakRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class StudyStreakService {
    
    @Autowired
    private StudyStreakRepository studyStreakRepository;
    
    /**
     * Record a study session for today
     */
    public void recordStudySession(UUID userId) {
        LocalDate today = LocalDate.now();
        Optional<StudyStreak> existing = studyStreakRepository.findByUserIdAndStudyDate(userId, today);
        
        if (existing.isPresent()) {
            StudyStreak streak = existing.get();
            streak.setStudyCount(streak.getStudyCount() + 1);
            streak.setLastStudiedAt(LocalDateTime.now());
            studyStreakRepository.save(streak);
        } else {
            StudyStreak streak = new StudyStreak();
            streak.setUserId(userId);
            streak.setStudyDate(today);
            streak.setStudyCount(1);
            streak.setLastStudiedAt(LocalDateTime.now());
            studyStreakRepository.save(streak);
        }
    }
    
    /**
     * Get current streak count (consecutive days)
     */
    public int getCurrentStreak(UUID userId) {
        List<StudyStreak> streaks = studyStreakRepository.findByUserIdOrderByDateDesc(userId);
        if (streaks.isEmpty()) {
            return 0;
        }
        
        LocalDate today = LocalDate.now();
        LocalDate checkDate = today;
        int streakCount = 0;
        
        // Check if studied today
        if (!streaks.isEmpty() && streaks.get(0).getStudyDate().equals(today)) {
            streakCount = 1;
            checkDate = today.minusDays(1);
        } else if (!streaks.isEmpty() && streaks.get(0).getStudyDate().equals(today.minusDays(1))) {
            // Studied yesterday, continue from yesterday
            streakCount = 1;
            checkDate = today.minusDays(2);
        } else {
            // No recent study, streak is broken
            return 0;
        }
        
        // Count consecutive days backwards
        for (StudyStreak streak : streaks) {
            if (streak.getStudyDate().equals(checkDate)) {
                streakCount++;
                checkDate = checkDate.minusDays(1);
            } else if (streak.getStudyDate().isBefore(checkDate)) {
                // Gap found, streak is broken
                break;
            }
        }
        
        return streakCount;
    }
    
    /**
     * Get study dates for the last 7 days (for UI display)
     */
    public List<Boolean> getLast7DaysActivity(UUID userId) {
        List<Boolean> activity = new java.util.ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Optional<StudyStreak> streak = studyStreakRepository.findByUserIdAndStudyDate(userId, date);
            activity.add(streak.isPresent());
        }
        
        return activity;
    }
}






