package com.hieu.ms.feature.role;

import org.mapstruct.Mapper;

import com.hieu.ms.feature.role.dto.PermissionRequest;
import com.hieu.ms.feature.role.dto.PermissionResponse;

@Mapper(componentModel = "spring")
interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
