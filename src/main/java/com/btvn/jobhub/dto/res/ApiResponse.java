package com.btvn.jobhub.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success; // [cite: 294]
    private String message; // [cite: 295]
    private T data; // [cite: 296]
}
