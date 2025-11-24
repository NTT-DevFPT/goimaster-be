package com.goimaster.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "word_stats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "word_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordStats {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "word_id", nullable = false)
    private UUID wordId;
    
    @Column(name = "seen_count")
    private Integer seenCount = 0;
    
    @Column(name = "correct_count")
    private Integer correctCount = 0;
    
    @Column(name = "incorrect_count")
    private Integer incorrectCount = 0;
    
    @Column(name = "last_practiced_at")
    private LocalDateTime lastPracticedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (lastPracticedAt == null && (seenCount > 0 || correctCount > 0 || incorrectCount > 0)) {
            lastPracticedAt = LocalDateTime.now();
        }
    }
}


