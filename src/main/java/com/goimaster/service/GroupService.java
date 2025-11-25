package com.goimaster.service;

import com.goimaster.dto.request.CreateGroupRequest;
import com.goimaster.model.Group;
import com.goimaster.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GroupService {
    
    @Autowired
    private GroupRepository groupRepository;
    
    public List<Group> getGroupsByUserId(UUID userId) {
        return groupRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public Group createGroup(UUID userId, CreateGroupRequest request) {
        // Check if group name already exists for this user
        if (groupRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new IllegalArgumentException("Group with this name already exists");
        }
        
        Group group = new Group();
        group.setUserId(userId);
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        
        return groupRepository.save(group);
    }
    
    public Group getGroupById(UUID groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    }
    
    public Group updateGroup(UUID userId, UUID groupId, CreateGroupRequest request) {
        Group group = getGroupById(groupId);
        
        // Verify ownership
        if (!group.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }
        
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        
        return groupRepository.save(group);
    }
    
    public void deleteGroup(UUID userId, UUID groupId) {
        Group group = getGroupById(groupId);
        
        // Verify ownership
        if (!group.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }
        
        groupRepository.delete(group);
    }
}


