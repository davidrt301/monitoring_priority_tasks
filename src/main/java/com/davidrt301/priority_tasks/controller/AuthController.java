package com.davidrt301.priority_tasks.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.davidrt301.priority_tasks.model.dtos.Login.LoginRequest;
import com.davidrt301.priority_tasks.model.dtos.Login.LoginResponse;
import com.davidrt301.priority_tasks.model.dtos.UserRequest;
import com.davidrt301.priority_tasks.model.dtos.UserResponse;
import com.davidrt301.priority_tasks.model.dtos.error.ErrorResponse;
import com.davidrt301.priority_tasks.service.auth.LoginService;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para el manejo de sesiones y registro de usuarios")
public class AuthController {

    private final LoginService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario y devuelve un token JWT válido.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar nuevo usuario", description = "Crea una nueva cuenta de usuario en el sistema.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario registrado con éxito"),
        @ApiResponse(responseCode = "400", description = "Datos de registro inválidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "El nombre de usuario o email ya existe",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void register(@Valid @RequestBody UserRequest request) {
        authService.register(request);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar todos los usuarios", description = "Retorna una lista de todos los usuarios registrados. Útil para administración.")
    @ApiResponse(responseCode = "200", description = "Lista recuperada exitosamente")
    public List<UserResponse> getAllUsers() {
        return authService.getAll();
    }
}
