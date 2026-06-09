package com.btvn.jobhub.controller;

import com.btvn.jobhub.dto.req.LoginRequest;
import com.btvn.jobhub.dto.res.AuthResponse;
import com.btvn.jobhub.repository.TokenBlacklistRepository;
import com.btvn.jobhub.security.jwt.JwtTokenProvider;
import com.btvn.jobhub.security.principal.UserPrincipalService;
import com.btvn.jobhub.service.AuthService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // Mock Security Context
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private UserPrincipalService userPrincipalService;
    @MockitoBean private TokenBlacklistRepository tokenBlacklistRepository;

    // Mock Business Service
    @MockitoBean private AuthService authService;

    @Test
    @DisplayName("Controller 1 [Auth]: Đăng nhập hệ thống thành công - Trả về Token và Quyền hạn")
    void login_Controller_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("candidate@gmail.com");
        request.setPassword("secure123");

        AuthResponse expectedResponse = AuthResponse.builder()
                .accessToken("mock.access.token")
                .refreshToken("mock.refresh.token")
                .role("ROLE_CANDIDATE")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock.access.token"))
                .andExpect(jsonPath("$.data.role").value("ROLE_CANDIDATE"));
    }
}