package com.btvn.jobhub.service.impl;

import com.btvn.jobhub.dto.req.ApplicationRequest;
import com.btvn.jobhub.dto.res.ApplicationResponse;
import com.btvn.jobhub.entity.Application;
import com.btvn.jobhub.entity.JobPosting;
import com.btvn.jobhub.entity.User;
import com.btvn.jobhub.entity.enumType.ApplicationStatusEnum;
import com.btvn.jobhub.entity.enumType.JobStatusEnum;
import com.btvn.jobhub.exception.BadRequestException;
import com.btvn.jobhub.exception.ForbiddenException;
import com.btvn.jobhub.exception.ResourceConflictException;
import com.btvn.jobhub.repository.ApplicationRepository;
import com.btvn.jobhub.repository.JobPostingRepository;
import com.btvn.jobhub.repository.UserRepository;
import com.btvn.jobhub.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
        if (cvFile == null || cvFile.isEmpty()) {
            throw new BadRequestException("Vui lòng tải lên file hồ sơ CV của bạn.");
        }

        if (applicationRepository.existsByCandidateIdAndJobPostingId(candidateId, jobPostingId)) {
            throw new ResourceConflictException("Bạn đã nộp hồ sơ ứng tuyển cho công việc này rồi.");
        }

        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy tin tuyển dụng có ID: " + jobPostingId));

        if (jobPosting.getStatus() != JobStatusEnum.APPROVED) {
            throw new BadRequestException("Tin tuyển dụng này hiện không mở nhận hồ sơ.");
        }

        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy thông tin ứng viên."));

        String generatedCvUrl = cloudinaryService.uploadFile(cvFile);

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

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getCandidateApplications(Long candidateId, Pageable pageable) {
        Page<Application> appPage = applicationRepository.findByCandidateId(candidateId, pageable);

        List<ApplicationResponse> dtoList = appPage.getContent().stream()
                .map(this::convertToApplicationResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, appPage.getTotalElements());
    }

    private Application getValidatedApplication(Long applicationId, Long employerId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy hồ sơ ứng tuyển có ID: " + applicationId));

        Long actualEmployerId = app.getJobPosting().getEmployer().getId();
        if (!actualEmployerId.equals(employerId)) {
            throw new ForbiddenException("Bạn không có quyền xử lý hồ sơ ứng tuyển thuộc tin tuyển dụng của công ty khác.");
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