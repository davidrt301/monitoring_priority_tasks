package com.davidrt301.priority_tasks.repository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import com.davidrt301.priority_tasks.model.entities.Task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t JOIN FETCH t.user WHERE t.id = :id")
    Optional<Task> findWithUserById(@Param("id") Long id);

    List<Task> findByUserId(Long userId);

    List<Task> findByUserIdAndCategoryId(Long userId, Long categoryId);

    
    List<Task> findByUserIdAndCompletedFalseAndExpirationDateBefore(Long userId, LocalDateTime now);

    
    List<Task> findByCompletedFalseAndExpirationDateBefore(LocalDateTime now);
}
