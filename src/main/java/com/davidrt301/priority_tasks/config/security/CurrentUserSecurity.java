package com.davidrt301.priority_tasks.config.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.davidrt301.priority_tasks.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CurrentUserSecurity {

    private final UserRepository userRepository;

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    public String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("No hay un usuario autenticado.");
        }
        return authentication.getName();
    }

    public Long currentUserId() {
        String username = currentUsername();
        return userRepository.findByUsername(username)
                .map(user -> user.getId())
                .orElseThrow(() -> new AccessDeniedException("Usuario autenticado no encontrado."));
    }

    public void ensureCurrentUserIdOrAdmin(Long resourceUserId) {
        if (!isAdmin() && !currentUserId().equals(resourceUserId)) {
            throw new AccessDeniedException("No tienes permisos para acceder a este recurso.");
        }
    }

    public void ensureCurrentUsernameOrAdmin(String resourceUsername) {
        if (!isAdmin() && !currentUsername().equals(resourceUsername)) {
            throw new AccessDeniedException("No tienes permisos para acceder a este recurso.");
        }
    }
}
