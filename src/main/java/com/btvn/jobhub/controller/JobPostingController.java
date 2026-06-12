package com.btvn.jobhub.controller;

import com.btvn.jobhub.dto.req.JobPostingRequest;
import com.btvn.jobhub.dto.res.ApiResponse;
import com.btvn.jobhub.dto.res.JobPostingResponse;
import com.btvn.jobhub.entity.enumType.JobStatusEnum;
import com.btvn.jobhub.security.principal.UserPrincipal;
import com.btvn.jobhub.service.JobPostingService;
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

@RestController
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;

    @GetMapping("/api/v1/jobs/search")
    public ResponseEntity<ApiResponse<Page<JobPostingResponse>>> getJobsByStatus(
            @RequestParam(required = false) JobStatusEnum status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortBy).descending());
        Page<JobPostingResponse> jobPage = jobPostingService.getAllJobs(status, userPrincipal, pageable);

        return ResponseEntity.ok(
                ApiResponse.<Page<JobPostingResponse>>builder()
                        .success(true)
                        .message("Lấy danh sách tin tuyển dụng thành công.")
                        .data(jobPage)
                        .build()
        );
    }

    @PostMapping("/api/v1/employer/jobs")
    public ResponseEntity<ApiResponse<JobPostingResponse>> createJob(
            @Valid @RequestBody JobPostingRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        JobPostingResponse response = jobPostingService.createJob(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<JobPostingResponse>builder()
                        .success(true)
                        .message("Tạo tin tuyển dụng thành công ở trạng thái chờ duyệt.")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/api/v1/employer/jobs/{id}/submit-approval")
    public ResponseEntity<ApiResponse<JobPostingResponse>> submitJobForApproval(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal userPrincipal) {

        JobPostingResponse response = jobPostingService.submitJobForApproval(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.<JobPostingResponse>builder().success(true).message("Gửi yêu cầu duyệt tin tuyển dụng thành công.").data(response).build());
    }

    @PutMapping("/api/v1/employer/jobs/{id}/close")
    public ResponseEntity<ApiResponse<JobPostingResponse>> closeJob(
            @PathVariable Long id, @AuthenticationPrincipal UserPrincipal userPrincipal) {

        JobPostingResponse response = jobPostingService.closeJob(id, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.<JobPostingResponse>builder().success(true).message("Đóng tin tuyển dụng thành công.").data(response).build());
    }

    @PutMapping("/api/v1/admin/jobs/{id}/approve")
    public ResponseEntity<ApiResponse<JobPostingResponse>> approveJob(@PathVariable Long id) {
        JobPostingResponse response = jobPostingService.approveJob(id);
        return ResponseEntity.ok(ApiResponse.<JobPostingResponse>builder().success(true).message("Phê duyệt tin tuyển dụng thành công.").data(response).build());
    }

    @PutMapping("/api/v1/admin/jobs/{id}/reject")
    public ResponseEntity<ApiResponse<JobPostingResponse>> rejectJob(@PathVariable Long id) {
        JobPostingResponse response = jobPostingService.rejectJob(id);
        return ResponseEntity.ok(ApiResponse.<JobPostingResponse>builder().success(true).message("Đã từ chối phê duyệt tin tuyển dụng.").data(response).build());
    }
}