package com.davidrt301.priority_tasks.service.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.davidrt301.priority_tasks.exceptions.BusinessException;
import com.davidrt301.priority_tasks.exceptions.DataConflictException;
import com.davidrt301.priority_tasks.exceptions.ResourceNotFoundException;
import com.davidrt301.priority_tasks.mappers.UserMapper;
import com.davidrt301.priority_tasks.model.dtos.UserRequest;
import com.davidrt301.priority_tasks.model.dtos.UserResponse;
import com.davidrt301.priority_tasks.model.entities.Role;
import com.davidrt301.priority_tasks.model.entities.User;
import com.davidrt301.priority_tasks.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final String USERNAME = "davidrt301";
    private static final String EMAIL = "david@test.com";
    private static final String PASSWORD = "plainPassword";
    private static final String ENCODED_PASSWORD = "encodedPassword123";

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserServiceImpl userService;

    private User user;
    private UserRequest userRequest;
    private UserResponse userResponse;

    @BeforeEach
    void init() {
        user = createTestUser(USER_ID, USERNAME, EMAIL);
        userRequest = new UserRequest(USERNAME, EMAIL, PASSWORD);
        userResponse = createTestResponse(USER_ID, USERNAME, EMAIL);
    }

    @Nested
    @DisplayName("Pruebas de Registro (Create)")
    class CreateTests {
        @Test
        @DisplayName("Debe registrar un usuario exitosamente con rol USER")
        void shouldCreateUser_WhenDataIsValid() {
            // GIVEN
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
            when(userMapper.toEntity(userRequest)).thenReturn(user);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            // WHEN
            UserResponse result = userService.create(userRequest);

            // THEN
            assertNotNull(result);
            assertEquals(USERNAME, result.username());
            verify(user).setPassword(ENCODED_PASSWORD);
            verify(user).setRoleStatus(Role.ROLE_USER);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Debe fallar si faltan campos obligatorios")
        void shouldThrowException_WhenFieldsAreBlank() {
            UserRequest invalidRequest = new UserRequest("", " ", null);
            assertThrows(BusinessException.class, () -> userService.create(invalidRequest));
        }

        @Test
        @DisplayName("Debe fallar si el email ya existe")
        void shouldThrowConflict_WhenEmailExists() {
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);
            assertThrows(DataConflictException.class, () -> userService.create(userRequest));
        }
    }

    @Nested
    @DisplayName("Pruebas de Búsqueda")
    class FindTests {
        @Test
        @DisplayName("Debe encontrar usuario por ID")
        void shouldFindById_WhenExists() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            UserResponse result = userService.findById(USER_ID);

            assertEquals(USER_ID, result.id());
            verify(userRepository).findById(USER_ID);
        }

        @Test
        @DisplayName("Debe lanzar excepción si el usuario no existe")
        void shouldThrowNotFound_WhenIdDoesNotExist() {
            when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> userService.findById(99L));
        }

        @Test
        @DisplayName("Debe listar todos los usuarios")
        void shouldReturnAllUsers() {
            when(userRepository.findAll()).thenReturn(List.of(user));
            when(userMapper.toResponse(any())).thenReturn(userResponse);

            List<UserResponse> result = userService.findAll();

            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("Pruebas de Actualización")
    class UpdateTests {
        @Test
        @DisplayName("Debe realizar actualización parcial (solo username)")
        void shouldUpdatePartially_WhenOnlyUsernameProvided() {
            // GIVEN
            UserRequest partialRequest = new UserRequest("newNick", null, null);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.save(any())).thenReturn(user);
            when(userMapper.toResponse(any())).thenReturn(createTestResponse(USER_ID, "newNick", EMAIL));

            // WHEN
            UserResponse result = userService.update(USER_ID, partialRequest);

            // THEN
            assertEquals("newNick", result.username());
            assertEquals(EMAIL, user.getEmail()); // El email no debió cambiar
            verify(userRepository, never()).existsByEmail(anyString());
        }

        @Test
        @DisplayName("Debe validar unicidad si se intenta cambiar el email")
        void shouldValidateEmailUniqueness_WhenEmailChanges() {
            String newEmail = "new@test.com";
            UserRequest emailRequest = new UserRequest(null, newEmail, null);
            
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail(newEmail)).thenReturn(true); // Conflicto

            assertThrows(DataConflictException.class, () -> userService.update(USER_ID, emailRequest));
        }

        @Test
        @DisplayName("Debe actualizar password si se proporciona")
        void shouldUpdatePassword_WhenProvided() {
            UserRequest passRequest = new UserRequest(null, null, "newSecret");
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("newSecret")).thenReturn("hashNew");
            when(userRepository.save(any())).thenReturn(user);
            when(userMapper.toResponse(any())).thenReturn(userResponse);

            userService.update(USER_ID, passRequest);

            verify(user).setPassword("hashNew");
        }
    }

    @Nested
    @DisplayName("Pruebas de Eliminación")
    class DeleteTests {
        @Test
        @DisplayName("Debe eliminar usuario si existe")
        void shouldDelete_WhenExists() {
            when(userRepository.existsById(USER_ID)).thenReturn(true);
            userService.deleteUser(USER_ID);
            verify(userRepository).deleteById(USER_ID);
        }

        @Test
        @DisplayName("Debe lanzar excepción si intenta eliminar inexistente")
        void shouldThrowNotFound_WhenDeletingInexistent() {
            when(userRepository.existsById(anyLong())).thenReturn(false);
            assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(99L));
        }
    }

    // --- HELPERS ---

    private User createTestUser(Long id, String username, String email) {
        // Usamos spy para verificar llamadas a setters en objetos internos si es necesario,
        // o simplemente una instancia normal.
        User u = spy(new User());
        u.setId(id);
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword("oldHash");
        return u;
    }

    private UserResponse createTestResponse(Long id, String username, String email) {
        return new UserResponse(id, username, email, null, Role.ROLE_USER.name());
    }
}
