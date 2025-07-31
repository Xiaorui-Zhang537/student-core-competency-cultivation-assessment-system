package com.noncore.assessment.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class BatchAssignmentStatusRequest {
    private List<Long> assignmentIds;
    private String status;
} 