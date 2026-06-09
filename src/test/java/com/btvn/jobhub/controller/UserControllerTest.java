package com.btvn.jobhub.controller;

import com.btvn.jobhub.dto.req.CreateUserRequest;
import com.btvn.jobhub.dto.res.UserResponse;
import com.btvn.jobhub.entity.enumType.RoleEnum;
import com.btvn.jobhub.repository.TokenBlacklistRepository;
import com.btvn.jobhub.security.jwt.JwtTokenProvider;
import com.btvn.jobhub.security.principal.UserPrincipalService;
import com.btvn.jobhub.service.UserService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // Mock Security Context
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private UserPrincipalService userPrincipalService;
    @MockitoBean private TokenBlacklistRepository tokenBlacklistRepository;

    // Mock Business Service
    @MockitoBean private UserService userService;

    @Test
    @DisplayName("Controller 2 [User]: Admin tạo mới tài khoản nội bộ thành công - HTTP 201")
    void createUser_Controller_Success() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("staff@jobhub.com");
        request.setPassword("passwordHash1");
        request.setRole(RoleEnum.EMPLOYER);

        UserResponse mockUserResponse = UserResponse.builder()
                .id(55L)
                .email("staff@jobhub.com")
                .role(RoleEnum.EMPLOYER)
                .isActive(true)
                .build();

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(mockUserResponse);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(55))
                .andExpect(jsonPath("$.data.email").value("staff@jobhub.com"));
    }

    @Test
    @DisplayName("Controller 3 [User]: Thực hiện Đóng/Mở trạng thái tài khoản (Toggle Status)")
    void toggleUserStatus_Controller_Success() throws Exception {
        String requestJson = "{\"isActive\":false}";

        doNothing().when(userService).toggleUserStatus(eq(10L), eq(false));

        mockMvc.perform(put("/api/users/10/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Vô hiệu hóa tài khoản thành công."));
    }
}