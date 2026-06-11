package com.btvn.jobhub.service.impl;

import com.btvn.jobhub.dto.req.ChangePasswordRequest;
import com.btvn.jobhub.dto.req.ResetPasswordRequest;
import com.btvn.jobhub.dto.res.UserResponse;
import com.btvn.jobhub.entity.User;
import com.btvn.jobhub.entity.enumType.RoleEnum;
import com.btvn.jobhub.repository.jpa.ApplicationRepository;
import com.btvn.jobhub.repository.jpa.JobPostingRepository;
import com.btvn.jobhub.repository.jpa.UserRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MultiModuleServiceTest {

    // === MOCK REPOSITORIES & UTILS ===
    @Mock private UserRepository userRepository;
    @Mock private JobPostingRepository jobPostingRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private Cloudinary cloudinary;
    @Mock private Uploader uploader;

    // === INJECT MOCKS INTO SERVICE IMPLS ===
    @InjectMocks private AuthServiceImpl authService;
    @InjectMocks private CloudinaryService cloudinaryService;
    @InjectMocks private ApplicationServiceImpl applicationService;
    @InjectMocks private UserServiceImpl userService;

    // =========================================================================
    // MODULE 1: AUTH SERVICE (LOGIC QUÊN & ĐỔI MẬT KHẨU PHỨC TẠP)
    // =========================================================================

    @Test
    @DisplayName("Service 1 [Auth]: Đổi mật khẩu thất bại khi nhập sai mật khẩu cũ")
    void changePassword_ThrowException_WhenOldPasswordIncorrect() {
        User mockUser = User.builder().id(1L).passwordHash("hashed_old_pass").build();
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrong_old_pass");
        request.setNewPassword("new_pass_123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrong_old_pass", "hashed_old_pass")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.changePassword(request, 1L));
        assertEquals("Mật khẩu hiện tại không chính xác.", ex.getMessage());
    }

    @Test
    @DisplayName("Service 2 [Auth]: Khôi phục mật khẩu thành công - Token bị hủy dứt điểm ngay sau đó")
    void resetPassword_Success_AndClearToken() {
        User mockUser = User.builder()
                .email("test@gmail.com")
                .resetToken("valid_uuid_token")
                .resetTokenExpiry(LocalDateTime.now().plusMinutes(5))
                .build();

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid_uuid_token");
        request.setNewPassword("freshPassword123");

        // 💡 ĐÃ SỬA: Thay đổi từ findAll() thành findByResetToken() khớp khít với logic Service mới
        when(userRepository.findByResetToken("valid_uuid_token")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode("freshPassword123")).thenReturn("new_hashed_password");

        authService.resetPassword(request);

        assertNull(mockUser.getResetToken()); // Đảm bảo Single-use token hoạt động đúng
        assertNull(mockUser.getResetTokenExpiry());
        assertEquals("new_hashed_password", mockUser.getPasswordHash());
        verify(userRepository, times(1)).save(mockUser);
    }

    // =========================================================================
    // MODULE 2: CLOUDINARY SERVICE (KIỂM TRA CHẶN ĐỊNH DẠNG TÀI LIỆU)
    // =========================================================================

    @Test
    @DisplayName("Service 3 [Cloudinary]: Từ chối tải lên (Fail-Fast) khi file sai định dạng (Ví dụ: .exe, .sh)")
    void uploadFile_ThrowException_WhenInvalidContentType() {
        MockMultipartFile dangerousFile = new MockMultipartFile(
                "cvFile", "malware.exe", "application/octet-stream", "dangerous content".getBytes()
        );

        RuntimeException ex = assertThrows(RuntimeException.class, () -> cloudinaryService.uploadFile(dangerousFile));
        assertTrue(ex.getMessage().contains("Định dạng file không hợp lệ"));
        verifyNoInteractions(cloudinary);
    }

    // =========================================================================
    // MODULE 3: APPLICATION SERVICE (CHỐNG SPAM CV & ĐIỀU KIỆN TIN APPROVED)
    // =========================================================================

    @Test
    @DisplayName("Service 4 [Application]: Ứng tuyển thất bại khi Candidate cố tình nộp đơn lại lần 2")
    void applyForJob_ThrowException_WhenAlreadyApplied() {
        MockMultipartFile pdfFile = new MockMultipartFile("cvFile", "cv.pdf", "application/pdf", "content".getBytes());

        // Cấu hình chống spam trả về true (đã tồn tại bản ghi nộp đơn trước đó)
        when(applicationRepository.existsByCandidateIdAndJobPostingId(1L, 100L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                applicationService.applyForJob(100L, "Cover letter", pdfFile, 1L)
        );
        assertEquals("Bạn đã nộp hồ sơ ứng tuyển cho công việc này rồi.", ex.getMessage());
        verify(jobPostingRepository, never()).findById(any());
    }

    // =========================================================================
    // MODULE 4: USER SERVICE (STREAM PROCESSING PHÂN TRANG)
    // =========================================================================

    @Test
    @DisplayName("Service 5 [User]: Lấy toàn bộ người dùng có phân trang và ánh xạ Entity sang DTO mượt mà")
    void getAllUsers_Success_WithStreamMapping() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("id").descending());
        User userEntity = User.builder().id(9L).email("admin@jobhub.com").role(RoleEnum.ADMIN).isActive(true).build();
        Page<User> mockPage = new PageImpl<>(List.of(userEntity), pageable, 1);

        when(userRepository.findAll(pageable)).thenReturn(mockPage);

        Page<UserResponse> result = userService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("admin@jobhub.com", result.getContent().get(0).getEmail());
        assertEquals(RoleEnum.ADMIN, result.getContent().get(0).getRole());
    }
}