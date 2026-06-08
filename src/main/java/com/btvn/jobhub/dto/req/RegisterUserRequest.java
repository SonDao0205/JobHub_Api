package com.btvn.jobhub.dto.req;

import com.btvn.jobhub.entity.enumType.RoleEnum;
import lombok.Data;

@Data
public class RegisterUserRequest {
    private String email;
    private String password;
    private RoleEnum role;
}