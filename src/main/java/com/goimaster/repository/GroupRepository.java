package com.goimaster.repository;

import com.goimaster.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
    List<Group> findByUserIdOrderByCreatedAtDesc(UUID userId);
    boolean existsByUserIdAndName(UUID userId, String name);
}


