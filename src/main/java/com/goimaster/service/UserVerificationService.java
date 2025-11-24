package com.goimaster.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service to verify if user exists in Supabase auth.users table
 */
@Service
public class UserVerificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserVerificationService.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public record EmailStatus(boolean exists, boolean confirmed, boolean deleted) {}
    
    /**
     * Check if user exists in auth.users table
     * @param userId User ID to check
     * @return true if user exists, false otherwise
     */
    public boolean userExists(UUID userId) {
        if (userId == null) {
            logger.warn("Null user ID provided");
            return false;
        }
        
        try {
            // Query auth.users table directly
            // Check both: user exists AND not deleted
            String sql = "SELECT COUNT(*) FROM auth.users WHERE id = ? AND deleted_at IS NULL";
            
            logger.info("EXECUTING USER EXISTENCE CHECK: SQL={}, userId={}", sql, userId);
            
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
            
            logger.info("QUERY RESULT: count={} for userId={}", count, userId);
            
            boolean exists = count != null && count > 0;
            
            if (!exists) {
                logger.error("=== USER NOT FOUND === User: {}. Count result: {}", userId, count);
                
                // Additional check: maybe user exists but is deleted
                try {
                    String checkDeletedSql = "SELECT COUNT(*) FROM auth.users WHERE id = ?";
                    Integer totalCount = jdbcTemplate.queryForObject(checkDeletedSql, Integer.class, userId);
                    logger.info("Total user count (including deleted): {} for userId: {}", totalCount, userId);
                    
                    if (totalCount != null && totalCount > 0) {
                        logger.error("User {} EXISTS in auth.users but has deleted_at set (user is DELETED)", userId);
                        
                        // Check deleted_at value
                        String checkDeletedAtSql = "SELECT deleted_at FROM auth.users WHERE id = ?";
                        try {
                            java.sql.Timestamp deletedAt = jdbcTemplate.queryForObject(
                                checkDeletedAtSql, 
                                java.sql.Timestamp.class, 
                                userId
                            );
                            logger.error("User {} deleted_at timestamp: {}", userId, deletedAt);
                        } catch (Exception e) {
                            logger.error("Could not retrieve deleted_at for user {}", userId, e);
                        }
                    } else {
                        logger.error("User {} does NOT EXIST at all in auth.users table", userId);
                    }
                } catch (Exception e) {
                    logger.error("Error checking deleted status for user {}", userId, e);
                }
                
                return false;
            } else {
                logger.info("=== USER EXISTS === User: {} verified in auth.users table", userId);
                return true;
            }
            
        } catch (org.springframework.dao.DataAccessException e) {
            logger.error("DATABASE ERROR checking if user exists for userId {}: {}", userId, e.getMessage(), e);
            logger.error("Exception type: {}", e.getClass().getName());
            if (e.getCause() != null) {
                logger.error("Cause: {}", e.getCause().getMessage());
            }
            // On error, deny access for security
            return false;
        } catch (Exception e) {
            logger.error("UNEXPECTED ERROR checking if user exists for userId {}: {}", userId, e.getMessage(), e);
            logger.error("Exception class: {}", e.getClass().getName());
            // On error, deny access for security
            return false;
        }
    }
    
    /**
     * Check if user profile exists in user_profiles table
     * This is an additional check to ensure user has completed registration
     */
    public boolean userProfileExists(UUID userId) {
        if (userId == null) {
            return false;
        }
        
        try {
            String sql = "SELECT COUNT(*) FROM user_profiles WHERE id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("Error checking user profile: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Check email status in auth.users table
     */
    public EmailStatus getEmailStatus(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new EmailStatus(false, false, false);
        }
        
        try {
            String sql = """
                SELECT confirmed_at, deleted_at
                FROM auth.users
                WHERE lower(email) = lower(?)
                LIMIT 1
            """;
            
            return jdbcTemplate.query(sql, rs -> {
                if (rs.next()) {
                    boolean confirmed = rs.getTimestamp("confirmed_at") != null;
                    boolean deleted = rs.getTimestamp("deleted_at") != null;
                    return new EmailStatus(true, confirmed, deleted);
                }
                return new EmailStatus(false, false, false);
            }, email.trim());
        } catch (Exception e) {
            logger.error("Error checking email status for {}: {}", email, e.getMessage(), e);
            return new EmailStatus(false, false, false);
        }
    }
}

