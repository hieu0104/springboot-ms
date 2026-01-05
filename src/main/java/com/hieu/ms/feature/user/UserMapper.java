package com.hieu.ms.feature.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.hieu.ms.feature.user.dto.UserCreationRequest;
import com.hieu.ms.feature.user.dto.UserResponse;
import com.hieu.ms.feature.user.dto.UserUpdateRequest;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "assignedIssues", ignore = true)
    @Mapping(target = "projectSize", ignore = true)
    @Mapping(target = "subscription", ignore = true)
    @Mapping(source = "email", target = "email")
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
