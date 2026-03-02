package com.hieu.ms.feature.role;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hieu.ms.feature.role.dto.RoleRequest;
import com.hieu.ms.feature.role.dto.RoleResponse;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
