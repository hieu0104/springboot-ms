package com.hieu.ms.mapper;

import com.hieu.ms.dto.response.RoleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hieu.ms.dto.request.RoleRequest;
import com.hieu.ms.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
