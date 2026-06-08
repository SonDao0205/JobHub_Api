package com.btvn.jobhub.dto.req;

import lombok.Data;

@Data
public class JobPostingRequest {
    private String title;
    private String description;
    private String salaryRange;
}