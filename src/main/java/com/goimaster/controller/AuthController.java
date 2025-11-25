package com.goimaster.controller;

import com.goimaster.dto.request.LoginRequest;
import com.goimaster.dto.request.RegisterRequest;
import com.goimaster.exception.AuthenticationException;
import com.goimaster.exception.UserNotFoundException;
import com.goimaster.model.User;
import com.goimaster.service.AuthService;
import com.goimaster.service.UserVerificationService;
import jakarta.validation.Valid;
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
 * Controller for authentication
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserVerificationService userVerificationService;
    
    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var result = authService.register(request.getName(), request.getEmail(), request.getPassword());
            
            User user = (User) result.get("user");
            response.put("user", Map.of(
                "id", user.getId().toString(),
                "email", user.getEmail(),
                "name", user.getName()
            ));
            response.put("token", result.get("token"));
            response.put("message", "Registration successful");
            
            logger.info("User registered: {}", user.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (AuthenticationException e) {
            response.put("error", "REGISTRATION_FAILED");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Registration error: {}", e.getMessage(), e);
            response.put("error", "INTERNAL_ERROR");
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Login user
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            var result = authService.login(request.getEmail(), request.getPassword());
            
            User user = (User) result.get("user");
            response.put("user", Map.of(
                "id", user.getId().toString(),
                "email", user.getEmail(),
                "name", user.getName()
            ));
            response.put("token", result.get("token"));
            response.put("message", "Login successful");
            
            logger.info("User logged in: {}", user.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (AuthenticationException e) {
            response.put("error", "LOGIN_FAILED");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (UserNotFoundException e) {
            response.put("error", "USER_NOT_FOUND");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (Exception e) {
            logger.error("Login error: {}", e.getMessage(), e);
            response.put("error", "INTERNAL_ERROR");
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Verify if current user session is valid and user still exists
     */
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new AuthenticationException("Authorization header is required");
            }
            
            String token = authHeader.substring(7);
            User user = authService.verifyToken(token);
            
            response.put("valid", true);
            response.put("userId", user.getId().toString());
            response.put("message", "User session is valid");
            
            logger.info("User {} verified successfully", user.getId());
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


