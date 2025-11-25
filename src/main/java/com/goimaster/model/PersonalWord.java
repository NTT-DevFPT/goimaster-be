package com.goimaster.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "personal_words", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "kanji", "furigana",
        "meaning", "han_viet" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalWord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String kanji;

    @Column(name = "han_viet", nullable = false)
    private String hanViet;

    @Column(nullable = false)
    private String furigana;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String meaning;

    @Column(name = "added_at")
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}
