//package com.btvn.jobhub.controller;
//
//import com.btvn.jobhub.dto.req.*;
//import com.btvn.jobhub.dto.res.*;
//import com.btvn.jobhub.entity.enumType.JobStatusEnum;
//import com.btvn.jobhub.security.principal.UserPrincipal;
//import com.btvn.jobhub.service.*;
//import org.junit.jupiter.api.Test;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.http.HttpStatus;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//
//import java.util.Collections;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//class ControllerUnitTest {
//
//    @Test
//    void candidateApplyJobReturnsCreated() {
//        ApplicationService applicationService = mock(ApplicationService.class);
//        ApplicationController controller = new ApplicationController(applicationService);
//
//        UserPrincipal principal = samplePrincipal(1L, "candidate@gmail.com", "ROLE_CANDIDATE");
//        ApplicationRequest request = new ApplicationRequest();
//        MockMultipartFile cvFile = new MockMultipartFile("cvFile", "cv.pdf", "application/pdf", "demo".getBytes());
//
//        ApplicationResponse mockResponse = new ApplicationResponse();
//        when(applicationService.applyForJob(any(), any(), any(), eq(1L))).thenReturn(mockResponse);
//
//        var response = controller.applyJob(request, cvFile, principal);
//
//        assertEquals(HttpStatus.CREATED, response.getStatusCode());
//        assertTrue(response.getBody().isSuccess());
//    }
//
//    @Test
//    void candidateGetHistoryReturnsPage() {
//        ApplicationService applicationService = mock(ApplicationService.class);
//        ApplicationController controller = new ApplicationController(applicationService);
//
//        UserPrincipal principal = samplePrincipal(1L, "candidate@gmail.com", "ROLE_CANDIDATE");
//        when(applicationService.getCandidateApplications(eq(1L), any())).thenReturn(new PageImpl<>(List.of(new ApplicationResponse())));
//
//        var response = controller.getHistory(1, 5, "appliedAt", principal);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(1, response.getBody().getData().getTotalElements());
//    }
//
//    @Test
//    void authLoginReturnsTokens() {
//        AuthService authService = mock(AuthService.class);
//        AuthController controller = new AuthController(authService);
//
//        LoginRequest request = new LoginRequest();
//        AuthResponse mockResponse = AuthResponse.builder().accessToken("access-token").refreshToken("refresh-token").build();
//        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);
//
//        var response = controller.authenticateUser(request);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals("access-token", response.getBody().getData().getAccessToken());
//    }
//
//    @Test
//    void employerCreateJobReturnsPending() {
//        JobPostingService jobPostingService = mock(JobPostingService.class);
//        JobPostingController controller = new JobPostingController(jobPostingService);
//
//        UserPrincipal principal = samplePrincipal(2L, "employer@gmail.com", "ROLE_EMPLOYER");
//        JobPostingRequest request = new JobPostingRequest();
//        JobPostingResponse mockResponse = new JobPostingResponse();
//        when(jobPostingService.createJob(any(JobPostingRequest.class), eq(2L))).thenReturn(mockResponse);
//
//        var response = controller.createJob(request, principal);
//
//        assertEquals(HttpStatus.CREATED, response.getStatusCode());
//        verify(jobPostingService).createJob(any(JobPostingRequest.class), eq(2L));
//    }
//
//    @Test
//    void adminGetAllUsersReturnsPage() {
//        UserService userService = mock(UserService.class);
//        UserController controller = new UserController(userService);
//
//        when(userService.getAllUsers(any())).thenReturn(new PageImpl<>(List.of(new UserResponse())));
//
//        var response = controller.getAllUsers(1, 5, "id");
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody().getData());
//    }
//
//    private UserPrincipal samplePrincipal(Long id, String email, String role) {
//        return new UserPrincipal(
//                id,
//                email,
//                "password",
//                Collections.singletonList(new SimpleGrantedAuthority(role)),
//                true
//        );
//    }
//}