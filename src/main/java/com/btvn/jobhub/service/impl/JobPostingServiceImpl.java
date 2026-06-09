package com.btvn.jobhub.service.impl;

import com.btvn.jobhub.dto.req.JobPostingRequest;
import com.btvn.jobhub.dto.res.JobPostingResponse;
import com.btvn.jobhub.dto.res.UserResponse;
import com.btvn.jobhub.entity.JobPosting;
import com.btvn.jobhub.entity.User;
import com.btvn.jobhub.entity.enumType.JobStatusEnum;
import com.btvn.jobhub.repository.JobPostingRepository;
import com.btvn.jobhub.repository.UserRepository;
import com.btvn.jobhub.service.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobPostingServiceImpl implements JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public JobPostingResponse createJob(JobPostingRequest request, Long employerId) {
        User employer = userRepository.findById(employerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhà tuyển dụng có ID: " + employerId));

        JobPosting jobPosting = JobPosting.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .salaryRange(request.getSalaryRange())
                .status(JobStatusEnum.DRAFT)
                .employer(employer)
                .applications(new ArrayList<>())
                .build();

        JobPosting savedJob = jobPostingRepository.save(jobPosting);
        return convertToJobPostingResponse(savedJob);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobPostingResponse> getAllJobs(JobStatusEnum status, Pageable pageable) {
        Page<JobPosting> jobPage = jobPostingRepository.findByStatus(status, pageable);

        List<JobPostingResponse> dtoList = jobPage.getContent().stream()
                .map(this::convertToJobPostingResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, jobPage.getTotalElements());
    }

    // =========================================================================
    // KHU VỰC LOGIC CỦA NHÀ TUYỂN DỤNG (EMPLOYER)
    // =========================================================================

    @Override
    @Transactional
    public JobPostingResponse submitJobForApproval(Long jobId, Long employerId) {
        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tuyển dụng có ID: " + jobId));

        // Kiểm tra quyền sở hữu bài đăng
        if (!jobPosting.getEmployer().getId().equals(employerId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa tin tuyển dụng của công ty khác.");
        }

        // Kiểm tra logic vòng đời: Chỉ cho phép gửi duyệt khi đang là tin nháp (DRAFT)
        if (jobPosting.getStatus() != JobStatusEnum.DRAFT) {
            throw new RuntimeException("Nhà tuyển dụng chỉ được gửi duyệt khi tin ở trạng thái DRAFT. Trạng thái hiện tại: " + jobPosting.getStatus());
        }

        jobPosting.setStatus(JobStatusEnum.PENDING_APPROVAL);
        return convertToJobPostingResponse(jobPostingRepository.save(jobPosting));
    }

    @Override
    @Transactional
    public JobPostingResponse closeJob(Long jobId, Long employerId) {
        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tuyển dụng có ID: " + jobId));

        // Kiểm tra quyền sở hữu bài đăng
        if (!jobPosting.getEmployer().getId().equals(employerId)) {
            throw new RuntimeException("Bạn không có quyền đóng tin tuyển dụng của công ty khác.");
        }

        // Kiểm tra logic vòng đời: Không thể đóng một tin chưa từng được gửi duyệt (vẫn là nháp)
        if (jobPosting.getStatus() == JobStatusEnum.DRAFT) {
            throw new RuntimeException("Không thể đóng một tin tuyển dụng đang nằm ở trạng thái nháp (DRAFT).");
        }

        jobPosting.setStatus(JobStatusEnum.CLOSED);
        return convertToJobPostingResponse(jobPostingRepository.save(jobPosting));
    }

    // =========================================================================
    // KHU VỰC LOGIC CỦA QUẢN TRỊ VIÊN (ADMIN)
    // =========================================================================

    @Override
    @Transactional
    public JobPostingResponse approveJob(Long jobId) {
        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tuyển dụng có ID: " + jobId));

        // Tùy chọn kiểm tra (nếu muốn chặt chẽ): Chỉ duyệt tin đang chờ duyệt
        if (jobPosting.getStatus() != JobStatusEnum.PENDING_APPROVAL) {
            throw new RuntimeException("Chỉ có thể phê duyệt các tin tuyển dụng đang chờ duyệt (PENDING_APPROVAL).");
        }

        jobPosting.setStatus(JobStatusEnum.APPROVED);
        return convertToJobPostingResponse(jobPostingRepository.save(jobPosting));
    }

    @Override
    @Transactional
    public JobPostingResponse rejectJob(Long jobId) {
        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tuyển dụng có ID: " + jobId));

        // Tùy chọn kiểm tra (nếu muốn chặt chẽ): Chỉ từ chối tin đang chờ duyệt
        if (jobPosting.getStatus() != JobStatusEnum.PENDING_APPROVAL) {
            throw new RuntimeException("Chỉ có thể từ chối các tin tuyển dụng đang chờ duyệt (PENDING_APPROVAL).");
        }

        jobPosting.setStatus(JobStatusEnum.REJECTED);
        return convertToJobPostingResponse(jobPostingRepository.save(jobPosting));
    }

    // Helper: Chuyển đổi dữ liệu từ Entity sang DTO phản hồi
    private JobPostingResponse convertToJobPostingResponse(JobPosting job) {
        User employerEntity = job.getEmployer();

        UserResponse employerResponse = UserResponse.builder()
                .id(employerEntity.getId())
                .email(employerEntity.getEmail())
                .role(employerEntity.getRole())
                .isActive(employerEntity.getIsActive())
                .build();

        return JobPostingResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .salaryRange(job.getSalaryRange())
                .status(job.getStatus())
                .employer(employerResponse)
                .build();
    }
}