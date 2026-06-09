package com.btvn.jobhub.controller;

import com.btvn.jobhub.dto.req.CreateUserRequest;
import com.btvn.jobhub.dto.req.RegisterUserRequest;
import com.btvn.jobhub.dto.req.UpdateUserStatusRequest;
import com.btvn.jobhub.dto.res.ApiResponse;
import com.btvn.jobhub.dto.res.UserResponse;
import com.btvn.jobhub.entity.User;
import com.btvn.jobhub.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        // 1. Khởi tạo đối tượng Pageable (mặc định sắp xếp giảm dần theo trường được truyền vào)
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortBy).descending());

        // 2. Gọi sang Service để lấy dữ liệu đã được xử lý bằng Stream API
        Page<UserResponse> userPage = userService.getAllUsers(pageable);

        // 3. Trả về cấu hình dữ liệu chuẩn JSON DTO theo định dạng hệ thống
        return ResponseEntity.ok(
                ApiResponse.<Page<UserResponse>>builder()
                        .success(true)
                        .message("Lấy danh sách người dùng thành công.")
                        .data(userPage)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("Tạo tài khoản thành công.")
                        .data(response)
                        .build()
        );
    }

    // FR-05 / UC-02: Kích hoạt / Vô hiệu hóa trạng thái người dùng (Toggle Status) [cite: 34, 262, 264]
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {

        // Gọi xuống Service xử lý thay đổi trạng thái trong CSDL
        userService.toggleUserStatus(id, request.getIsActive());

        // Trả về HTTP 200 OK thông báo cập nhật thành công
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message(request.getIsActive() ? "Kích hoạt tài khoản thành công." : "Vô hiệu hóa tài khoản thành công.")
                        .build()
        );
    }

}
