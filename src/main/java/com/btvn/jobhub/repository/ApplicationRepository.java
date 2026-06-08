package com.btvn.jobhub.repository;
import com.btvn.jobhub.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    // Kiểm tra xem ứng viên đã nộp tin tuyển dụng này chưa (Logic chống spam CV)
    boolean existsByCandidateIdAndJobPostingId(Long candidateId, Long jobId); // [cite: 202, 270]

    // Lấy danh sách CV nộp vào một job cụ thể cho Employer
    List<Application> findByJobPostingId(Long jobId);
}
