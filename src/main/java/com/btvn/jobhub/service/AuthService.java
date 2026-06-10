package com.btvn.jobhub.service;

import com.btvn.jobhub.dto.req.*;
import com.btvn.jobhub.dto.res.AuthResponse;
import com.btvn.jobhub.dto.res.UserResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    void logout(String accessToken);
    AuthResponse refreshToken(String refreshToken);
    UserResponse register(RegisterUserRequest request);
    void changePassword(ChangePasswordRequest request, Long userId);
    void processForgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
