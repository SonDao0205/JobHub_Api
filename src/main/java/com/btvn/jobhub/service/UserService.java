package com.btvn.jobhub.service;

import com.btvn.jobhub.dto.req.RegisterUserRequest;
import com.btvn.jobhub.dto.res.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(RegisterUserRequest request); //
    List<UserResponse> getAllUsers(); // Bắt buộc dùng Stream API
    void toggleUserStatus(Long userId, boolean isActive); //
}