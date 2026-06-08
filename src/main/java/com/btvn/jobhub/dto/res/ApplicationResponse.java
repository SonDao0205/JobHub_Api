package com.btvn.jobhub.dto.res;

import com.btvn.jobhub.entity.enumType.ApplicationStatusEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApplicationResponse {
    private Long id;
    private Long jobId;
    private Long candidateId;
    private String coverLetter;
    private String cvUrl;
    private LocalDateTime appliedAt;
    private ApplicationStatusEnum status;
}
