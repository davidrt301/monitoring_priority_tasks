package com.davidrt301.priority_tasks.controller;

import com.davidrt301.priority_tasks.model.dtos.CategoryRequest;
import com.davidrt301.priority_tasks.model.dtos.CategoryResponse;
import com.davidrt301.priority_tasks.model.dtos.error.ErrorResponse;
import com.davidrt301.priority_tasks.service.category.CategoryService;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "API para la gestión de categorías de tareas")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear nueva categoría", description = "Crea una categoría en el sistema. El nombre debe ser único.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Categoría creada con éxito"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o la categoría ya existe", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar categorías", description = "Obtiene todas las categorías registradas.")
    @ApiResponse(responseCode = "200", description = "Operación exitosa")
    public List<CategoryResponse> findAll() {
        return categoryService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Obtener categoría por ID", description = "Retorna los detalles de una categoría específica.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CategoryResponse findById(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Actualizar categoría", description = "Actualiza el nombre de una categoría existente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría actualizada con éxito"),
        @ApiResponse(responseCode = "400", description = "Error de validación o nombre duplicado", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría del sistema por su ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Categoría eliminada con éxito"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada", 
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }

    @PostMapping("/seed")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Poblar categorías iniciales", description = "Inserta automáticamente un conjunto de categorías predeterminadas si no existen.")
    @ApiResponse(responseCode = "201", description = "Proceso de seeding finalizado")
    public void seed() {
        categoryService.seedDefaultCategories();
    }
}
