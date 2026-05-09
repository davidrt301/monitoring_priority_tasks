package com.davidrt301.priority_tasks.service.task;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.mockito.ArgumentMatchers.anyLong;

import org.junit.jupiter.api.Nested;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import com.davidrt301.priority_tasks.exceptions.ResourceNotFoundException;
import com.davidrt301.priority_tasks.mappers.TaskMapper;
import com.davidrt301.priority_tasks.model.dtos.TaskRequest;
import com.davidrt301.priority_tasks.model.dtos.TaskResponse;
import com.davidrt301.priority_tasks.model.entities.Category;
import com.davidrt301.priority_tasks.model.entities.Priority;
import com.davidrt301.priority_tasks.model.entities.User;
import com.davidrt301.priority_tasks.model.entities.Task;
import com.davidrt301.priority_tasks.repository.CategoryRepository;
import com.davidrt301.priority_tasks.repository.TaskRepository;
import com.davidrt301.priority_tasks.repository.UserRepository;


@ExtendWith(MockitoExtension.class)
public class TaskServiceImplTest {

    // Constantes para evitar valores mágicos
    private static final Long TASK_ID_1 = 1L;
    private static final Long TASK_ID_2 = 2L;
    private static final Long USER_ID = 1L;
    private static final Long CATEGORY_ID = 1L;
    private static final String USERNAME = "davidrt301";

    @Mock private Clock clock;
    @Mock private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock 
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task task, task2;
    private TaskRequest taskRequest;
    private TaskResponse taskResponse;
    private TaskResponse taskResponse2;
    private LocalDateTime fixedNow;
    private User user;
    private Category category;

    @BeforeEach
    void init(){
        fixedNow = LocalDateTime.of(2023, 10, 27, 10, 0);
        Clock fixedClock = Clock.fixed(fixedNow.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        
        lenient().when(clock.instant()).thenReturn(fixedClock.instant());
        lenient().when(clock.getZone()).thenReturn(fixedClock.getZone());

        user = new User(USER_ID, USERNAME, "email@test.com", "pass", null);
        category = new Category(CATEGORY_ID, "Trabajo");

        task = createTestTask(TASK_ID_1, "Tarea 1");
        task2 = createTestTask(TASK_ID_2, "Tarea 2");
        
        taskRequest = new TaskRequest("Tarea 1", "desc", fixedNow.minusDays(1), 3, USER_ID, CATEGORY_ID);
        taskResponse = createTestResponse(TASK_ID_1, "Tarea 1");
        taskResponse2 = createTestResponse(TASK_ID_2, "Tarea 2");
    }

    @Nested
    @DisplayName("Pruebas de Creación (Create)")
    class CreateTests {
        @Test
        @DisplayName("Debe crear una tarea con éxito")
        void shouldCreateTask_WhenCorrectData() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
            when(taskMapper.toEntity(taskRequest, category, user)).thenReturn(task);
            when(taskRepository.save(task)).thenReturn(task);
            when(taskMapper.toResponse(task)).thenReturn(taskResponse);

            TaskResponse response = taskService.create(taskRequest);

            assertNotNull(response);
            assertEquals(TASK_ID_1, response.id());
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException cuando el usuario no existe")
        void shouldThrowException_WhenUserNotExist() {
            when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
            
            assertThrows(ResourceNotFoundException.class, () -> taskService.create(taskRequest));
            verify(taskRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Pruebas de Búsqueda (Find)")
    class FindTests {
        @Test
        @DisplayName("Debe retornar una tarea por id")
        void shouldReturnTaskById() {
            when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.of(task));
            when(taskMapper.toResponse(task)).thenReturn(taskResponse);

            TaskResponse response = taskService.findById(TASK_ID_1);

            assertAll("Validación de Tarea",
                () -> assertNotNull(response),
                () -> assertEquals(TASK_ID_1, response.id()),
                () -> assertEquals(USERNAME, response.username())
            );
        }

        @Test
        @DisplayName("Debe retornar todas las tareas")
        void shouldReturnAllTasks() {
            when(taskRepository.findAll()).thenReturn(List.of(task, task2));
            when(taskMapper.toResponse(task)).thenReturn(taskResponse);
            when(taskMapper.toResponse(task2)).thenReturn(taskResponse2);

            List<TaskResponse> responses = taskService.findAll();

            assertEquals(2, responses.size());
        }

        @Test
        @DisplayName("Debe retornar tareas por ID de usuario cuando el usuario existe")
        void shouldReturnTasksByUserId_WhenUserExists() {
            // GIVEN
            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(taskRepository.findByUserId(USER_ID)).thenReturn(List.of(task, task2));
            when(taskMapper.toResponse(task)).thenReturn(taskResponse);
            when(taskMapper.toResponse(task2)).thenReturn(taskResponse2);

            // WHEN
            List<TaskResponse> result = taskService.findTasksByUserId(USER_ID);

            // THEN
            assertAll("Validación de tareas por usuario",
                () -> assertNotNull(result),
                () -> assertEquals(2, result.size()),
                () -> verify(userRepository).existsById(USER_ID),
                () -> verify(taskRepository).findByUserId(USER_ID)
            );
        }

        @Test
        @DisplayName("Debe lanzar ResourceNotFoundException cuando el usuario no existe al buscar por ID")
        void shouldThrowException_WhenUserDoesNotExist_FindingByUserId() {
            when(userRepository.existsById(USER_ID)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class, () -> taskService.findTasksByUserId(USER_ID));
            verify(taskRepository, never()).findByUserId(anyLong());
        }
    }

    @Nested
    @DisplayName("Pruebas de Actualización y Estado")
    class UpdateAndStatusTests {
        @Test
        @DisplayName("Debe actualizar una tarea con éxito")
        void shouldUpdateTask_WhenExists() {
            when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.of(task));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
            when(taskRepository.save(task)).thenReturn(task);
            when(taskMapper.toResponse(task)).thenReturn(taskResponse);

            TaskResponse response = taskService.update(TASK_ID_1, taskRequest);

            assertNotNull(response);
            verify(taskMapper).updateEntity(taskRequest, category, user, task);
        }

        @Test
        @DisplayName("Debería marcar una tarea como completada")
        void shouldMarkAsCompleted() {
            when(taskRepository.findById(TASK_ID_1)).thenReturn(Optional.of(task));
            when(taskRepository.save(any())).thenReturn(task);
            
            TaskResponse completedResponse = createTestResponse(TASK_ID_1, "T1"); // Simplificado
            when(taskMapper.toResponse(task)).thenReturn(completedResponse);

            taskService.markAsCompleted(TASK_ID_1);
            
            assertTrue(task.isCompleted());
            verify(taskRepository).save(task);
        }
    }

    @Nested
    @DisplayName("Pruebas de Tareas Vencidas (Overdue)")
    class OverdueTests {
        @Test
        @DisplayName("Debería retornar tareas vencidas globales")
        void shouldReturnGlobalOverdueTasks() {
            when(taskRepository.findByCompletedFalseAndExpirationDateBefore(fixedNow)).thenReturn(List.of(task, task2));
            when(taskMapper.toResponse(task)).thenReturn(taskResponse);
            when(taskMapper.toResponse(task2)).thenReturn(taskResponse2);

            List<TaskResponse> result = taskService.findOverdueTasks();

            assertEquals(2, result.size());
            verify(taskRepository).findByCompletedFalseAndExpirationDateBefore(fixedNow);
        }

        @Test
        @DisplayName("Debería retornar vencidas por usuario")
        void shouldReturnOverdueTasksByUser() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);
            when(taskRepository.findByUserIdAndCompletedFalseAndExpirationDateBefore(USER_ID, fixedNow)).thenReturn(List.of(task));
            when(taskMapper.toResponse(task)).thenReturn(taskResponse);

            List<TaskResponse> result = taskService.findOverdueTasksByUserId(USER_ID);

            assertFalse(result.isEmpty());
            verify(userRepository).existsById(USER_ID);
        }
    }

    @Nested
    @DisplayName("Pruebas de Eliminación (Delete)")
    class DeleteTests {
        @Test
        @DisplayName("Debe eliminar con éxito")
        void shouldDelete_WhenExists() {
            when(taskRepository.existsById(TASK_ID_1)).thenReturn(true);
            taskService.delete(TASK_ID_1);
            verify(taskRepository).deleteById(TASK_ID_1);
        }

        @Test
        @DisplayName("Debe fallar al eliminar inexistente")
        void shouldFailDelete_WhenNotExists() {
            when(taskRepository.existsById(anyLong())).thenReturn(false);
            assertThrows(ResourceNotFoundException.class, () -> taskService.delete(99L));
        }
    }

    @Nested
    @DisplayName("Pruebas de Filtrado por Usuario y Categoría")
    class FilterTests {
        @Test
        @DisplayName("Debe retornar por usuario y categoría")
        void shouldReturnByUserAndCategory() {
            when(taskRepository.findByUserIdAndCategoryId(USER_ID, CATEGORY_ID)).thenReturn(List.of(task));
            when(taskMapper.toResponse(task)).thenReturn(taskResponse);

            List<TaskResponse> result = taskService.findTasksByUserIdAndCategory(USER_ID, CATEGORY_ID);

            assertEquals(1, result.size());
        }
    }

    // --- MÉTODOS DE APOYO (HELPERS) ---

    private Task createTestTask(Long id, String title) {
        Task t = new Task();
        t.setId(id);
        t.setTitle(title);
        t.setCreationDate(fixedNow);
        t.setUser(user);
        t.setCategory(category);
        t.setExpirationDate(fixedNow.minusDays(1)); // Vencida por defecto
        return t;
    }

    private TaskResponse createTestResponse(Long id, String title) {
        return new TaskResponse(id, title, "desc", fixedNow, fixedNow.minusDays(1),
            Priority.LOW, false, "Trabajo", USERNAME);
    }
}
