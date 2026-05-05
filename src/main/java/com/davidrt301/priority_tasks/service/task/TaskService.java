package com.davidrt301.priority_tasks.service.task;

import java.util.List;

import com.davidrt301.priority_tasks.model.dtos.TaskRequest;
import com.davidrt301.priority_tasks.model.dtos.TaskResponse;

public interface TaskService {

    // CRUD Básico
    TaskResponse create(TaskRequest request);

    TaskResponse findById(Long id);

    List<TaskResponse> findAll();

    TaskResponse update(Long id, TaskRequest request);

    void delete(Long id);

    // Métodos de Negocio Específicos
    TaskResponse markAsCompleted(Long id);

    List<TaskResponse> findOverdueTasks();// buscarTareasPendientes

    List<TaskResponse> findOverdueTasksByUserId(Long userId);// buscarTareasVencidasPorIdDeUsuario

    List<TaskResponse> findTasksByUserId(Long userId);// Buscar tareas por ID de usuario

    List<TaskResponse> findTasksByUserIdAndCategory(Long userId, Long categoryId);// Buscar tareas por ID de usuario y
                                                                                  // categoría

}
