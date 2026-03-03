package com.hieu.ms.feature.authentication;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hieu.ms.feature.authentication.dto.*;
import com.hieu.ms.feature.role.dto.IntrospectResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Authentication", description = "APIs xác thực và phân quyền")
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/token")
    @Operation(summary = "Đăng nhập", description = "Xác thực người dùng và trả về JWT token")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
                @ApiResponse(responseCode = "401", description = "Thông tin đăng nhập không hợp lệ")
            })
    com.hieu.ms.shared.dto.response.ApiResponse<AuthenticationResponse> login(
            @Valid @RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return com.hieu.ms.shared.dto.response.ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/introspect")
    @Operation(summary = "Kiểm tra token", description = "Kiểm tra tính hợp lệ của JWT token")
    com.hieu.ms.shared.dto.response.ApiResponse<IntrospectResponse> introspect(
            @Valid @RequestBody IntrospectRequest request) throws Exception {
        var result = authenticationService.introspect(request);
        return com.hieu.ms.shared.dto.response.ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới token", description = "Làm mới JWT token khi hết hạn")
    com.hieu.ms.shared.dto.response.ApiResponse<AuthenticationResponse> refresh(
            @Valid @RequestBody RefreshRequest request) throws Exception {
        var result = authenticationService.refreshToken(request);
        return com.hieu.ms.shared.dto.response.ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất", description = "Đăng xuất và vô hiệu hóa token hiện tại")
    com.hieu.ms.shared.dto.response.ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request)
            throws Exception {
        authenticationService.logout(request);
        return com.hieu.ms.shared.dto.response.ApiResponse.<Void>builder().build();
    }
}
