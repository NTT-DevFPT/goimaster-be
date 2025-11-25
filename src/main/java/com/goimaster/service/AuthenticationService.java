package com.goimaster.service;

import com.goimaster.exception.AuthenticationException;
import com.goimaster.exception.UserNotFoundException;
import com.goimaster.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for authenticating requests with JWT tokens
 * Verifies JWT token and checks if user exists in database
 */
@Service
public class AuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private AuthService authService;
    
    /**
     * Extract and verify user ID from JWT token and verify user exists
     * @param authHeader Authorization header (Bearer token)
     * @param userIdHeader X-User-Id header (optional, for cross-check)
     * @return User ID from token
     * @throws AuthenticationException if token is invalid
     * @throws UserNotFoundException if user doesn't exist
     */
    public UUID verifyAndGetUserId(String authHeader, String userIdHeader) throws AuthenticationException, UserNotFoundException {
        // Step 1: Verify Authorization header exists
        if (authHeader == null || authHeader.trim().isEmpty()) {
            logger.warn("Missing Authorization header");
            throw new AuthenticationException("Authorization header is required. Please log in.");
        }
        
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Invalid Authorization header format");
            throw new AuthenticationException("Invalid authorization header format. Expected 'Bearer <token>'");
        }
        
        String token = authHeader.substring(7).trim();
        
        if (token.isEmpty()) {
            logger.warn("Empty token in Authorization header");
            throw new AuthenticationException("Token is required");
        }
        
        try {
            // Step 2: Verify token and get user
            User user = authService.verifyToken(token);
            UUID userId = user.getId();
            
            logger.debug("Extracted user ID from token: {}", userId);
            
            // Step 3: Cross-check with X-User-Id header if provided
            if (userIdHeader != null && !userIdHeader.trim().isEmpty()) {
                try {
                    UUID headerUserId = UUID.fromString(userIdHeader.trim());
                    if (!userId.equals(headerUserId)) {
                        logger.warn("User ID mismatch: token={}, header={}. Possible security issue.", userId, headerUserId);
                        throw new AuthenticationException("User ID mismatch between token and header");
                    }
                    logger.debug("X-User-Id header matches token user ID");
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid X-User-Id header format: {}", userIdHeader);
                    throw new AuthenticationException("Invalid X-User-Id header format");
                }
            }
            
            logger.info("Successfully authenticated and verified user: {}", userId);
            return userId;
            
        } catch (UserNotFoundException e) {
            throw e;
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during authentication: {}", e.getMessage(), e);
            throw new AuthenticationException("Authentication failed: " + e.getMessage());
        }
    }
}

