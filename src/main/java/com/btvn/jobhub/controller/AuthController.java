package com.btvn.jobhub.controller;

import com.btvn.jobhub.dto.req.*;
import com.btvn.jobhub.dto.res.ApiResponse;
import com.btvn.jobhub.dto.res.AuthResponse;
import com.btvn.jobhub.dto.res.UserResponse;
import com.btvn.jobhub.security.principal.UserPrincipal;
import com.btvn.jobhub.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


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

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        authService.changePassword(request, userPrincipal.getId());
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Thay đổi mật khẩu thành công.")
                        .build()
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.processForgotPassword(request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Mã khôi phục đã được khởi tạo. Vui lòng kiểm tra Console Log hệ thống.")
                        .build()
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Đặt lại mật khẩu mới thành công! Bạn có thể dùng mật khẩu này để đăng nhập.")
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