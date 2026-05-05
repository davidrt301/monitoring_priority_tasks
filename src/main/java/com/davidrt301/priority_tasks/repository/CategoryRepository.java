package com.davidrt301.priority_tasks.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.davidrt301.priority_tasks.model.entities.Category;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    
    boolean existsByName(String name);
}
