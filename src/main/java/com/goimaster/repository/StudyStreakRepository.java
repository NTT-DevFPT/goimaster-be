package com.goimaster.repository;

import com.goimaster.model.StudyStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudyStreakRepository extends JpaRepository<StudyStreak, UUID> {
    Optional<StudyStreak> findByUserIdAndStudyDate(UUID userId, LocalDate date);
    
    @Query("SELECT s FROM StudyStreak s WHERE s.userId = :userId ORDER BY s.studyDate DESC")
    List<StudyStreak> findByUserIdOrderByDateDesc(@Param("userId") UUID userId);
    
    @Query("SELECT COUNT(DISTINCT s.studyDate) FROM StudyStreak s WHERE s.userId = :userId AND s.studyDate >= :startDate")
    Long countConsecutiveDays(@Param("userId") UUID userId, @Param("startDate") LocalDate startDate);
}


