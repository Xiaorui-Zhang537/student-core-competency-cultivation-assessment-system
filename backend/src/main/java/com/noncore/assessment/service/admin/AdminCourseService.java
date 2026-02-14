package com.noncore.assessment.service.admin;

import com.noncore.assessment.dto.response.admin.AdminCourseDetailResponse;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.util.PageResult;

/**
 * 管理员课程服务。
 *
 * @author System
 * @since 2026-02-14
 */
public interface AdminCourseService {

    PageResult<Course> pageCourses(int page, int size, String query, String category, String difficulty, String status, Long teacherId);

    AdminCourseDetailResponse getCourseDetail(Long courseId);
}

