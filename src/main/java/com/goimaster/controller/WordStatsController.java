package com.goimaster.controller;

import com.goimaster.dto.request.BatchUpdateWordStatsRequest;
import com.goimaster.exception.AuthenticationException;
import com.goimaster.exception.UserNotFoundException;
import com.goimaster.model.WordStats;
import com.goimaster.service.AuthenticationService;
import com.goimaster.service.WordStatsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class WordStatsController {
    
    private static final Logger logger = LoggerFactory.getLogger(WordStatsController.class);
    
    @Autowired
    private WordStatsService wordStatsService;
    
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
    
    @GetMapping("/lessons/{lessonId}/word-stats")
    public ResponseEntity<List<WordStats>> getWordStats(
            HttpServletRequest request,
            @PathVariable UUID lessonId) {
        try {
            UUID userId = getUserId(request);
            List<WordStats> stats = wordStatsService.getWordStatsByLessonId(userId, lessonId);
            return ResponseEntity.ok(stats);
        } catch (AuthenticationException | UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error getting word stats", e);
            throw new RuntimeException("Failed to get word stats", e);
        }
    }
    
    @PutMapping("/word-stats/batch")
    public ResponseEntity<Void> batchUpdateWordStats(
            HttpServletRequest request,
            @Valid @RequestBody BatchUpdateWordStatsRequest updateRequest) {
        try {
            UUID userId = getUserId(request);
            wordStatsService.batchUpdateWordStats(userId, updateRequest);
            return ResponseEntity.noContent().build();
        } catch (AuthenticationException | UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating word stats", e);
            throw new RuntimeException("Failed to update word stats", e);
        }
    }
}


