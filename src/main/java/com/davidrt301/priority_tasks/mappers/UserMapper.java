package com.davidrt301.priority_tasks.mappers;

import com.davidrt301.priority_tasks.model.dtos.UserRequest;
import com.davidrt301.priority_tasks.model.dtos.UserResponse;
import com.davidrt301.priority_tasks.model.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roleStatus", ignore = true)
    User toEntity(UserRequest request);
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "role", source = "roleStatus")
    UserResponse toResponse(User user);
}
