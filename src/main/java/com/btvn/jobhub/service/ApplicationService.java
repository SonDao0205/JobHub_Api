package com.btvn.jobhub.service;

import com.btvn.jobhub.dto.req.ApplicationRequest;
import com.btvn.jobhub.dto.res.ApplicationResponse;
import com.btvn.jobhub.entity.enumType.ApplicationStatusEnum;

public interface ApplicationService {
    // Bắt buộc gắn @AfterReturning AOP ở implementation của hàm này để ghi log
    ApplicationResponse applyForJob(ApplicationRequest request, Long candidateId); // [cite: 269]

    ApplicationResponse updateApplicationStatus(Long applicationId, ApplicationStatusEnum newStatus); // [cite: 214]
}