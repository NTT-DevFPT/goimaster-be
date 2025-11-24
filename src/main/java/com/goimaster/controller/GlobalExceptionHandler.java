package com.goimaster.controller;

import com.goimaster.exception.AuthenticationException;
import com.goimaster.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(AuthenticationException ex) {
        logger.warn("Authentication failed: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        error.put("code", "AUTHENTICATION_FAILED");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException ex) {
        logger.error("User not found: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        error.put("code", "USER_NOT_FOUND");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Invalid argument: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        Map<String, String> error = new HashMap<>();
        error.put("error", "An internal error occurred");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}


