package com.noncore.assessment.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class BatchCourseStatusRequest {
    private List<Long> courseIds;
    private String status;
} 