package com.hieu.ms.feature.role;

import java.util.List;

import com.hieu.ms.feature.role.dto.PermissionRequest;
import com.hieu.ms.feature.role.dto.PermissionResponse;
import org.springframework.web.bind.annotation.*;

import com.hieu.ms.shared.dto.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Permission Management", description = "APIs quản lý quyền hạn")
public class PermissionController {
    PermissionService permissionService;

    @PostMapping
    @Operation(summary = "Tạo quyền mới", description = "Tạo một quyền hạn mới trong hệ thống")
    ApiResponse<PermissionResponse> create(@RequestBody PermissionRequest request) {
        return ApiResponse.<PermissionResponse>builder()
                .result(permissionService.create(request))
                .build();
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách quyền", description = "Lấy tất cả quyền hạn trong hệ thống")
    ApiResponse<List<PermissionResponse>> getAll() {
        return ApiResponse.<List<PermissionResponse>>builder()
                .result(permissionService.getAll())
                .build();
    }

    @DeleteMapping("/{permission}")
    @Operation(summary = "Xóa quyền", description = "Xóa một quyền hạn khỏi hệ thống")
    ApiResponse<Void> delete(@PathVariable String permission) {
        permissionService.delete(permission);
        return ApiResponse.<Void>builder().build();
    }
}
