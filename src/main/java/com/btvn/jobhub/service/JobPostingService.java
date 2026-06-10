package com.btvn.jobhub.service;

import com.btvn.jobhub.dto.req.JobPostingRequest;
import com.btvn.jobhub.dto.res.JobPostingResponse;
import com.btvn.jobhub.entity.enumType.JobStatusEnum;
import com.btvn.jobhub.security.principal.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JobPostingService {
    JobPostingResponse createJob(JobPostingRequest request, Long employerId);
    Page<JobPostingResponse> getAllJobs(JobStatusEnum status, Pageable pageable);
    JobPostingResponse submitJobForApproval(Long jobId, Long employerId);
    JobPostingResponse closeJob(Long jobId, Long employerId);
    JobPostingResponse approveJob(Long jobId);
    JobPostingResponse rejectJob(Long jobId);
}