package com.goimaster.service;

import com.goimaster.exception.AuthenticationException;
import com.goimaster.exception.UserNotFoundException;
import com.goimaster.model.Role;
import com.goimaster.model.User;
import com.goimaster.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoderService passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    /**
     * Register a new user
     */
    @Transactional
    public Map<String, Object> register(String name, String email, String password) {
        // Check if email already exists
        String normalizedEmail = email.toLowerCase().trim();
        if (userRepository.existsByEmailAndNotDeleted(normalizedEmail)) {
            throw new AuthenticationException("Email already registered");
        }
        
        // Create new user
        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name.trim());
        user.setEmailVerified(false);
        user.setRole(Role.USER); // Default role is USER
        
        user = userRepository.save(user);
        logger.info("New user registered: {} with role: {}", user.getEmail(), user.getRole());
        
        // Generate JWT token
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        
        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("token", token);
        
        return response;
    }
    
    /**
     * Login user
     */
    public Map<String, Object> login(String email, String password) {
        String normalizedEmail = email.toLowerCase().trim();
        
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new AuthenticationException("EMAIL_NOT_FOUND"));
        
        if (user.isDeleted()) {
            throw new UserNotFoundException("USER_DELETED");
        }
        
        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("INVALID_PASSWORD");
        }
        
        // Check if user is deleted
        logger.info("User logged in: {}", user.getEmail());
        
        // Generate JWT token
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        
        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("token", token);
        
        return response;
    }
    
    /**
     * Verify JWT token and return user
     */
    public User verifyToken(String token) {
        try {
            UUID userId = jwtService.getUserIdFromToken(token);
            
            if (jwtService.isTokenExpired(token)) {
                throw new AuthenticationException("Token expired");
            }
            
            User user = userRepository.findByIdAndNotDeleted(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            
            if (user.isDeleted()) {
                throw new UserNotFoundException("User account has been deleted");
            }
            
            return user;
            
        } catch (UserNotFoundException | AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error verifying token: {}", e.getMessage());
            throw new AuthenticationException("Invalid token");
        }
    }
}

