package com.goimaster.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Security configuration for JWT token validation from Supabase
 * This filter will extract and validate the Authorization header
 * Note: CORS filter should be processed first, so this filter should have higher order
 */
@Configuration
public class SecurityConfig {
    
    @Bean
    @Order(2) // Higher order - runs after CORS filter
    public OncePerRequestFilter authFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, 
                                          HttpServletResponse response, 
                                          FilterChain filterChain) throws ServletException, IOException {
                // Skip authentication check for OPTIONS requests (CORS preflight)
                if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // For now, we'll validate in controllers
                // In production, implement proper JWT validation here
                filterChain.doFilter(request, response);
            }
        };
    }
}


