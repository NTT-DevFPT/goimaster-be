package com.goimaster.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "global_words")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalWord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String kanji;

    @Column(name = "han_viet", nullable = false)
    private String hanViet;

    @Column(nullable = false)
    private String furigana;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String meaning;

    @Column(name = "first_added_at")
    private LocalDateTime firstAddedAt;

    @Column(name = "added_count")
    private Integer addedCount = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        firstAddedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (addedCount == null) {
            addedCount = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
