package com.btvn.jobhub.repository;

import com.btvn.jobhub.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    // Phục vụ Spring Security Filter kiểm tra Token đã bị thu hồi chưa
    boolean existsByTokenString(String tokenString); // [cite: 251, 266]
}