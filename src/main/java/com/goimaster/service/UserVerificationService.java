package com.goimaster.service;

import com.goimaster.model.User;
import com.goimaster.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Service to verify if user exists in database
 */
@Service
public class UserVerificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserVerificationService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    public record EmailStatus(boolean exists, boolean confirmed, boolean deleted) {}
    
    /**
     * Check if user exists and is not deleted
     * @param userId User ID to check
     * @return true if user exists, false otherwise
     */
    public boolean userExists(UUID userId) {
        if (userId == null) {
            logger.warn("Null user ID provided");
            return false;
        }
        
        try {
            boolean exists = userRepository.existsByIdAndNotDeleted(userId);
            logger.info("User {} exists check: {}", userId, exists);
            return exists;
        } catch (Exception e) {
            logger.error("Error checking if user exists for userId {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Check email status in users table
     */
    public EmailStatus getEmailStatus(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new EmailStatus(false, false, false);
        }
        
        try {
            Optional<User> userOpt = userRepository.findByEmailAndNotDeleted(email.trim().toLowerCase());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                return new EmailStatus(true, user.getEmailVerified(), user.isDeleted());
            }
            return new EmailStatus(false, false, false);
        } catch (Exception e) {
            logger.error("Error checking email status for {}: {}", email, e.getMessage(), e);
            return new EmailStatus(false, false, false);
        }
    }
}

