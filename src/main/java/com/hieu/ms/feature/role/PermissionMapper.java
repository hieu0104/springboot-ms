package com.hieu.ms.feature.role;

import com.hieu.ms.feature.role.dto.PermissionRequest;
import com.hieu.ms.feature.role.dto.PermissionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
