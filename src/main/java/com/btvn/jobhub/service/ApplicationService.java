package com.btvn.jobhub.service;

import com.btvn.jobhub.dto.req.ApplicationRequest;
import com.btvn.jobhub.dto.res.ApplicationResponse;
import com.btvn.jobhub.entity.enumType.ApplicationStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ApplicationService {
    ApplicationResponse applyForJob(Long jobPostingId, String coverLetter, MultipartFile cvFile, Long candidateId);
    ApplicationResponse reviewApplication(Long applicationId, Long employerId);
    ApplicationResponse inviteInterview(Long applicationId, Long employerId);
    ApplicationResponse acceptApplication(Long applicationId, Long employerId);
    ApplicationResponse rejectApplication(Long applicationId, Long employerId);
    Page<ApplicationResponse> getCandidateApplications(Long candidateId, Pageable pageable);
}