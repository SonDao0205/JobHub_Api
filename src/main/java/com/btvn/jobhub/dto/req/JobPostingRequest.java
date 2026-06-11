package com.btvn.jobhub.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobPostingRequest {

    @NotBlank(message = "Tiêu đề tin tuyển dụng không được để trống.")
    @Size(min = 5, max = 150, message = "Tiêu đề phải từ 5 đến 150 ký tự.")
    private String title;

    @NotBlank(message = "Mô tả công việc không được để trống.")
    @Size(min = 10, message = "Mô tả công việc phải có ít nhất 10 ký tự.")
    private String description;

    @NotBlank(message = "Mức lương không được để trống.")
    private String salaryRange;
}