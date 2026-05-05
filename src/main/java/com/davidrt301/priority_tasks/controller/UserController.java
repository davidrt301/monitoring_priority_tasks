package com.davidrt301.priority_tasks.controller;

import com.davidrt301.priority_tasks.config.security.UserAuthorizationService;
import com.davidrt301.priority_tasks.model.dtos.TaskResponse;
import com.davidrt301.priority_tasks.model.dtos.UserRequest;
import com.davidrt301.priority_tasks.model.dtos.UserResponse;
import com.davidrt301.priority_tasks.model.dtos.error.ErrorResponse;
import com.davidrt301.priority_tasks.service.task.TaskService;
import com.davidrt301.priority_tasks.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "API para la gestión de usuarios y consulta de sus tareas asociadas")
public class UserController {

    private final UserService userService;
    private final TaskService taskService;
    private final UserAuthorizationService userAuthorizationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar todos los usuarios", description = "Retorna una lista con todos los usuarios registrados en el sistema.")
    @ApiResponse(responseCode = "200", description = "Lista recuperada exitosamente")
    public List<UserResponse> findAll() {
        if (!userAuthorizationService.isAdmin()) {
            return List.of(userService.findById(userAuthorizationService.currentUserId()));
        }
        return userService.findAll();
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un nuevo usuario", description = "Crea un usuario en el sistema. El email y el nombre de usuario deben ser únicos.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o el usuario ya existe", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UserResponse create(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Obtener usuario por ID", description = "Retorna la información detallada de un usuario específico.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
        @ApiResponse(responseCode = "404", description = "El usuario no existe", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UserResponse findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@userAuthorizationService.canAccessUserId(#id)")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Actualizar información de usuario", description = "Modifica los datos de un usuario existente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error de validación en los datos enviados", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "El usuario no existe", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@userAuthorizationService.canAccessUserId(#id)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un usuario", description = "Elimina de forma permanente un usuario del sistema.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "El usuario no existe", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void deleteUser(@PathVariable Long id) {
        // Authorization is enforced by PreAuthorize expression.
        userService.deleteUser(id);
    }

    @GetMapping("/{id}/tasks")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar tareas de un usuario", description = "Obtiene todas las tareas asignadas a un usuario específico.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de tareas recuperada"),
        @ApiResponse(responseCode = "404", description = "El usuario no existe", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<TaskResponse> getTasksByUser(@PathVariable Long id) {
        return taskService.findTasksByUserId(id);
    }
}
