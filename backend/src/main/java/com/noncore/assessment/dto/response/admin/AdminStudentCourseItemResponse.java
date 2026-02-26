package com.noncore.assessment.dto.response.admin;

import lombok.Data;

/**
 * 管理员学生详情中的课程选项（用于课程筛选）。
 */
@Data
public class AdminStudentCourseItemResponse {
    private Long id;
    private String title;
    private Double progress;
    private String status;
    private String lastAccessTime;
}
