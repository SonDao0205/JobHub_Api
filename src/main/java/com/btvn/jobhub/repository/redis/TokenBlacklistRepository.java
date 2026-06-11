package com.btvn.jobhub.repository.redis;

import com.btvn.jobhub.entity.TokenBlacklist;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenBlacklistRepository extends CrudRepository<TokenBlacklist, String> {
    boolean existsByTokenString(String tokenString);
}