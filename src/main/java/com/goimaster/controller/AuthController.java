package com.goimaster.controller;

import com.goimaster.exception.AuthenticationException;
import com.goimaster.exception.UserNotFoundException;
import com.goimaster.service.AuthenticationService;
import com.goimaster.service.UserVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for authentication verification
 * Frontend can call this to verify if current user session is still valid
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private UserVerificationService userVerificationService;
    
    /**
     * Verify if current user session is valid and user still exists
     * Frontend should call this on app load to ensure user hasn't been deleted
     */
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verify user exists and token is valid
            UUID userId = authenticationService.verifyAndGetUserId(authHeader, userIdHeader);
            
            response.put("valid", true);
            response.put("userId", userId.toString());
            response.put("message", "User session is valid");
            
            logger.info("User {} verified successfully", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (UserNotFoundException e) {
            logger.error("User verification failed - user not found: {}", e.getMessage());
            
            response.put("valid", false);
            response.put("error", "USER_NOT_FOUND");
            response.put("message", "User account does not exist or has been deleted");
            
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            
        } catch (AuthenticationException e) {
            logger.error("User verification failed - authentication error: {}", e.getMessage());
            
            response.put("valid", false);
            response.put("error", "AUTHENTICATION_FAILED");
            response.put("message", "Invalid or expired token");
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            
        } catch (Exception e) {
            logger.error("Unexpected error during user verification: {}", e.getMessage(), e);
            
            response.put("valid", false);
            response.put("error", "INTERNAL_ERROR");
            response.put("message", "An unexpected error occurred");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Public endpoint to check email existence before registration
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam("email") String email) {
        Map<String, Object> response = new HashMap<>();
        
        if (email == null || email.trim().isEmpty()) {
            response.put("exists", false);
            response.put("confirmed", false);
            response.put("deleted", false);
            response.put("message", "Email is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        var status = userVerificationService.getEmailStatus(email);
        response.put("exists", status.exists());
        response.put("confirmed", status.confirmed());
        response.put("deleted", status.deleted());
        
        if (status.exists()) {
            response.put("message", status.confirmed()
                    ? "Email already registered"
                    : "Email pending verification");
        } else {
            response.put("message", "Email available");
        }
        
        return ResponseEntity.ok(response);
    }
}


