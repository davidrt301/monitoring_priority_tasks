package com.davidrt301.priority_tasks.model.dtos;

public record UserResponse(
    Long id,
    String username,
    String email,
    String password,
    String role
) {
}
