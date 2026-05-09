package com.davidrt301.priority_tasks.service.task;

import java.util.List;

import com.davidrt301.priority_tasks.model.dtos.TaskRequest;
import com.davidrt301.priority_tasks.model.dtos.TaskResponse;

public interface TaskService {

    TaskResponse create(TaskRequest request);

    TaskResponse findById(Long id);

    List<TaskResponse> findAll();

    TaskResponse update(Long id, TaskRequest request);

    void delete(Long id);

    // métodos de negocio específicos
    TaskResponse markAsCompleted(Long id);

    List<TaskResponse> findOverdueTasks();

    List<TaskResponse> findOverdueTasksByUserId(Long userId);// buscarTareasVencidasPorIdDeUsuario

    List<TaskResponse> findTasksByUserId(Long userId);

    List<TaskResponse> findTasksByUserIdAndCategory(Long userId, Long categoryId);

}
