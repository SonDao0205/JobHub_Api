package com.btvn.jobhub.repository;
import com.btvn.jobhub.entity.JobPosting;
import com.btvn.jobhub.entity.enumType.JobStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    // Tìm việc làm theo trạng thái
    List<JobPosting> findByStatus(JobStatusEnum status);
}
