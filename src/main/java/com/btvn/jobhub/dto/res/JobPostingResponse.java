package com.btvn.jobhub.dto.res;

import com.btvn.jobhub.entity.enumType.JobStatusEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobPostingResponse {
    private Long id;
    private String title;
    private String description;
    private String salaryRange;
    private JobStatusEnum status;
    private Long employerId;
}