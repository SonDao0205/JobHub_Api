package com.btvn.jobhub.repository;

import com.btvn.jobhub.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    boolean existsByCandidateIdAndJobPostingId(Long candidateId, Long jobId);
    List<Application> findByJobPostingId(Long jobId);

    // 💡 THÊM HÀM NÀY: Tìm danh sách đơn ứng tuyển của một Ứng viên (Có phân trang)
    Page<Application> findByCandidateId(Long candidateId, Pageable pageable);
}