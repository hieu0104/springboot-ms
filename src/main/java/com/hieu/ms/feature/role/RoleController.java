package com.hieu.ms.feature.role;

import com.hieu.ms.feature.role.dto.RoleRequest;
import com.hieu.ms.feature.role.dto.RoleResponse;
import com.hieu.ms.feature.role.dto.RoleSearchRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import com.hieu.ms.shared.dto.response.ApiResponse;
import com.hieu.ms.shared.dto.response.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Role Management", description = "APIs quản lý vai trò")
public class RoleController {
    RoleService roleService;

    @PostMapping
    @Operation(summary = "Tạo vai trò mới", description = "Tạo một vai trò mới trong hệ thống")
    ApiResponse<RoleResponse> create(@RequestBody RoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.create(request))
                .build();
    }

    @GetMapping
    @Operation(
            summary = "Lấy danh sách vai trò",
            description = "Lấy vai trò với hỗ trợ pagination, sorting và search qua DTO.")
    ApiResponse<PageResponse<RoleResponse>> getAll(@ParameterObject @ModelAttribute RoleSearchRequest request) {
        return ApiResponse.<PageResponse<RoleResponse>>builder()
                .result(roleService.getRoles(request))
                .build();
    }

    @DeleteMapping("/{role}")
    @Operation(summary = "Xóa vai trò", description = "Xóa một vai trò khỏi hệ thống")
    ApiResponse<Void> delete(@PathVariable String role) {
        roleService.delete(role);
        return ApiResponse.<Void>builder().build();
    }
}
