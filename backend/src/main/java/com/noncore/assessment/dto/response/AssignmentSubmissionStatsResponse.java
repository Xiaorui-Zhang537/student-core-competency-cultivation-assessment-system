package com.noncore.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 作业提交统计响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSubmissionStatsResponse {
    private Long assignmentId;
    private Long courseId;
    private Integer totalEnrolled;
    private Integer submittedCount;
    private Integer unsubmittedCount;
}


