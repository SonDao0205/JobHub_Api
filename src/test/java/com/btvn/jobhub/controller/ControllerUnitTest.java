package com.btvn.jobhub.controller;

import com.btvn.jobhub.dto.req.*;
import com.btvn.jobhub.dto.res.*;
import com.btvn.jobhub.entity.enumType.JobStatusEnum;
import com.btvn.jobhub.security.principal.UserPrincipal;
import com.btvn.jobhub.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ControllerUnitTest {

    @Test
    void candidateUploadCvReturnsOk() {
        ApplicationService applicationService = mock(ApplicationService.class);
        ApplicationController controller = new ApplicationController(applicationService);

        UserPrincipal principal = samplePrincipal(1L, "candidate@gmail.com", "ROLE_CANDIDATE");
        MockMultipartFile cvFile = new MockMultipartFile("cvFile", "cv.pdf", "application/pdf", "demo-cv-content".getBytes());

        when(applicationService.uploadCandidateCv(eq(1L), any())).thenReturn("https://cloudinary.com/cv/123.pdf");

        var response = controller.uploadCv(cvFile, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("https://cloudinary.com/cv/123.pdf", response.getBody().getData());
    }

    @Test
    void candidateApplyJobReturnsCreated() {
        ApplicationService applicationService = mock(ApplicationService.class);
        ApplicationController controller = new ApplicationController(applicationService);

        UserPrincipal principal = samplePrincipal(1L, "candidate@gmail.com", "ROLE_CANDIDATE");
        ApplicationRequest request = new ApplicationRequest();
        request.setJobPostingId(10L);
        request.setCoverLetter("Thư xin việc mẫu");

        ApplicationResponse mockResponse = new ApplicationResponse();
        // Cập nhật match theo tham số mới: applyForJob(Long, String, Long)
        when(applicationService.applyForJob(eq(10L), anyString(), eq(1L))).thenReturn(mockResponse);

        var response = controller.applyJob(request, principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    void candidateGetHistoryReturnsPage() {
        ApplicationService applicationService = mock(ApplicationService.class);
        ApplicationController controller = new ApplicationController(applicationService);

        UserPrincipal principal = samplePrincipal(1L, "candidate@gmail.com", "ROLE_CANDIDATE");
        when(applicationService.getCandidateApplications(eq(1L), any())).thenReturn(new PageImpl<>(List.of(new ApplicationResponse())));

        var response = controller.getHistory(1, 5, "appliedAt", principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().getTotalElements());
    }

    @Test
    void authLoginReturnsTokens() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);

        LoginRequest request = new LoginRequest();
        AuthResponse mockResponse = AuthResponse.builder().accessToken("access-token").refreshToken("refresh-token").build();
        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        var response = controller.authenticateUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("access-token", response.getBody().getData().getAccessToken());
    }

    @Test
    void employerCreateJobReturnsPending() {
        JobPostingService jobPostingService = mock(JobPostingService.class);
        JobPostingController controller = new JobPostingController(jobPostingService);

        UserPrincipal principal = samplePrincipal(2L, "employer@gmail.com", "ROLE_EMPLOYER");
        JobPostingRequest request = new JobPostingRequest();
        JobPostingResponse mockResponse = new JobPostingResponse();
        when(jobPostingService.createJob(any(JobPostingRequest.class), eq(2L))).thenReturn(mockResponse);

        var response = controller.createJob(request, principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(jobPostingService).createJob(any(JobPostingRequest.class), eq(2L));
    }

    @Test
    void adminGetAllUsersWithKeywordReturnsPage() {
        UserService userService = mock(UserService.class);
        UserController controller = new UserController(userService);

        // Khớp với signature nhận (String keyword, Pageable pageable) trong service mới
        when(userService.getAllUsers(eq("gmail.com"), any())).thenReturn(new PageImpl<>(List.of(new UserResponse())));

        var response = controller.getAllUsers("gmail.com", 1, 5, "id");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getData());
        verify(userService).getAllUsers(eq("gmail.com"), any());
    }

    @Test
    void adminDeleteUserReturnsSuccess() {
        UserService userService = mock(UserService.class);
        UserController controller = new UserController(userService);

        doNothing().when(userService).deleteUser(eq(100L));

        var response = controller.deleteUser(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        verify(userService).deleteUser(eq(100L));
    }

    private UserPrincipal samplePrincipal(Long id, String email, String role) {
        return new UserPrincipal(
                id,
                email,
                "password",
                Collections.singletonList(new SimpleGrantedAuthority(role)),
                true
        );
    }
}