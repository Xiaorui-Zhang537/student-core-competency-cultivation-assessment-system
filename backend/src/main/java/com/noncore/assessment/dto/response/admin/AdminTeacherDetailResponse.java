package com.noncore.assessment.dto.response.admin;

import com.noncore.assessment.entity.User;
import lombok.Builder;
import lombok.Data;

/**
 * 管理员-教师数据中心详情响应。
 *
 * @author System
 * @since 2026-02-14
 */
@Data
@Builder
public class AdminTeacherDetailResponse {
    private User teacher;
    private Long courses;
    private Long assignments;
    private Long activeEnrollments;
}

