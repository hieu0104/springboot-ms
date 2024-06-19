package com.hieu.ms.mapper;

import com.hieu.ms.dto.request.PermissionRequest;
import com.hieu.ms.dto.response.PermissionResponse;
import org.mapstruct.Mapper;

import com.hieu.ms.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
