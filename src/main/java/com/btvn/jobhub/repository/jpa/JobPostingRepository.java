package com.btvn.jobhub.repository.jpa;

import com.btvn.jobhub.entity.JobPosting;
import com.btvn.jobhub.entity.enumType.JobStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    Page<JobPosting> findByStatus(JobStatusEnum status, Pageable pageable);
    Page<JobPosting> findByEmployerId(Long employerId, Pageable pageable);
    Page<JobPosting> findByStatusAndEmployerId(JobStatusEnum status, Long employerId, Pageable pageable);
}