package com.noncore.assessment.dto.response.admin;

import lombok.Data;

/**
 * 管理员学生详情中的最近学习事件。
 */
@Data
public class AdminStudentRecentEventResponse {
    private String eventType;
    private String title;
    private Long courseId;
    private String courseTitle;
    private String occurredAt;
    private Integer durationSeconds;
    private String link;
}
