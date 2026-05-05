package com.davidrt301.priority_tasks.controller;

import com.davidrt301.priority_tasks.config.security.TaskAuthorizationService;
import com.davidrt301.priority_tasks.model.dtos.TaskRequest;
import com.davidrt301.priority_tasks.model.dtos.TaskResponse;
import com.davidrt301.priority_tasks.model.dtos.error.ErrorResponse;
import com.davidrt301.priority_tasks.service.task.TaskService;
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
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tareas", description = "API para la gestión de tareas, priorización y seguimiento")
public class TaskController {

    private final TaskService taskService;
    private final TaskAuthorizationService taskAuthorizationService;

    @PostMapping
    @PreAuthorize("@taskAuthorizationService.canAccessUserId(#request.userId)")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una nueva tarea", description = "Crea una tarea y calcula automáticamente su prioridad basada en la fecha de vencimiento y complejidad.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tarea creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TaskResponse create(@Valid @RequestBody TaskRequest request) {
        return taskService.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Listar todas las tareas", description = "Obtiene un listado completo de las tareas registradas.")
    @ApiResponse(responseCode = "200", description = "Lista recuperada exitosamente")
    public List<TaskResponse> findAll() {
        if (!taskAuthorizationService.isAdmin()) {
            return taskService.findTasksByUserId(taskAuthorizationService.currentUserId());
        }
        return taskService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@taskAuthorizationService.canAccessTaskId(#id)")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Obtener tarea por ID", description = "Retorna los detalles de una tarea específica.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tarea encontrada"),
        @ApiResponse(responseCode = "404", description = "La tarea no existe", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TaskResponse findById(@PathVariable Long id) {
        return taskService.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@taskAuthorizationService.canUpdateTask(#id, #request.userId)")
     @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Actualizar tarea", description = "Modifica los datos de una tarea existente y recalcula su prioridad.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tarea actualizada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Error en los datos o lógica de negocio", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "La tarea no existe", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return taskService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@taskAuthorizationService.canAccessTaskId(#id)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar tarea", description = "Elimina físicamente una tarea del sistema.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Tarea eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "La tarea no existe", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("@taskAuthorizationService.canAccessTaskId(#id)")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Marcar tarea como completada", description = "Cambia el estado de una tarea a completado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tarea marcada como completada"),
        @ApiResponse(responseCode = "404", description = "La tarea no existe", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TaskResponse markAsCompleted(@PathVariable Long id) {
        return taskService.markAsCompleted(id);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar tareas vencidas", description = "Obtiene todas las tareas cuya fecha de vencimiento es anterior a la actual y no han sido completadas.")
    @ApiResponse(responseCode = "200", description = "Lista de tareas vencidas recuperada")
    public List<TaskResponse> findOverdue() {
        if (!taskAuthorizationService.isAdmin()) {
            return taskService.findOverdueTasksByUserId(taskAuthorizationService.currentUserId());
        }
        return taskService.findOverdueTasks();
    }

    @GetMapping("/user/{userId}/category/{categoryId}")
    @PreAuthorize("@taskAuthorizationService.canAccessUserId(#userId)")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Filtrar tareas por usuario y categoría", description = "Busca tareas que pertenezcan a un usuario y una categoría específicos.")
    @ApiResponse(responseCode = "200", description = "Lista filtrada recuperada exitosamente")
    public List<TaskResponse> findByUserIdAndCategory(
            @PathVariable Long userId, 
            @PathVariable Long categoryId) {
        return taskService.findTasksByUserIdAndCategory(userId, categoryId);
    }
}
