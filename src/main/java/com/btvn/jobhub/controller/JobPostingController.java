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

    // FR-06 / UC-06: Nhà tuyển dụng tạo tin tuyển dụng mới
    @PostMapping("/createJob")
    public ResponseEntity<ApiResponse<JobPostingResponse>> createJob(
            @Valid @RequestBody JobPostingRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) { // 💡 TỰ ĐỘNG LẤY USER ĐANG ĐĂNG NHẬP

        // Trích xuất ID của Employer từ phiên Stateless Security đang hoạt động
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
            @RequestParam JobStatusEnum status, // Nhận bất kỳ giá trị nào trong 5 status: DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, CLOSED
            @RequestParam(defaultValue = "1") int page, // Page index bắt đầu từ 0 theo chuẩn Spring Data
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        // 1. Khởi tạo cấu hình phân trang và sắp xếp giảm dần theo trường chỉ định
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortBy).descending());

        // 2. Gọi sang Service xử lý logic luồng dòng chảy dữ liệu
        Page<JobPostingResponse> jobPage = jobPostingService.getAllJobs(status, pageable);

        // 3. Trả về Response chung chuẩn cấu trúc JSON
        return ResponseEntity.ok(
                ApiResponse.<Page<JobPostingResponse>>builder()
                        .success(true)
                        .message("Lấy danh sách tin tuyển dụng theo trạng thái " + status + " thành công.")
                        .data(jobPage)
                        .build()
        );
    }

    // FR-07 / UC-07: Ứng viên (Candidate) xem danh sách việc làm đã được phê duyệt công khai
    @GetMapping("/jobApproved")
    public ResponseEntity<ApiResponse<Page<JobPostingResponse>>> getApprovedJobs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy) {

        // 1. Khởi tạo cấu hình phân trang (Spring Data Page tính từ 0 nên lấy page - 1)
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortBy).descending());

        // 2. Tái sử dụng hàm getAllJobs từ Service, truyền cứng trạng thái JobStatusEnum.APPROVED
        Page<JobPostingResponse> jobPage = jobPostingService.getAllJobs(JobStatusEnum.APPROVED, pageable);

        // 3. Trả về cấu trúc JSON chuẩn hệ thống
        return ResponseEntity.ok(
                ApiResponse.<Page<JobPostingResponse>>builder()
                        .success(true)
                        .message("Lấy danh sách việc làm đang tuyển dụng thành công.")
                        .data(jobPage)
                        .build()
        );
    }

    // Nhà tuyển dụng gửi yêu cầu duyệt tin (Chuyển trạng thái từ DRAFT sang PENDING_APPROVAL)
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

    // Nhà tuyển dụng đóng tin tuyển dụng (Chuyển trạng thái sang CLOSED)
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

    // Admin phê duyệt tin tuyển dụng (Chuyển trạng thái sang APPROVED)
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

    // Admin từ chối tin tuyển dụng (Chuyển trạng thái sang REJECTED)
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