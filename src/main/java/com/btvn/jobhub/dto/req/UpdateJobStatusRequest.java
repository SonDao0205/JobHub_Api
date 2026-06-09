package com.btvn.jobhub.dto.req;

import com.btvn.jobhub.entity.enumType.JobStatusEnum;
import com.btvn.jobhub.entity.enumType.RoleEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateJobStatusRequest {
    @NotNull(message = "Trạng thái công việc không được để trống")
    private JobStatusEnum status;

    @JsonIgnore
    @AssertTrue(message = "Chỉ được phép cậpp nhật với các status : DRAFT, PENDING_APPROVAL, APPROVED, REJECTED,CLOSED")
    public boolean isValidStatus() {
        return status != null && (status == JobStatusEnum.DRAFT || status == JobStatusEnum.PENDING_APPROVAL || status == JobStatusEnum.APPROVED || status == JobStatusEnum.REJECTED || status == JobStatusEnum.CLOSED);
    }
}