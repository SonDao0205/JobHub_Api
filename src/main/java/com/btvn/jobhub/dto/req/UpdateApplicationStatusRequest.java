package com.btvn.jobhub.dto.req;

import com.btvn.jobhub.entity.enumType.ApplicationStatusEnum;
import lombok.Data;

@Data
public class UpdateApplicationStatusRequest {
    private ApplicationStatusEnum newStatus;
}
