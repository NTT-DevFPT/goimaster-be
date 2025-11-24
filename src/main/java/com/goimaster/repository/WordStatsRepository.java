package com.goimaster.repository;

import com.goimaster.model.WordStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WordStatsRepository extends JpaRepository<WordStats, UUID> {
    Optional<WordStats> findByUserIdAndWordId(UUID userId, UUID wordId);
    List<WordStats> findByUserIdAndWordIdIn(UUID userId, List<UUID> wordIds);
    
    @Query("SELECT ws FROM WordStats ws WHERE ws.userId = :userId AND ws.wordId IN " +
           "(SELECT w.id FROM Word w WHERE w.lessonId = :lessonId)")
    List<WordStats> findByUserIdAndLessonId(@Param("userId") UUID userId, @Param("lessonId") UUID lessonId);
}


