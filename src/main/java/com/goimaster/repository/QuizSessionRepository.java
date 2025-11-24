package com.goimaster.repository;

import com.goimaster.model.QuizSession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizSessionRepository extends JpaRepository<QuizSession, UUID> {

    @EntityGraph(attributePaths = "details")
    Optional<QuizSession> findById(UUID id);

    List<QuizSession> findByLessonIdOrderByCompletedAtDesc(UUID lessonId);

    List<QuizSession> findByUserIdAndLessonIdOrderByCompletedAtDesc(UUID userId, UUID lessonId);
}
