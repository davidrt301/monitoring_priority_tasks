package com.davidrt301.priority_tasks.model.dtos;

import com.davidrt301.priority_tasks.model.entities.Priority;
import java.time.LocalDateTime;

public record TaskResponse(
    Long id,
    String title,
    String description,
    LocalDateTime creationDate,
    LocalDateTime expirationDate,
    Priority priority,
    boolean completed,
    String categoryName,
    String username
) {
}
