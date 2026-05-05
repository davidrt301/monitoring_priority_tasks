package com.davidrt301.priority_tasks.config.security;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserAuthorizationService {

    private final CurrentUserSecurity currentUserSecurity;

    public boolean isAdmin() {
        return currentUserSecurity.isAdmin();
    }

    public Long currentUserId() {
        return currentUserSecurity.currentUserId();
    }

    public boolean canAccessUserId(Long userId) {
        return currentUserSecurity.isAdmin() || currentUserSecurity.currentUserId().equals(userId);
    }
}
