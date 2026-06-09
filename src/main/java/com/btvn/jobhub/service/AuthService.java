package com.btvn.jobhub.service;

import com.btvn.jobhub.dto.req.*;
import com.btvn.jobhub.dto.res.AuthResponse;
import com.btvn.jobhub.dto.res.UserResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request); // [cite: 259]
    void logout(String accessToken); // [cite: 265]
    AuthResponse refreshToken(String refreshToken); // [cite: 249]
    UserResponse register(RegisterUserRequest request);
    void changePassword(ChangePasswordRequest request, Long userId);
    void processForgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
