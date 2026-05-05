package com.davidrt301.priority_tasks.config.security;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.davidrt301.priority_tasks.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TaskAuthorizationService {

    private final CurrentUserSecurity currentUserSecurity;
    private final TaskRepository taskRepository;

    public boolean isAdmin() {
        return currentUserSecurity.isAdmin();
    }

    public Long currentUserId() {
        return currentUserSecurity.currentUserId();
    }

    public boolean canAccessUserId(Long userId) {
        return currentUserSecurity.isAdmin() || currentUserSecurity.currentUserId().equals(userId);
    }

    public boolean canAccessTaskId(Long taskId) {
        if (currentUserSecurity.isAdmin()) {
            return true;
        }
        Long currentUserId = currentUserSecurity.currentUserId();
        // return taskRepository.findById(taskId)
        //         .map(task -> Objects.equals(task.getUser() != null ? task.getUser().getId()
        return taskRepository.findWithUserById(taskId)
                .map(task -> Objects.equals(task.getUser().getId(), currentUserId))
                .orElse(false);
    }

    public boolean canUpdateTask(Long taskId, Long requestUserId) {
        return canAccessTaskId(taskId) && canAccessUserId(requestUserId);
    }
}
