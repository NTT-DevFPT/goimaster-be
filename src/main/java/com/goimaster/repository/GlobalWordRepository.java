package com.goimaster.repository;

import com.goimaster.model.GlobalWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GlobalWordRepository extends JpaRepository<GlobalWord, UUID> {

    // Find by exact kanji match
    List<GlobalWord> findByKanji(String kanji);

    // Find by all fields (for words without kanji or for verification)
    Optional<GlobalWord> findByKanjiAndFuriganaAndMeaningAndHanViet(
            String kanji, String furigana, String meaning, String hanViet);

    // For words with no kanji
    Optional<GlobalWord> findByFuriganaAndMeaningAndHanViet(
            String furigana, String meaning, String hanViet);

    // Search by furigana pattern (for quiz distractors)
    List<GlobalWord> findByFuriganaContaining(String furigana);

    // Get all words ordered by popularity
    List<GlobalWord> findAllByOrderByAddedCountDesc();

    // Search functionality
    @Query("SELECT gw FROM GlobalWord gw WHERE " +
            "LOWER(gw.kanji) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(gw.furigana) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(gw.meaning) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(gw.hanViet) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<GlobalWord> searchWords(@Param("search") String search);
}
