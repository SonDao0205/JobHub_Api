package com.btvn.jobhub.service;

import com.btvn.jobhub.dto.req.JobPostingRequest;
import com.btvn.jobhub.dto.res.JobPostingResponse;
import com.btvn.jobhub.entity.enumType.JobStatusEnum;

import java.util.List;

public interface JobPostingService {
    JobPostingResponse createJob(JobPostingRequest request, Long employerId); // [cite: 34]
    List<JobPostingResponse> getAllJobs(JobStatusEnum status); //
    JobPostingResponse updateJobStatus(Long jobId, JobStatusEnum status); // Phê duyệt tin [cite: 263]
}