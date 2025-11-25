package com.goimaster.controller;

import com.goimaster.dto.request.CreateGroupRequest;
import com.goimaster.exception.AuthenticationException;
import com.goimaster.exception.UserNotFoundException;
import com.goimaster.model.Group;
import com.goimaster.service.AuthenticationService;
import com.goimaster.service.GroupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    
    private static final Logger logger = LoggerFactory.getLogger(GroupController.class);
    
    @Autowired
    private GroupService groupService;
    
    @Autowired
    private AuthenticationService authenticationService;
    
    /**
     * Extract and verify user ID from request
     * This method verifies JWT token and ensures user exists in database
     */
    private UUID getUserId(HttpServletRequest request) throws AuthenticationException, UserNotFoundException {
        String authHeader = request.getHeader("Authorization");
        String userIdHeader = request.getHeader("X-User-Id");
        
        return authenticationService.verifyAndGetUserId(authHeader, userIdHeader);
    }
    
    @GetMapping
    public ResponseEntity<List<Group>> getGroups(HttpServletRequest request) {
        try {
            UUID userId = getUserId(request);
            List<Group> groups = groupService.getGroupsByUserId(userId);
            return ResponseEntity.ok(groups);
        } catch (AuthenticationException | UserNotFoundException e) {
            throw e; // Let GlobalExceptionHandler handle these
        } catch (Exception e) {
            logger.error("Error getting groups", e);
            throw new RuntimeException("Failed to get groups", e);
        }
    }
    
    @PostMapping
    public ResponseEntity<Group> createGroup(
            HttpServletRequest request,
            @Valid @RequestBody CreateGroupRequest createRequest) {
        try {
            UUID userId = getUserId(request);
            Group group = groupService.createGroup(userId, createRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(group);
        } catch (AuthenticationException | UserNotFoundException e) {
            throw e; // Let GlobalExceptionHandler handle these
        } catch (Exception e) {
            logger.error("Error creating group", e);
            throw new RuntimeException("Failed to create group", e);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroup(@PathVariable UUID id) {
        Group group = groupService.getGroupById(id);
        return ResponseEntity.ok(group);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Group> updateGroup(
            HttpServletRequest request,
            @PathVariable UUID id,
            @Valid @RequestBody CreateGroupRequest updateRequest) {
        try {
            UUID userId = getUserId(request);
            Group group = groupService.updateGroup(userId, id, updateRequest);
            return ResponseEntity.ok(group);
        } catch (AuthenticationException | UserNotFoundException e) {
            throw e; // Let GlobalExceptionHandler handle these
        } catch (Exception e) {
            logger.error("Error updating group", e);
            throw new RuntimeException("Failed to update group", e);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(HttpServletRequest request, @PathVariable UUID id) {
        try {
            UUID userId = getUserId(request);
            groupService.deleteGroup(userId, id);
            return ResponseEntity.noContent().build();
        } catch (AuthenticationException | UserNotFoundException e) {
            throw e; // Let GlobalExceptionHandler handle these
        } catch (Exception e) {
            logger.error("Error deleting group", e);
            throw new RuntimeException("Failed to delete group", e);
        }
    }
}


