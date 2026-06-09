package com.btvn.jobhub.dto.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String accessToken; // [cite: 261]
    private String refreshToken; // [cite: 261]
    private String role;
}