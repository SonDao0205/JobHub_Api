//package com.btvn.jobhub.service;
//
//import com.btvn.jobhub.dto.req.*;
//import com.btvn.jobhub.dto.res.AuthResponse;
//import com.btvn.jobhub.dto.res.UserResponse;
//import com.btvn.jobhub.entity.User;
//import com.btvn.jobhub.repository.jpa.UserRepository;
//import com.btvn.jobhub.repository.redis.TokenBlacklistRepository;
//import com.btvn.jobhub.security.jwt.JwtTokenProvider;
//import com.btvn.jobhub.service.impl.AuthServiceImpl;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.Collections;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class AuthServiceTest {
//
//    @Mock
//    private AuthenticationManager authenticationManager;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private JwtTokenProvider tokenProvider;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private TokenBlacklistRepository tokenBlacklistRepository;
//
//    @Mock
//    private UserDetailsService userDetailsService;
//
//    @InjectMocks
//    private AuthServiceImpl authService;
//
//    @Test
//    void registerUserSuccess() {
//        RegisterUserRequest request = new RegisterUserRequest();
//        request.setEmail("test@gmail.com");
//        request.setPassword("password123");
//
//        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
//        when(passwordEncoder.encode(any())).thenReturn("encoded-password");
//        when(userRepository.save(any())).thenReturn(new User());
//
//        UserResponse response = authService.register(request);
//
//        assertNotNull(response);
//        verify(userRepository).existsByEmail(request.getEmail());
//        verify(passwordEncoder).encode(any());
//        verify(userRepository).save(any());
//    }
//
//    @Test
//    void loginSuccess() {
//        LoginRequest request = new LoginRequest();
//        request.setEmail("test@gmail.com");
//        request.setPassword("password123");
//
//        Authentication authentication = mock(Authentication.class);
//        when(authentication.getAuthorities()).thenReturn(Collections.emptyList());
//
//        when(authenticationManager.authenticate(any())).thenReturn(authentication);
//        when(tokenProvider.generateAccessToken(authentication)).thenReturn("access-token-mock");
//        when(tokenProvider.generateRefreshToken(authentication)).thenReturn("refresh-token-mock");
//
//        AuthResponse response = authService.login(request);
//
//        assertNotNull(response);
//        assertEquals("access-token-mock", response.getAccessToken());
//        assertEquals("refresh-token-mock", response.getRefreshToken());
//    }
//
//    @Test
//    void logoutSuccess() {
//        String token = "valid-token-string";
//
//        when(tokenProvider.validateToken(token)).thenReturn(true);
//        when(tokenProvider.getEmailFromJwt(token)).thenReturn("test@gmail.com");
//        when(tokenProvider.getRemainingTimeMs(token)).thenReturn(1000L * 60);
//        when(tokenBlacklistRepository.save(any())).thenReturn(null);
//
//        assertDoesNotThrow(() -> authService.logout(token));
//        verify(tokenProvider).validateToken(token);
//        verify(tokenBlacklistRepository).save(any());
//    }
//
//    @Test
//    void changePasswordSuccess() {
//        ChangePasswordRequest request = new ChangePasswordRequest();
//        request.setOldPassword("oldPass123");
//        request.setNewPassword("newPass123");
//        Long userId = 1L;
//
//        User mockUser = new User();
//        mockUser.setPasswordHash("encoded-old-password");
//
//        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
//        when(passwordEncoder.matches(eq("oldPass123"), any())).thenReturn(true);
//        when(passwordEncoder.matches(eq("newPass123"), any())).thenReturn(false);
//        when(passwordEncoder.encode(eq("newPass123"))).thenReturn("new-encoded-password");
//
//        assertDoesNotThrow(() -> authService.changePassword(request, userId));
//        verify(userRepository).save(mockUser);
//    }
//
//    @Test
//    void refreshTokenSuccess() {
//        String refreshToken = "valid-refresh-token";
//        String email = "test@gmail.com";
//
//        UserDetails userDetails = mock(UserDetails.class);
//        when(userDetails.isEnabled()).thenReturn(true);
//        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());
//
//        when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
//        when(tokenProvider.getEmailFromJwt(refreshToken)).thenReturn(email);
//        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
//
//        when(tokenProvider.generateAccessToken(any())).thenReturn("new-access-token");
//        when(tokenProvider.generateRefreshToken(any())).thenReturn("new-refresh-token");
//
//        AuthResponse response = authService.refreshToken(refreshToken);
//
//        assertNotNull(response);
//        assertEquals("new-access-token", response.getAccessToken());
//        assertEquals("new-refresh-token", response.getRefreshToken());
//        verify(tokenProvider).validateToken(refreshToken);
//        verify(userDetailsService).loadUserByUsername(email);
//    }
//}