package com.goimaster.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.util.UUID;

@Entity
@Table(name = "quiz_session_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizSessionDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @JsonBackReference
    private QuizSession session;

    @Column(name = "word_id", nullable = false)
    private UUID wordId;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    // Optional: Store the word content at the time of quiz for historical accuracy
    private String kanji;
    private String meaning;
}
