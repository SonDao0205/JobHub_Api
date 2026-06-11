package com.btvn.jobhub.dto.res;

import com.btvn.jobhub.entity.enumType.ApplicationStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationResponse {
    private Long id;
    private String coverLetter;
    private String cvUrl;
    private LocalDateTime appliedAt;
    private ApplicationStatusEnum status;
    private Long candidateId;
    private Long jobPostingId;
}