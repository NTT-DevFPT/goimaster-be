package com.goimaster.service;

import com.goimaster.dto.request.BatchUpdateWordStatsRequest;
import com.goimaster.model.WordStats;
import com.goimaster.repository.WordRepository;
import com.goimaster.repository.WordStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class WordStatsService {
    
    @Autowired
    private WordStatsRepository wordStatsRepository;
    
    @Autowired
    private WordRepository wordRepository;
    
    public List<WordStats> getWordStatsByLessonId(UUID userId, UUID lessonId) {
        return wordStatsRepository.findByUserIdAndLessonId(userId, lessonId);
    }
    
    public void batchUpdateWordStats(UUID userId, BatchUpdateWordStatsRequest request) {
        for (BatchUpdateWordStatsRequest.WordStatUpdate update : request.getUpdates()) {
            // Verify word exists
            wordRepository.findById(update.getWordId())
                    .orElseThrow(() -> new IllegalArgumentException("Word not found: " + update.getWordId()));
            
            Optional<WordStats> existingStats = wordStatsRepository.findByUserIdAndWordId(userId, update.getWordId());
            
            WordStats stats;
            if (existingStats.isPresent()) {
                stats = existingStats.get();
                stats.setSeenCount(stats.getSeenCount() + update.getSeenCount());
                stats.setCorrectCount(stats.getCorrectCount() + update.getCorrectCount());
                stats.setIncorrectCount(stats.getIncorrectCount() + update.getIncorrectCount());
                stats.setLastPracticedAt(LocalDateTime.now());
            } else {
                stats = new WordStats();
                stats.setUserId(userId);
                stats.setWordId(update.getWordId());
                stats.setSeenCount(update.getSeenCount());
                stats.setCorrectCount(update.getCorrectCount());
                stats.setIncorrectCount(update.getIncorrectCount());
                stats.setLastPracticedAt(LocalDateTime.now());
            }
            
            wordStatsRepository.save(stats);
        }
    }
}


