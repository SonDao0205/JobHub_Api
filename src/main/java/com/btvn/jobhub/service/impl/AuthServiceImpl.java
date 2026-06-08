package com.btvn.jobhub.service.impl;

import com.btvn.jobhub.dto.req.LoginRequest;
import com.btvn.jobhub.dto.req.RegisterUserRequest;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
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
}