package com.davidrt301.priority_tasks.model.dtos.Login;

import org.springframework.security.core.AuthenticatedPrincipal;

public record PrincipalInformation(
    String username, 
    String email
) implements AuthenticatedPrincipal {
    @Override
    public String getName() {
        return username;
    }
}
