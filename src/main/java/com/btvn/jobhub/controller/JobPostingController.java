package com.btvn.jobhub.controller;

import com.btvn.jobhub.dto.req.JobPostingRequest;
import com.btvn.jobhub.dto.req.UpdateJobStatusRequest;
import com.btvn.jobhub.dto.res.ApiResponse;
import com.btvn.jobhub.dto.res.JobPostingResponse;
import com.btvn.jobhub.entity.User;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;

    @PostMapping("/createJob")
    public ResponseEntity<ApiResponse<JobPostingResponse>> createJob(
            @Valid @RequestBody JobPostingRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long employerId = userPrincipal.getId();

        JobPostingResponse response = jobPostingService.createJob(request, employerId);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<JobPostingResponse>builder()
                        .success(true)
                        .message("Tạo tin tuyển dụng thành công ở trạng thái chờ duyệt.")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<JobPostingResponse>>> getJobsByStatus(
            @RequestParam JobStatusEnum status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortBy).descending());

        Page<JobPostingResponse> jobPage = jobPostingService.getAllJobs(status, pageable);

        return ResponseEntity.ok(
                ApiResponse.<Page<JobPostingResponse>>builder()
                        .success(true)
                        .message("Lấy danh sách tin tuyển dụng theo trạng thái " + status + " thành công.")
                        .data(jobPage)
                        .build()
        );
    }

    @GetMapping("/jobApproved")
    public ResponseEntity<ApiResponse<Page<JobPostingResponse>>> getApprovedJobs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortBy).descending());

        Page<JobPostingResponse> jobPage = jobPostingService.getAllJobs(JobStatusEnum.APPROVED, pageable);

        return ResponseEntity.ok(
                ApiResponse.<Page<JobPostingResponse>>builder()
                        .success(true)
                        .message("Lấy danh sách việc làm đang tuyển dụng thành công.")
                        .data(jobPage)
                        .build()
        );
    }

    @PutMapping("/{id}/submit-approval")
    public ResponseEntity<ApiResponse<JobPostingResponse>> submitJobForApproval(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long employerId = userPrincipal.getId();
        JobPostingResponse response = jobPostingService.submitJobForApproval(id, employerId);

        return ResponseEntity.ok(
                ApiResponse.<JobPostingResponse>builder()
                        .success(true)
                        .message("Gửi yêu cầu duyệt tin tuyển dụng thành công.")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<ApiResponse<JobPostingResponse>> closeJob(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Long employerId = userPrincipal.getId();
        JobPostingResponse response = jobPostingService.closeJob(id, employerId);

        return ResponseEntity.ok(
                ApiResponse.<JobPostingResponse>builder()
                        .success(true)
                        .message("Đóng tin tuyển dụng thành công.")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<JobPostingResponse>> approveJob(@PathVariable Long id) {

        JobPostingResponse response = jobPostingService.approveJob(id);

        return ResponseEntity.ok(
                ApiResponse.<JobPostingResponse>builder()
                        .success(true)
                        .message("Phê duyệt tin tuyển dụng thành công. Tin hiện đã được hiển thị công khai.")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<JobPostingResponse>> rejectJob(@PathVariable Long id) {

        JobPostingResponse response = jobPostingService.rejectJob(id);

        return ResponseEntity.ok(
                ApiResponse.<JobPostingResponse>builder()
                        .success(true)
                        .message("Đã từ chối phê duyệt tin tuyển dụng.")
                        .data(response)
                        .build()
        );
    }
}