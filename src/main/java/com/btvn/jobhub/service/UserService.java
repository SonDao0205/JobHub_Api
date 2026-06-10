package com.btvn.jobhub.service;

import com.btvn.jobhub.dto.req.CreateUserRequest;
import com.btvn.jobhub.dto.req.RegisterUserRequest;
import com.btvn.jobhub.dto.res.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    Page<UserResponse> getAllUsers(Pageable pageable);
    void toggleUserStatus(Long userId, boolean isActive);
}