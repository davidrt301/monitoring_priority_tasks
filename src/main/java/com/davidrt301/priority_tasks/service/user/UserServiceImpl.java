package com.davidrt301.priority_tasks.service.user;


import java.util.List;
import java.util.Optional;

import com.davidrt301.priority_tasks.model.entities.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.davidrt301.priority_tasks.exceptions.BusinessException;
import com.davidrt301.priority_tasks.exceptions.DataConflictException;
import com.davidrt301.priority_tasks.exceptions.ResourceNotFoundException;
import com.davidrt301.priority_tasks.mappers.UserMapper;
import com.davidrt301.priority_tasks.model.dtos.UserRequest;
import com.davidrt301.priority_tasks.model.dtos.UserResponse;
import com.davidrt301.priority_tasks.model.entities.User;
import com.davidrt301.priority_tasks.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {



    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse create(UserRequest request) {
        validateRequiredFields(request);
        validateUniqueness(request.username(), request.email());

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoleStatus(Role.ROLE_USER); 

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        applyPartialUpdate(user, request);

        return userMapper.toResponse(userRepository.save(user));
    }


    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }


    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) throw new ResourceNotFoundException("Usuario no existe con ID: " + id);
        userRepository.deleteById(id);
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    //-------------------------- METODOS VALIDACIONES --------------------------//

    private void validateRequiredFields(UserRequest request) {
        if (isBlank(request.username())) throw new BusinessException("El nombre de usuario es obligatorio.");
        if (isBlank(request.email())) throw new BusinessException("El email es obligatorio.");
        if (isBlank(request.password())) throw new BusinessException("La contraseña es obligatoria.");
    }

    private void validateUniqueness(String username, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DataConflictException("El email ya se encuentra registrado.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new DataConflictException("El nombre de usuario ya está en uso.");
        }
    }

    private void applyPartialUpdate(User user, UserRequest request) {
        Optional.ofNullable(request.username())
                .filter(s -> !s.isBlank())
                .ifPresent(user::setUsername);

        Optional.ofNullable(request.email())
                .filter(s -> !s.isBlank())
                .ifPresent(email -> {
                    if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                        throw new DataConflictException("El nuevo email ya está en uso.");
                    }
                    user.setEmail(email);
                });

        Optional.ofNullable(request.password())
                .filter(s -> !s.isBlank())
                .map(passwordEncoder::encode)
                .ifPresent(user::setPassword);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}
