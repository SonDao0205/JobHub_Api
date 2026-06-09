package com.btvn.jobhub.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationRequest {
    @NotNull(message = "ID công việc không được để trống")
    private Long jobPostingId;

    @NotBlank(message = "Thư giới thiệu (Cover Letter) không được để trống")
    private String coverLetter;
}