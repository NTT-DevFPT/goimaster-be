package com.goimaster.controller;

import com.goimaster.dto.request.CreateQuizSessionRequest;
import com.goimaster.exception.AuthenticationException;
import com.goimaster.exception.UserNotFoundException;
import com.goimaster.model.QuizSession;
import com.goimaster.service.AuthenticationService;
import com.goimaster.service.QuizSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class QuizSessionController {
    
    private static final Logger logger = LoggerFactory.getLogger(QuizSessionController.class);
    
    @Autowired
    private QuizSessionService quizSessionService;
    
    @Autowired
    private AuthenticationService authenticationService;
    
    /**
     * Extract and verify user ID from request
     */
    private UUID getUserId(HttpServletRequest request) throws AuthenticationException, UserNotFoundException {
        String authHeader = request.getHeader("Authorization");
        String userIdHeader = request.getHeader("X-User-Id");
        
        return authenticationService.verifyAndGetUserId(authHeader, userIdHeader);
    }
    
    @GetMapping("/lessons/{lessonId}/quiz-sessions")
    public ResponseEntity<List<QuizSession>> getQuizSessions(
            HttpServletRequest request,
            @PathVariable UUID lessonId) {
        try {
            UUID userId = getUserId(request);
            List<QuizSession> sessions = quizSessionService.getQuizSessionsByUserAndLesson(userId, lessonId);
            return ResponseEntity.ok(sessions);
        } catch (AuthenticationException | UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error getting quiz sessions", e);
            throw new RuntimeException("Failed to get quiz sessions", e);
        }
    }
    
    @PostMapping("/lessons/{lessonId}/quiz-sessions")
    public ResponseEntity<QuizSession> createQuizSession(
            HttpServletRequest request,
            @PathVariable UUID lessonId,
            @Valid @RequestBody CreateQuizSessionRequest createRequest) {
        try {
            UUID userId = getUserId(request);
            QuizSession session = quizSessionService.createQuizSession(userId, lessonId, createRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(session);
        } catch (AuthenticationException | UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating quiz session", e);
            throw new RuntimeException("Failed to create quiz session", e);
        }
    }
    
    @GetMapping("/quiz-sessions/{id}")
    public ResponseEntity<QuizSession> getQuizSession(@PathVariable UUID id) {
        QuizSession session = quizSessionService.getQuizSessionById(id);
        return ResponseEntity.ok(session);
    }
}


