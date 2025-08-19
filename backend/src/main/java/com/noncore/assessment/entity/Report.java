package com.noncore.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 举报实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    private Long id;
    private Long reporterId;
    private Long reportedStudentId;
    private Long courseId;
    private Long assignmentId;
    private Long submissionId;
    private String reason;
    private String details;
    private Long evidenceFileId;
    /** 状态: pending | in_review | resolved | rejected */
    @Builder.Default
    private String status = "pending";
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


