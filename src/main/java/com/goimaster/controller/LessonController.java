package com.goimaster.controller;

import com.goimaster.dto.request.CreateLessonRequest;
import com.goimaster.model.Lesson;
import com.goimaster.service.LessonService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class LessonController {
    
    @Autowired
    private LessonService lessonService;
    
    @GetMapping("/groups/{groupId}/lessons")
    public ResponseEntity<List<Lesson>> getLessons(@PathVariable UUID groupId) {
        List<Lesson> lessons = lessonService.getLessonsByGroupId(groupId);
        return ResponseEntity.ok(lessons);
    }
    
    @PostMapping("/groups/{groupId}/lessons")
    public ResponseEntity<Lesson> createLesson(
            @PathVariable UUID groupId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @Valid @RequestBody CreateLessonRequest request) {
        Lesson lesson = lessonService.createLesson(
                groupId,
                userId != null ? UUID.fromString(userId) : null,
                request);
        return ResponseEntity.status(HttpStatus.CREATED).body(lesson);
    }
    
    @GetMapping("/lessons/{id}")
    public ResponseEntity<Lesson> getLesson(@PathVariable UUID id) {
        Lesson lesson = lessonService.getLessonById(id);
        return ResponseEntity.ok(lesson);
    }
    
    @PutMapping("/lessons/{id}")
    public ResponseEntity<Lesson> updateLesson(
            @PathVariable UUID id,
            @Valid @RequestBody CreateLessonRequest request) {
        Lesson lesson = lessonService.updateLesson(id, request);
        return ResponseEntity.ok(lesson);
    }
    
    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable UUID id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }
}


