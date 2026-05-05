package com.davidrt301.priority_tasks.service.auth;

import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.davidrt301.priority_tasks.config.security.JwtToken;
import com.davidrt301.priority_tasks.exceptions.InternalServerException;
import com.davidrt301.priority_tasks.model.dtos.UserRequest;
import com.davidrt301.priority_tasks.model.dtos.UserResponse;
import com.davidrt301.priority_tasks.model.dtos.Login.LoginReques;
import com.davidrt301.priority_tasks.model.dtos.Login.LoginResponse;
import com.davidrt301.priority_tasks.model.entities.User;
import com.davidrt301.priority_tasks.repository.UserRepository;
import com.davidrt301.priority_tasks.service.user.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class LoginServiceImpl implements LoginService{

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtToken jwtToken;
    private final UserService userService;

    @Override
    public LoginResponse login(LoginReques reques) {
        var auth = new UsernamePasswordAuthenticationToken(reques.username(), reques.password());
        authenticationManager.authenticate(auth);

        User user = userRepository.findByUsername(reques.username())
                .orElseThrow(() -> new InternalServerException("Inconsistencia de datos: usuario autenticado no encontrado."));
                    
        String token = jwtToken.generateToken(reques.username(), Map.of(
                            "email", user.getEmail(),
                            "role", user.getRoleStatus().name()
        ));
        return new LoginResponse(token,user.getUsername());
    }

    @Override
    @Transactional
    public void register(UserRequest request) {

        userService.create(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userService.findAll();
    }


}
