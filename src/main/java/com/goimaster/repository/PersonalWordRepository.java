package com.goimaster.repository;

import com.goimaster.model.PersonalWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonalWordRepository extends JpaRepository<PersonalWord, UUID> {

    // Find all words for a specific user
    List<PersonalWord> findByUserIdOrderByAddedAtDesc(UUID userId);

    // Find exact match by all fields
    Optional<PersonalWord> findByUserIdAndKanjiAndFuriganaAndMeaningAndHanViet(
            UUID userId, String kanji, String furigana, String meaning, String hanViet);

    // Search user's words
    @Query("SELECT pw FROM PersonalWord pw WHERE pw.userId = :userId AND (" +
            "LOWER(pw.kanji) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(pw.furigana) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(pw.meaning) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(pw.hanViet) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<PersonalWord> searchUserWords(@Param("userId") UUID userId, @Param("search") String search);
}
