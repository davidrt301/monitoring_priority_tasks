package com.davidrt301.priority_tasks.model.dtos;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserRequest(
    @Size(min = 8, max = 50, message = "El nombre de usuario debe tener entre 8 y 50 caracteres")
    String username,

    @Email(message = "Debe proporcionar una dirección de correo válida")
    String email,

    @Size(min = 6, message = "La contraseña debe tener mínimo 6 caracteres")
    String password
) {
    public UserRequest {
        // Limpia espacios solo si el password no es nulo para evitar NullPointerException
        if (password != null) {
            password = password.replaceAll("\\s+", "");
        }
    }
}
