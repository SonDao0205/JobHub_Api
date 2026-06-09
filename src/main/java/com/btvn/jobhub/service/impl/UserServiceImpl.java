package com.btvn.jobhub.service.impl;

import com.btvn.jobhub.dto.req.CreateUserRequest;
import com.btvn.jobhub.dto.req.RegisterUserRequest;
import com.btvn.jobhub.dto.res.UserResponse;
import com.btvn.jobhub.entity.User;
import com.btvn.jobhub.repository.UserRepository;
import com.btvn.jobhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // Fail-Fast: Kiểm tra trùng lặp email hệ thống
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng trong hệ thống.");
        }

        // Mã hóa mật khẩu bằng BCrypt (strength 10 đã được cấu hình trong SecurityConfig) [cite: 40]
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        return convertToUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);

        // Bắt buộc xử lý và biến đổi dữ liệu bằng Java Stream API theo đặc tả SRS
        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(this::convertToUserResponse) // Ánh xạ từng Entity sang DTO
                .collect(Collectors.toList());

        return new PageImpl<>(userResponses, pageable, userPage.getTotalElements());
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId, boolean isActive) {
        // Fail-Fast: Kiểm tra sự tồn tại của User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng có ID: " + userId));

        user.setIsActive(isActive);
        userRepository.save(user); // Cập nhật an toàn vào Cơ sở dữ liệu
    }

    /**
     * Hàm hỗ trợ (Helper) ánh xạ từ Entity sang DTO để tái sử dụng, tránh lộ cấu trúc dữ liệu Entity
     */
    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .build();
    }
}
