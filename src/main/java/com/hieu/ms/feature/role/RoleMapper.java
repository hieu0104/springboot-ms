package com.hieu.ms.feature.role;

import com.hieu.ms.feature.role.dto.RoleRequest;
import com.hieu.ms.feature.role.dto.RoleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
