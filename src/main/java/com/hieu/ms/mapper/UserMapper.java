package com.hieu.ms.mapper;

import com.hieu.ms.dto.request.UserUpdateRequest;
import com.hieu.ms.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.hieu.ms.dto.request.UserCreationRequest;
import com.hieu.ms.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
