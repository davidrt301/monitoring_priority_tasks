package com.davidrt301.priority_tasks.service.auth;


import java.util.List;

import com.davidrt301.priority_tasks.model.dtos.UserRequest;
import com.davidrt301.priority_tasks.model.dtos.UserResponse;
import com.davidrt301.priority_tasks.model.dtos.Login.LoginRequest;
import com.davidrt301.priority_tasks.model.dtos.Login.LoginResponse;

public interface LoginService {

    LoginResponse login(LoginRequest request);

    void register (UserRequest request);

    List<UserResponse> getAll();
}
