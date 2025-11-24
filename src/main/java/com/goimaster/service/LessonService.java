package com.goimaster.service;

import com.goimaster.dto.request.CreateLessonRequest;
import com.goimaster.model.Lesson;
import com.goimaster.repository.GroupRepository;
import com.goimaster.repository.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LessonService {
    
    @Autowired
    private LessonRepository lessonRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    public List<Lesson> getLessonsByGroupId(UUID groupId) {
        return lessonRepository.findByGroupIdOrderByOrderIndexAsc(groupId);
    }
    
    @Autowired
    private WordService wordService;
    
    public Lesson createLesson(UUID groupId, UUID userId, CreateLessonRequest request) {
        // Verify group exists
        groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        
        Lesson lesson = new Lesson();
        lesson.setGroupId(groupId);
        lesson.setName(request.getName());
        lesson.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);
        lesson.setWordCount(0);
        
        Lesson savedLesson = lessonRepository.save(lesson);
        
        if (request.getWords() != null && !request.getWords().isEmpty()) {
            if (userId == null) {
                throw new IllegalArgumentException("User ID is required when importing words");
            }
            int createdCount = wordService.createWords(savedLesson.getId(), userId, request.getWords()).size();
            savedLesson.setWordCount(createdCount);
        }
        
        return savedLesson;
    }
    
    public Lesson getLessonById(UUID lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
    }
    
    public Lesson updateLesson(UUID lessonId, CreateLessonRequest request) {
        Lesson lesson = getLessonById(lessonId);
        lesson.setName(request.getName());
        if (request.getOrderIndex() != null) {
            lesson.setOrderIndex(request.getOrderIndex());
        }
        
        return lessonRepository.save(lesson);
    }
    
    public void deleteLesson(UUID lessonId) {
        Lesson lesson = getLessonById(lessonId);
        lessonRepository.delete(lesson);
    }
    
}


