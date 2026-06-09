package com.btvn.jobhub.controller;

import com.btvn.jobhub.dto.req.ApplicationRequest;
import com.btvn.jobhub.repository.TokenBlacklistRepository;
import com.btvn.jobhub.security.jwt.JwtTokenProvider;
import com.btvn.jobhub.security.principal.UserPrincipalService;
import com.btvn.jobhub.service.ApplicationService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApplicationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // Mock Security Context
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    @MockitoBean private UserPrincipalService userPrincipalService;
    @MockitoBean private TokenBlacklistRepository tokenBlacklistRepository;

    // Mock Business Service
    @MockitoBean private ApplicationService applicationService;

    @Test
    @DisplayName("Controller 4 [Application]: Ứng viên nộp đơn thành công bằng Multipart dữ liệu hỗn hợp")
    void applyJob_Multipart_Success() throws Exception {
        ApplicationRequest appRequest = new ApplicationRequest();
        appRequest.setJobPostingId(5L);
        appRequest.setCoverLetter("Hồ sơ năng lực tốt.");

        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "", "application/json", objectMapper.writeValueAsString(appRequest).getBytes()
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "cvFile", "mycv.pdf", "application/pdf", "pdf binary content thô".getBytes()
        );

        when(applicationService.applyForJob(eq(5L), eq("Hồ sơ năng lực tốt."), any(), any())).thenReturn(null);

        // Bổ sung .principal để Mock User đăng nhập, tránh Resolver quét nhầm sang DTO khác gây lỗi isActive
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/applications/apply")
                        .file(requestPart)
                        .file(filePart)
                        .principal(() -> "candidate@gmail.com"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Controller 5 [Application]: Nhà tuyển dụng chuyển đổi trạng thái hồ sơ sang MỜI PHỎNG VẤN")
    void interviewApp_Controller_Success() throws Exception {

        // Bổ sung .principal tương tự để vượt qua validation của ArgumentResolver
        mockMvc.perform(put("/api/applications/88/interview")
                        .principal(() -> "employer@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đã chuyển trạng thái hồ sơ sang: MỜI PHỎNG VẤN."));
    }
}