package com.btvn.jobhub.dto.req;

import com.btvn.jobhub.entity.enumType.JobStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateJobStatusRequest {
    @NotNull(message = "Trạng thái công việc không được để trống")
    private JobStatusEnum status;
}