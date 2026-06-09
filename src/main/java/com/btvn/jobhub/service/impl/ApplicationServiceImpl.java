package com.btvn.jobhub.service.impl;

import com.btvn.jobhub.dto.req.ApplicationRequest;
import com.btvn.jobhub.dto.res.ApplicationResponse;
import com.btvn.jobhub.entity.Application;
import com.btvn.jobhub.entity.JobPosting;
import com.btvn.jobhub.entity.User;
import com.btvn.jobhub.entity.enumType.ApplicationStatusEnum;
import com.btvn.jobhub.entity.enumType.JobStatusEnum;
import com.btvn.jobhub.repository.ApplicationRepository;
import com.btvn.jobhub.repository.JobPostingRepository;
import com.btvn.jobhub.repository.UserRepository;
import com.btvn.jobhub.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; // Đã import chuẩn
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable; // 💡 THÊM IMPORT NÀY ĐỂ SỬA LỖI ĐỎ COMPILE
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobPostingRepository jobPostingRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public ApplicationResponse applyForJob(Long jobPostingId, String coverLetter, MultipartFile cvFile, Long candidateId) {
        // 1. Fail-Fast: Chặn ngay nếu file rỗng
        if (cvFile == null || cvFile.isEmpty()) {
            throw new RuntimeException("Vui lòng tải lên file hồ sơ CV của bạn.");
        }

        // 2. Chống spam CV
        if (applicationRepository.existsByCandidateIdAndJobPostingId(candidateId, jobPostingId)) {
            throw new RuntimeException("Bạn đã nộp hồ sơ ứng tuyển cho công việc này rồi.");
        }

        // 3. Kiểm tra bài đăng có tồn tại và đã duyệt không
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tuyển dụng có ID: " + jobPostingId));

        if (jobPosting.getStatus() != JobStatusEnum.APPROVED) {
            throw new RuntimeException("Tin tuyển dụng này hiện không mở nhận hồ sơ.");
        }

        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin ứng viên."));

        // 4. ĐẨY FILE LÊN CLOUDINARY VÀ LẤY URL TỰ ĐỘNG
        String generatedCvUrl = cloudinaryService.uploadFile(cvFile);

        // 5. Khởi tạo Application lưu trữ xuống MySQL
        Application application = Application.builder()
                .coverLetter(coverLetter)
                .cvUrl(generatedCvUrl)
                .appliedAt(LocalDateTime.now())
                .status(ApplicationStatusEnum.PENDING)
                .candidate(candidate)
                .jobPosting(jobPosting)
                .build();

        Application savedApplication = applicationRepository.save(application);
        return convertToApplicationResponse(savedApplication);
    }

    // =========================================================================
    // KHU VỰC DUYỆT ĐƠN CỦA NHÀ TUYỂN DỤNG (EMPLOYER)
    // =========================================================================

    @Override
    @Transactional
    public ApplicationResponse reviewApplication(Long applicationId, Long employerId) {
        Application app = getValidatedApplication(applicationId, employerId);
        app.setStatus(ApplicationStatusEnum.REVIEWING);
        return convertToApplicationResponse(applicationRepository.save(app));
    }

    @Override
    @Transactional
    public ApplicationResponse inviteInterview(Long applicationId, Long employerId) {
        Application app = getValidatedApplication(applicationId, employerId);
        app.setStatus(ApplicationStatusEnum.INTERVIEWING);
        return convertToApplicationResponse(applicationRepository.save(app));
    }

    @Override
    @Transactional
    public ApplicationResponse acceptApplication(Long applicationId, Long employerId) {
        Application app = getValidatedApplication(applicationId, employerId);
        app.setStatus(ApplicationStatusEnum.ACCEPTED);
        return convertToApplicationResponse(applicationRepository.save(app));
    }

    @Override
    @Transactional
    public ApplicationResponse rejectApplication(Long applicationId, Long employerId) {
        Application app = getValidatedApplication(applicationId, employerId);
        app.setStatus(ApplicationStatusEnum.REJECTED);
        return convertToApplicationResponse(applicationRepository.save(app));
    }

    // =========================================================================
    // KHU VỰC LẤY LỊCH SỬ ỨNG TUYỂN CỦA ỨNG VIÊN (CANDIDATE)
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getCandidateApplications(Long candidateId, Pageable pageable) {
        Page<Application> appPage = applicationRepository.findByCandidateId(candidateId, pageable);

        // Biến đổi tập hợp Entity sang DTO bằng Java Stream API theo đặc tả quy chuẩn SRS
        List<ApplicationResponse> dtoList = appPage.getContent().stream()
                .map(this::convertToApplicationResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, appPage.getTotalElements());
    }

    /**
     * Helper: Tìm thực thể Đơn ứng tuyển và kiểm tra bảo mật chéo xem tin tuyển dụng này có thuộc về chính Employer không
     */
    private Application getValidatedApplication(Long applicationId, Long employerId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ ứng tuyển có ID: " + applicationId));

        // Kiểm tra bảo mật chéo: JobPosting của đơn này phải do chính Employer này đăng tuyển
        Long actualEmployerId = app.getJobPosting().getEmployer().getId();
        if (!actualEmployerId.equals(employerId)) {
            throw new RuntimeException("Bạn không có quyền xử lý hồ sơ ứng tuyển thuộc tin tuyển dụng của công ty khác.");
        }

        return app;
    }

    private ApplicationResponse convertToApplicationResponse(Application app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .coverLetter(app.getCoverLetter())
                .cvUrl(app.getCvUrl())
                .appliedAt(app.getAppliedAt())
                .status(app.getStatus())
                .candidateId(app.getCandidate().getId())
                .jobPostingId(app.getJobPosting().getId())
                .build();
    }
}