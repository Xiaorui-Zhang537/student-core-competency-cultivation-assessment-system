package com.noncore.assessment.dto.response.admin;

import com.noncore.assessment.entity.Course;
import lombok.Builder;
import lombok.Data;

/**
 * 管理员课程详情响应。
 *
 * @author System
 * @since 2026-02-14
 */
@Data
@Builder
public class AdminCourseDetailResponse {
    private Course course;
    private Long activeEnrollments;
}

