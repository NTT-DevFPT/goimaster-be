package com.goimaster.controller;

import com.goimaster.exception.AuthenticationException;
import com.goimaster.exception.UserNotFoundException;
import com.goimaster.service.AuthenticationService;
import com.goimaster.service.StudyStreakService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class StudyStreakController {
    
    private static final Logger logger = LoggerFactory.getLogger(StudyStreakController.class);
    
    @Autowired
    private StudyStreakService studyStreakService;
    
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
    
    @GetMapping("/study-streak")
    public ResponseEntity<Map<String, Object>> getStudyStreak(HttpServletRequest request) {
        try {
            UUID userId = getUserId(request);
            
            int currentStreak = studyStreakService.getCurrentStreak(userId);
            List<Boolean> last7Days = studyStreakService.getLast7DaysActivity(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("currentStreak", currentStreak);
            response.put("last7Days", last7Days);
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException | UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error getting study streak", e);
            throw new RuntimeException("Failed to get study streak", e);
        }
    }
}

