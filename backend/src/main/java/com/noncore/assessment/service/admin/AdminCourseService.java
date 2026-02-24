package com.noncore.assessment.service.admin;

import com.noncore.assessment.dto.response.admin.AdminCourseDetailResponse;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.User;
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

    /**
     * 管理员分页查询课程学生列表（不做课程归属校验）。
     *
     * <p>说明：用于管理员全局审计/数据中心场景。筛选与排序规则复用教师端的高级查询实现。</p>
     */
    PageResult<User> pageCourseStudents(Long courseId,
                                        Integer page,
                                        Integer size,
                                        String search,
                                        String sortBy,
                                        String activity,
                                        String grade,
                                        String progress);
}

