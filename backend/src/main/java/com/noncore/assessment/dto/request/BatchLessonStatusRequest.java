package com.noncore.assessment.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class BatchLessonStatusRequest {
    private List<Long> lessonIds;
    private String status;
} 