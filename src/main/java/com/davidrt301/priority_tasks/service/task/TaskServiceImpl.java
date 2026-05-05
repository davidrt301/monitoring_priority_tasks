package com.davidrt301.priority_tasks.service.task;

import com.davidrt301.priority_tasks.exceptions.ResourceNotFoundException;
import com.davidrt301.priority_tasks.mappers.TaskMapper;
import com.davidrt301.priority_tasks.model.dtos.TaskRequest;
import com.davidrt301.priority_tasks.model.dtos.TaskResponse;
import com.davidrt301.priority_tasks.model.entities.Category;
import com.davidrt301.priority_tasks.model.entities.Task;
import com.davidrt301.priority_tasks.model.entities.User;
import com.davidrt301.priority_tasks.repository.CategoryRepository;
import com.davidrt301.priority_tasks.repository.TaskRepository;
import com.davidrt301.priority_tasks.repository.UserRepository;
import lombok.RequiredArgsConstructor;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TaskServiceImpl implements TaskService{

    private final TaskRepository taskRepository;
    private final UserRepository userRepository; // Necesario para validar el usuario
    private final CategoryRepository categoryRepository; // Necesario para validar la categoría
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public TaskResponse create(TaskRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + request.userId()));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + request.categoryId()));

        // Ahora pasamos los 3 objetos al mapper. 
        // MapStruct se encarga de setear el User y la Category por nosotros.
        
        Task task = taskMapper.toEntity(request, category, user);
        
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse findById(Long id) {
        return taskRepository.findById(id)
                .map(taskMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> findAll() {
        return taskRepository.findAll().stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + id));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + request.userId()));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + request.categoryId()));

        taskMapper.updateEntity(request, category, user, task);

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tarea no encontrada con ID: " + id);
        }
        taskRepository.deleteById(id);
    }

    @Override
    @Transactional
    public TaskResponse markAsCompleted(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + id));
        task.setCompleted(true);
        // Podrías añadir lógica para actualizar el TaskStatus si lo usas
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> findOverdueTasks() {
        return taskRepository.findByCompletedFalseAndExpirationDateBefore(LocalDateTime.now()).stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> findOverdueTasksByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + userId);
        }
        
        return taskRepository.findByUserIdAndCompletedFalseAndExpirationDateBefore(userId, LocalDateTime.now())
                .stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> findTasksByUserId(Long userId) {
        // Verificamos primero si el usuario existe (Fail Fast)
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + userId);
        }
        
        return taskRepository.findByUserId(userId).stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> findTasksByUserIdAndCategory(Long userId, Long categoryId) {
        return taskRepository.findByUserIdAndCategoryId(userId, categoryId).stream()
                .map(taskMapper::toResponse)
                .toList();
    }
}
