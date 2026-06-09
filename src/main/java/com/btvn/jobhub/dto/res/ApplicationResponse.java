package com.btvn.jobhub.dto.res;

import com.btvn.jobhub.entity.enumType.ApplicationStatusEnum;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ApplicationResponse {
    private Long id;
    private String coverLetter;
    private String cvUrl;
    private LocalDateTime appliedAt;
    private ApplicationStatusEnum status;
    private Long candidateId;
    private Long jobPostingId;
}