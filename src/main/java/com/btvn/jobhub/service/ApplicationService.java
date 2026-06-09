package com.btvn.jobhub.service;

import com.btvn.jobhub.dto.req.ApplicationRequest;
import com.btvn.jobhub.dto.res.ApplicationResponse;
import com.btvn.jobhub.entity.enumType.ApplicationStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ApplicationService {
    ApplicationResponse applyForJob(Long jobPostingId, String coverLetter, MultipartFile cvFile, Long candidateId);

    // 💡 THÊM CÁC HÀM XỬ LÝ TRẠNG THÁI CHO EMPLOYER (Có kèm ID của người dùng đăng nhập để kiểm tra bảo mật chéo)
    ApplicationResponse reviewApplication(Long applicationId, Long employerId);
    ApplicationResponse inviteInterview(Long applicationId, Long employerId);
    ApplicationResponse acceptApplication(Long applicationId, Long employerId);
    ApplicationResponse rejectApplication(Long applicationId, Long employerId);

    // 💡 THÊM HÀM LẤY LỊCH SỬ CHO CANDIDATE
    Page<ApplicationResponse> getCandidateApplications(Long candidateId, Pageable pageable);
}