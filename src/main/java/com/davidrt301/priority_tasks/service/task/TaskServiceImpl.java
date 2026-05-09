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

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TaskServiceImpl implements TaskService{

    private final TaskRepository taskRepository;
    private final UserRepository userRepository; 
    private final CategoryRepository categoryRepository; 
    private final TaskMapper taskMapper;
    private final Clock clock;

    @Override
    @Transactional
    public TaskResponse create(TaskRequest request) {
        User user = validateUserExist(request.userId());
        Category category = validateCategoryExist(request.categoryId());
        
        Task task = taskMapper.toEntity(request, category, user);
        task.setCreationDate(LocalDateTime.now(clock)); // Sello de tiempo controlado
        
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse findById(Long id) {
        Task task = validateTaskExist(id);
        return taskMapper.toResponse(task);
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
        Task task = validateTaskExist(id);

        User user = validateUserExist(request.userId());

        Category category = validateCategoryExist(request.categoryId());

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
        Task task= validateTaskExist(id);
        task.setCompleted(true);
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> findOverdueTasks() {
        return taskRepository.findByCompletedFalseAndExpirationDateBefore(LocalDateTime.now(clock)).stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> findOverdueTasksByUserId(Long userId) {
        ensureUserExists(userId);
        
        return taskRepository.findByUserIdAndCompletedFalseAndExpirationDateBefore(userId, LocalDateTime.now(clock))
                .stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> findTasksByUserId(Long userId) {
        ensureUserExists(userId);
        
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

    private User validateUserExist(long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    private Category validateCategoryExist(long id){
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
    }

    private Task validateTaskExist(long id){
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + id));
    }

    private void ensureUserExists(long id){
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }
    }
}
