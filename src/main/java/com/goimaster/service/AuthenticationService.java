package com.goimaster.service;

import com.goimaster.config.SupabaseConfig;
import com.goimaster.exception.AuthenticationException;
import com.goimaster.exception.UserNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * Service for authenticating requests with Supabase JWT tokens
 * Verifies JWT token by calling Supabase API and checks if user exists in database
 */
@Service
public class AuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    @Autowired
    private SupabaseConfig supabaseConfig;
    
    @Autowired
    private UserVerificationService userVerificationService;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Extract and verify user ID from JWT token and verify user exists
     * @param authHeader Authorization header (Bearer token)
     * @param userIdHeader X-User-Id header (fallback, but still verify)
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
        
        UUID userId = null;
        
        try {
            // Step 2: Extract user ID from token (decode JWT payload)
            userId = extractUserIdFromTokenUnsafe(token);
            logger.debug("Extracted user ID from token: {}", userId);
            
            // Step 3: CRITICAL - Verify user exists in database FIRST
            // This is the most important check - even if token is valid, if user is deleted, deny access
            logger.info("===========================================");
            logger.info("STEP 3: CHECKING USER EXISTENCE");
            logger.info("User ID from token: {}", userId);
            logger.info("Calling userVerificationService.userExists()...");
            
            boolean userExistsResult = userVerificationService.userExists(userId);
            
            logger.info("===========================================");
            logger.info("USER EXISTENCE CHECK RESULT");
            logger.info("User ID: {}", userId);
            logger.info("Exists: {}", userExistsResult);
            logger.info("===========================================");
            
            if (!userExistsResult) {
                logger.error("===========================================");
                logger.error("ACCESS DENIED - USER NOT FOUND");
                logger.error("User ID: {}", userId);
                logger.error("User account does not exist or has been deleted.");
                logger.error("===========================================");
                
                UserNotFoundException exception = new UserNotFoundException(
                    "User account does not exist or has been deleted. Please contact support."
                );
                logger.error("THROWING UserNotFoundException: {}", exception.getMessage());
                throw exception;
            }
            
            logger.info("User {} verified to exist in database. Continuing authentication...", userId);
            
            // Step 4: Cross-check with X-User-Id header if provided
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
            
            // Step 5: Verify token with Supabase API AND check if user is deleted
            // NOTE: This is done AFTER user existence check - if user doesn't exist, we don't even check token
            logger.debug("Step 5: Verifying token with Supabase API for user: {}", userId);
            UserVerificationResult supabaseResult = verifyTokenWithSupabaseAndCheckUser(token, userId);
            
            if (!supabaseResult.isTokenValid()) {
                logger.warn("Token verification failed with Supabase API for user: {}", userId);
                throw new AuthenticationException("Invalid or expired token. Please log in again.");
            }
            
            // Additional check: if Supabase says user is deleted, deny access
            if (supabaseResult.isUserDeleted()) {
                logger.error("===========================================");
                logger.error("ACCESS DENIED - USER DELETED IN SUPABASE");
                logger.error("User ID: {}", userId);
                logger.error("Supabase API reports user is deleted");
                logger.error("===========================================");
                throw new UserNotFoundException("User account has been deleted. Please contact support.");
            }
            
            logger.info("Successfully authenticated and verified user: {}", userId);
            return userId;
            
        } catch (UserNotFoundException e) {
            // Re-throw user not found exceptions
            throw e;
        } catch (AuthenticationException e) {
            // Re-throw authentication exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during authentication: {}", e.getMessage(), e);
            throw new AuthenticationException("Authentication failed: " + e.getMessage());
        }
    }
    
    /**
     * Extract user ID from JWT token without full verification (just decode)
     * Used as first step before full verification
     */
    private UUID extractUserIdFromTokenUnsafe(String token) throws AuthenticationException {
        try {
            // JWT has 3 parts: header.payload.signature
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new AuthenticationException("Invalid token format");
            }
            
            // Decode payload (base64)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonNode payloadJson = objectMapper.readTree(payload);
            
            // Get 'sub' claim (user ID)
            String userIdStr = payloadJson.get("sub").asText();
            
            if (userIdStr == null || userIdStr.isEmpty()) {
                throw new AuthenticationException("Token does not contain user ID");
            }
            
            try {
                return UUID.fromString(userIdStr);
            } catch (IllegalArgumentException e) {
                throw new AuthenticationException("Invalid user ID format in token: " + userIdStr);
            }
            
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error extracting user ID from token: {}", e.getMessage());
            throw new AuthenticationException("Failed to parse token: " + e.getMessage());
        }
    }
    
    /**
     * Result class for Supabase token verification
     */
    private static class UserVerificationResult {
        private final boolean tokenValid;
        private final boolean userDeleted;
        
        public UserVerificationResult(boolean tokenValid, boolean userDeleted) {
            this.tokenValid = tokenValid;
            this.userDeleted = userDeleted;
        }
        
        public boolean isTokenValid() {
            return tokenValid;
        }
        
        public boolean isUserDeleted() {
            return userDeleted;
        }
    }
    
    /**
     * Verify token with Supabase API and check if user is deleted
     * Calls Supabase /auth/v1/user endpoint to verify token is valid
     */
    private UserVerificationResult verifyTokenWithSupabaseAndCheckUser(String token, UUID userId) {
        try {
            String url = supabaseConfig.getSupabaseUrl() + "/auth/v1/user";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);
            headers.set("apikey", supabaseConfig.getSupabaseAnonKey());
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            logger.debug("Calling Supabase API: {}", url);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Check if response contains user data
                JsonNode responseJson = objectMapper.readTree(response.getBody());
                
                if (responseJson.has("id")) {
                    // Check if user has deleted_at field
                    boolean isDeleted = responseJson.has("deleted_at") && 
                                       !responseJson.get("deleted_at").isNull();
                    
                    String userIdFromResponse = responseJson.get("id").asText();
                    logger.info("Supabase API response - User ID: {}, Deleted: {}", userIdFromResponse, isDeleted);
                    
                    // Verify user ID matches (if userId provided)
                    if (userId != null && !userId.toString().equals(userIdFromResponse)) {
                        logger.warn("User ID mismatch: token={}, Supabase={}", userId, userIdFromResponse);
                        return new UserVerificationResult(false, false);
                    }
                    
                    if (isDeleted) {
                        logger.error("User {} is marked as deleted in Supabase", userId);
                        return new UserVerificationResult(true, true);
                    }
                    
                    logger.debug("Token verified successfully with Supabase, user is active");
                    return new UserVerificationResult(true, false);
                }
            }
            
            logger.warn("Token verification failed: HTTP {}", response.getStatusCode());
            return new UserVerificationResult(false, false);
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                logger.warn("Token is invalid or expired (401)");
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.error("User not found in Supabase (404) - user may be deleted");
                return new UserVerificationResult(false, true);
            } else {
                logger.warn("Token verification failed: HTTP {}", e.getStatusCode());
            }
            return new UserVerificationResult(false, false);
        } catch (Exception e) {
            logger.error("Error verifying token with Supabase: {}", e.getMessage(), e);
            // On error, deny access for security
            return new UserVerificationResult(false, false);
        }
    }
    
    /**
     * Legacy method for backward compatibility
     */
    private boolean verifyTokenWithSupabase(String token) {
        return verifyTokenWithSupabaseAndCheckUser(token, null).isTokenValid();
    }
}

