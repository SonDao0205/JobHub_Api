package com.btvn.jobhub.controller;

import com.btvn.jobhub.dto.req.LoginRequest;
import com.btvn.jobhub.dto.req.RefreshTokenRequest;
import com.btvn.jobhub.dto.req.RegisterUserRequest;
import com.btvn.jobhub.dto.res.ApiResponse;
import com.btvn.jobhub.dto.res.AuthResponse;
import com.btvn.jobhub.dto.res.UserResponse;
import com.btvn.jobhub.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // FR-04: Đăng ký tài khoản mới
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("Đăng ký tài khoản thành công.")
                        .data(response)
                        .build()
        );
    }

    // FR-01 / UC-01: Đăng nhập hệ thống
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticateUser(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Đăng nhập thành công.")
                        .data(response)
                        .build()
        );
    }

    // FR-02: Xoay vòng Token (Refresh Token)
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshSession(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Cấp lại Access Token mới thành công.")
                        .data(response)
                        .build()
        );
    }

    // FR-03 / UC-03: Đăng xuất và thu hồi Token quyền truy cập
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logoutUser(HttpServletRequest request) {
        String token = parseJwtFromHeader(request);
        if (StringUtils.hasText(token)) {
            authService.logout(token);
        }
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Đăng xuất thành công, Token đã được đưa vào danh sách đen.")
                        .build()
        );
    }

    private String parseJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}