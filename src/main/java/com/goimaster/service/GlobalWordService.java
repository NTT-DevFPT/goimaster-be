package com.goimaster.service;

import com.goimaster.model.GlobalWord;
import com.goimaster.model.Word;
import com.goimaster.repository.GlobalWordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class GlobalWordService {

    @Autowired
    private GlobalWordRepository globalWordRepository;

    /**
     * Find or create a global word entry with smart duplicate detection
     * Rules:
     * 1. If kanji exists: match by kanji + verify furigana, meaning, hanViet
     * 2. If no kanji: match by furigana + meaning + hanViet
     */
    public GlobalWord findOrCreateGlobalWord(Word word) {
        // STEP 1: Check by kanji if present
        if (word.getKanji() != null && !word.getKanji().trim().isEmpty()) {
            // Try exact match first
            Optional<GlobalWord> exactMatch = globalWordRepository
                    .findByKanjiAndFuriganaAndMeaningAndHanViet(
                            word.getKanji(),
                            word.getFurigana(),
                            word.getMeaning(),
                            word.getHanViet());

            if (exactMatch.isPresent()) {
                // Found exact duplicate, increment counter
                GlobalWord existing = exactMatch.get();
                existing.setAddedCount(existing.getAddedCount() + 1);
                existing.setUpdatedAt(LocalDateTime.now());
                return globalWordRepository.save(existing);
            }

            // Check for same kanji but different readings/meanings
            List<GlobalWord> sameKanji = globalWordRepository.findByKanji(word.getKanji());
            for (GlobalWord gw : sameKanji) {
                // Verify all fields match
                if (gw.getFurigana().equals(word.getFurigana()) &&
                        gw.getMeaning().equals(word.getMeaning()) &&
                        gw.getHanViet().equals(word.getHanViet())) {
                    // Found duplicate
                    gw.setAddedCount(gw.getAddedCount() + 1);
                    gw.setUpdatedAt(LocalDateTime.now());
                    return globalWordRepository.save(gw);
                }
            }
        }

        // STEP 2: For words without kanji, check by combination
        else {
            Optional<GlobalWord> existing = globalWordRepository
                    .findByFuriganaAndMeaningAndHanViet(
                            word.getFurigana(),
                            word.getMeaning(),
                            word.getHanViet());

            if (existing.isPresent()) {
                GlobalWord gw = existing.get();
                gw.setAddedCount(gw.getAddedCount() + 1);
                gw.setUpdatedAt(LocalDateTime.now());
                return globalWordRepository.save(gw);
            }
        }

        // STEP 3: Not found, create new
        return createNewGlobalWord(word);
    }

    private GlobalWord createNewGlobalWord(Word word) {
        GlobalWord globalWord = new GlobalWord();
        globalWord.setKanji(word.getKanji());
        globalWord.setFurigana(word.getFurigana());
        globalWord.setMeaning(word.getMeaning());
        globalWord.setHanViet(word.getHanViet());
        globalWord.setFirstAddedAt(LocalDateTime.now());
        globalWord.setAddedCount(1);
        return globalWordRepository.save(globalWord);
    }

    public List<GlobalWord> getAllWords() {
        return globalWordRepository.findAllByOrderByAddedCountDesc();
    }

    public List<GlobalWord> searchWords(String query) {
        return globalWordRepository.searchWords(query);
    }

    public List<GlobalWord> findSimilarWords(String furiganaPattern) {
        return globalWordRepository.findByFuriganaContaining(furiganaPattern);
    }

    public GlobalWord getWordById(UUID id) {
        return globalWordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Global word not found"));
    }
}
