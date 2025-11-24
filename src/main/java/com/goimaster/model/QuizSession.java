package com.goimaster.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quiz_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "lesson_id", nullable = false)
    private UUID lessonId;

    @Column(nullable = false)
    private String mode; // 'kanji_meaning', 'furigana_meaning', 'kanji_furigana'

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
        if (completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private java.util.List<QuizSessionDetail> details = new java.util.ArrayList<>();
}
