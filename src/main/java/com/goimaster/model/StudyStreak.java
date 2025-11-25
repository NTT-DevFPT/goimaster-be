package com.goimaster.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "study_streaks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "study_date"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudyStreak {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "study_date", nullable = false)
    private LocalDate studyDate;
    
    @Column(name = "study_count")
    private Integer studyCount = 1;
    
    @Column(name = "last_studied_at")
    private LocalDateTime lastStudiedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (studyDate == null) {
            studyDate = LocalDate.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (lastStudiedAt == null) {
            lastStudiedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastStudiedAt = LocalDateTime.now();
    }
}






