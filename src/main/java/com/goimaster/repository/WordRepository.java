package com.goimaster.repository;

import com.goimaster.model.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WordRepository extends JpaRepository<Word, UUID> {
    List<Word> findByLessonIdOrderByCreatedAtAsc(UUID lessonId);
    void deleteByLessonId(UUID lessonId);
    long countByLessonId(UUID lessonId);
}


