package com.btvn.jobhub.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.concurrent.TimeUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("TokenBlacklist")
public class TokenBlacklist {

    @Id
    private String id;

    @Indexed
    private String tokenString;

    private String email;

    @TimeToLive(unit = TimeUnit.MILLISECONDS)
    private Long ttl;
}