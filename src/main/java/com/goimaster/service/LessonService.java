package com.goimaster.service;

import com.goimaster.dto.request.CreateLessonRequest;
import com.goimaster.model.Group;
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
    
    public Lesson createLesson(UUID groupId, CreateLessonRequest request) {
        // Verify group exists
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        
        Lesson lesson = new Lesson();
        lesson.setGroupId(groupId);
        lesson.setName(request.getName());
        lesson.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);
        lesson.setWordCount(0);
        
        return lessonRepository.save(lesson);
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
    
    public void updateWordCount(UUID lessonId, int wordCount) {
        Lesson lesson = getLessonById(lessonId);
        lesson.setWordCount(wordCount);
        lessonRepository.save(lesson);
    }
}


