package com.btvn.jobhub.controller;

import com.btvn.jobhub.dto.req.ApplicationRequest;
import com.btvn.jobhub.dto.res.ApiResponse;
import com.btvn.jobhub.dto.res.ApplicationResponse;
import com.btvn.jobhub.security.principal.UserPrincipal;
import com.btvn.jobhub.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;


    @PostMapping(value = "/apply", consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyJob(
            @Valid @RequestPart("request") ApplicationRequest request,
            @RequestPart("cvFile") MultipartFile cvFile,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long candidateId = userPrincipal.getId();
        ApplicationResponse response = applicationService.applyForJob(request.getJobPostingId(), request.getCoverLetter(), cvFile, candidateId);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ApplicationResponse>builder().success(true).message("Nộp hồ sơ ứng tuyển thành công!").data(response).build()
        );
    }


    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "appliedAt") String sortBy,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortBy).descending());
        Page<ApplicationResponse> responsePage = applicationService.getCandidateApplications(userPrincipal.getId(), pageable);

        return ResponseEntity.ok(
                ApiResponse.<Page<ApplicationResponse>>builder()
                        .success(true)
                        .message("Lấy lịch sử ứng tuyển thành công.")
                        .data(responsePage)
                        .build()
        );
    }





    @PutMapping("/{id}/review")
    public ResponseEntity<ApiResponse<ApplicationResponse>> reviewApp(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ApplicationResponse response = applicationService.reviewApplication(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.<ApplicationResponse>builder().success(true).message("Đã chuyển trạng thái hồ sơ sang: ĐANG XEM.").data(response).build());
    }

    @PutMapping("/{id}/interview")
    public ResponseEntity<ApiResponse<ApplicationResponse>> interviewApp(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ApplicationResponse response = applicationService.inviteInterview(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.<ApplicationResponse>builder().success(true).message("Đã chuyển trạng thái hồ sơ sang: MỜI PHỎNG VẤN.").data(response).build());
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<ApplicationResponse>> acceptApp(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ApplicationResponse response = applicationService.acceptApplication(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.<ApplicationResponse>builder().success(true).message("Đã CHẤP THUẬN hồ sơ ứng viên thành công.").data(response).build());
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ApplicationResponse>> rejectApp(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ApplicationResponse response = applicationService.rejectApplication(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.<ApplicationResponse>builder().success(true).message("Đã TỪ CHỐI hồ sơ ứng tuyển này.").data(response).build());
    }
}