package com.btvn.jobhub.service.impl;

import com.btvn.jobhub.dto.req.*;
import com.btvn.jobhub.dto.res.AuthResponse;
import com.btvn.jobhub.dto.res.UserResponse;
import com.btvn.jobhub.entity.TokenBlacklist;
import com.btvn.jobhub.entity.User;
import com.btvn.jobhub.repository.TokenBlacklistRepository;
import com.btvn.jobhub.repository.UserRepository;
import com.btvn.jobhub.security.jwt.JwtTokenProvider;
import com.btvn.jobhub.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    // Chức năng Đăng ký tài khoản mới (Ứng viên / Nhà tuyển dụng)
    @Override
    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng trong hệ thống.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .isActive(savedUser.getIsActive())
                .build();
    }

    // Chức năng Đăng nhập hệ thống (Cấp phát cặp chuỗi JWT)
    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("");

            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .role(role)
                    .build();
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Tài khoản hoặc mật khẩu không chính xác.");
        }
    }

    // Chức năng Xoay vòng Token (Refresh Token) từ mã bảo mật hạn dài
    @Override
    public AuthResponse refreshToken(String refreshToken) {
        // 1. Fail-Fast: Chặn ngay nếu Token không hợp lệ
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Mã Refresh Token không hợp lệ hoặc đã hết hiệu lực.");
        }

        // 2. Lấy thông tin User (loadUserByUsername tự động ném lỗi nếu không tìm thấy)
        String email = tokenProvider.getEmailFromJwt(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // 3. Fail-Fast: Chặn ngay nếu tài khoản bị vô hiệu hóa
        if (!userDetails.isEnabled()) {
            throw new RuntimeException("Tài khoản đã bị khóa hoặc vô hiệu hóa.");
        }

        // 4. Khởi tạo quyền và sinh cặp Token mới
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        return AuthResponse.builder()
                .accessToken(tokenProvider.generateAccessToken(auth))
                .refreshToken(tokenProvider.generateRefreshToken(auth))
                .build();
    }

    // Chức năng Đăng xuất chủ động (Vô hiệu hóa Token đưa vào Danh sách đen)
    @Override
    @Transactional
    public void logout(String accessToken) {
        if (tokenProvider.validateToken(accessToken)) {
            String email = tokenProvider.getEmailFromJwt(accessToken);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng."));

            // Đưa thẳng chuỗi Token hiện hành vào bảng Blacklist trong Database để chặn tái sử dụng
            TokenBlacklist blacklistEntry = TokenBlacklist.builder()
                    .tokenString(accessToken)
                    .revokedAt(LocalDateTime.now())
                    .user(user)
                    .build();

            tokenBlacklistRepository.save(blacklistEntry);
            SecurityContextHolder.clearContext();
        }
    }

    // 1. Logic Đổi mật khẩu chủ động (Yêu cầu đã đăng nhập)
    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng."));

        // Fail-Fast: Kiểm tra xem mật khẩu cũ truyền lên có khớp với Password đang lưu trong DB không
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu hiện tại không chính xác.");
        }

        // Fail-Fast: Tránh việc đổi mật khẩu mới trùng lặp khít với mật khẩu cũ
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu mới không được trùng với mật khẩu cũ.");
        }

        // Tiến hành băm mã hóa mật khẩu mới và cập nhật
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // 2. Logic Quên mật khẩu - Bước 1: Tạo mã Token khôi phục
    @Override
    @Transactional
    public void processForgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản nào liên kết với Email này."));

        // Khởi tạo chuỗi Token ngẫu nhiên bảo mật cao và đặt thời gian hết hạn là 15 phút
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        // 💡 Giả lập gửi Mail: In ra màn hình Console Log để bạn lấy Token đem đi test Postman bước tiếp theo
        System.out.println("========================================================================");
        System.out.println("HỆ THỐNG GỬI MÃ KHÔI PHỤC MẬT KHẨU ĐẾN: " + user.getEmail());
        System.out.println("TOKEN KHÔI PHỤC (HẠN 15 PHÚT): " + token);
        System.out.println("========================================================================");
    }

    // 3. Logic Quên mật khẩu - Bước 2: Kiểm tra Token hợp lệ và Đặt lại mật khẩu
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Tìm user dựa trên Token được gửi lên từ Client
        User user = userRepository.findAll().stream()
                .filter(u -> request.getToken().equals(u.getResetToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Mã xác thực (Token) không hợp lệ hoặc không tồn tại."));

        // Fail-Fast: Kiểm tra xem mã Token đã quá hạn 15 phút chưa
        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã xác thực (Token) đã hết hiệu lực. Vui lòng yêu cầu lại.");
        }

        // Cập nhật mật khẩu mới dứt điểm
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        // Xóa sạch dấu vết Token cũ sau khi khôi phục thành công để tăng tính bảo mật
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}