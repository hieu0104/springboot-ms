package com.hieu.ms.feature.user;

import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import com.hieu.ms.feature.user.dto.UserCreationRequest;
import com.hieu.ms.feature.user.dto.UserResponse;
import com.hieu.ms.feature.user.dto.UserSearchRequest;
import com.hieu.ms.feature.user.dto.UserUpdateRequest;
import com.hieu.ms.shared.dto.response.ApiResponse;
import com.hieu.ms.shared.dto.response.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "User Management", description = "APIs quản lý người dùng")
public class UserController {
    UserService userService;

    @PostMapping
    @Operation(summary = "Tạo người dùng mới", description = "Tạo một người dùng mới trong hệ thống")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Tạo người dùng thành công"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Dữ liệu không hợp lệ")
            })
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @GetMapping
    @Operation(
            summary = "Lấy danh sách người dùng",
            description = "Lấy người dùng với hỗ trợ pagination, sorting và search qua DTO")
    ApiResponse<PageResponse<UserResponse>> getUsers(@ParameterObject @ModelAttribute UserSearchRequest request) {
        return ApiResponse.<PageResponse<UserResponse>>builder()
                .result(userService.getUsers(request))
                .build();
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Lấy thông tin người dùng", description = "Lấy thông tin chi tiết của một người dùng theo ID")
    ApiResponse<UserResponse> getUser(@PathVariable("userId") String userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUser(userId))
                .build();
    }

    @GetMapping("/my-info")
    @Operation(summary = "Lấy thông tin của tôi", description = "Lấy thông tin của người dùng hiện đang đăng nhập")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Xóa người dùng", description = "Xóa một người dùng khỏi hệ thống")
    ApiResponse<String> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ApiResponse.<String>builder().result("User has been deleted").build();
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Cập nhật người dùng", description = "Cập nhật thông tin người dùng")
    ApiResponse<UserResponse> updateUser(@PathVariable String userId, @RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }
}
