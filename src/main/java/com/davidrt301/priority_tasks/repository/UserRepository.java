package com.davidrt301.priority_tasks.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.davidrt301.priority_tasks.model.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
