package com.noncore.assessment.dto.response.admin;

import com.noncore.assessment.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 管理员-学生数据中心详情响应。
 *
 * @author System
 * @since 2026-02-14
 */
@Data
@Builder
public class AdminStudentDetailResponse {
    private User student;
    private Long enrolledCourses;
    private Double completionRate;
    private Double avgGradePercentage;
    private String lastActiveAt;
    private Long abilityReports;
    private List<AdminStudentCourseItemResponse> courses;
    private List<AdminStudentRecentEventResponse> recentEvents;
}

