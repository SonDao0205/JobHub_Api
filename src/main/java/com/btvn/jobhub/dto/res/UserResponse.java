package com.btvn.jobhub.dto.res;

import com.btvn.jobhub.entity.enumType.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private RoleEnum role;
    private Boolean isActive;
}