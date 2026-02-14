package com.noncore.assessment.service.admin;

import com.noncore.assessment.dto.response.admin.AdminStudentDetailResponse;
import com.noncore.assessment.dto.response.admin.AdminTeacherDetailResponse;

/**
 * 管理员-学生/教师数据中心服务。
 *
 * @author System
 * @since 2026-02-14
 */
public interface AdminPeopleService {

    AdminStudentDetailResponse getStudentDetail(Long studentId);

    AdminTeacherDetailResponse getTeacherDetail(Long teacherId);
}

