package com.davidrt301.priority_tasks.service.user;

import com.davidrt301.priority_tasks.model.dtos.UserResponse;

import java.util.List;
import com.davidrt301.priority_tasks.model.dtos.UserRequest;


public interface UserService {
    
    UserResponse create(UserRequest request);

    UserResponse update(Long id, UserRequest request);
    
    UserResponse findById(Long id);

    List<UserResponse> findAll();
    
    void deleteUser(Long id);
}
