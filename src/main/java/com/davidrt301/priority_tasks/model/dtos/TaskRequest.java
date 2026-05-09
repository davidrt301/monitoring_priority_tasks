package com.davidrt301.priority_tasks.model.dtos;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record TaskRequest(
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 100, message = "El título no puede exceder los 100 caracteres")
    String title,

    String description,

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @FutureOrPresent(message = "La fecha de vencimiento no puede ser en el pasado")
    LocalDateTime expirationDate,

    @Min(value = 1, message = "La prioridad mínima es 1")
    @Max(value = 10, message = "La prioridad máxima es 10")
    int priority,

    @NotNull(message = "El ID de la categoría es obligatorio")
    Long categoryId,

    @NotNull(message = "El ID del usuario es obligatorio")
    Long userId
) {
}
