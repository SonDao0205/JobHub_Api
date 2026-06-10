package com.btvn.jobhub.dto.req;

import com.btvn.jobhub.entity.enumType.RoleEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotNull(message = "Quyền (Role) không được để trống")
    private RoleEnum role;


    @JsonIgnore
    @AssertTrue(message = "Chỉ được phép đăng ký với quyền EMPLOYER hoặc CANDIDATE")
    public boolean isValidRole() {
        return role != null && (role == RoleEnum.EMPLOYER || role == RoleEnum.CANDIDATE);
    }
}