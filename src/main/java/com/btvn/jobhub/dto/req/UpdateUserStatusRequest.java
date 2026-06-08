package com.btvn.jobhub.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserStatusRequest {
    @NotNull(message = "Trạng thái isActive không được để trống")
    private Boolean isActive;
}