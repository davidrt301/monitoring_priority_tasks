package com.davidrt301.priority_tasks.model.dtos.error;

import java.time.LocalDateTime;

public record ErrorResponse(
    LocalDateTime timestamp,
    String message,
    String details
) {
}
