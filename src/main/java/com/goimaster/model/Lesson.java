package com.goimaster.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "group_id", nullable = false)
    private UUID groupId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "order_index")
    private Integer orderIndex = 0;
    
    @Column(name = "word_count")
    private Integer wordCount = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


