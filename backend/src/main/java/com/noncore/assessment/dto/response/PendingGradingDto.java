package com.noncore.assessment.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 教师端：待批改作业列表项 DTO
 */
@Data
public class PendingGradingDto {
    /** 提交记录ID */
    private Long submissionId;

    /** 作业标题 */
    private String assignmentTitle;

    /** 学生姓名 */
    private String studentName;

    /** 课程标题 */
    private String courseTitle;

    /** 提交时间 */
    private LocalDateTime submittedAt;

    /** 截止时间 */
    private LocalDateTime dueDate;
}