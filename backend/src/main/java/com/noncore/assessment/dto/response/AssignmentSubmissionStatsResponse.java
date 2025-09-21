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
    /** 已评分提交数 */
    private Integer gradedCount;
    /** 未评分提交数（= 已提交 - 已评分） */
    private Integer ungradedCount;
}


