package com.noncore.assessment.service.admin.impl;

import com.noncore.assessment.dto.response.admin.AdminCourseDetailResponse;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.EnrollmentMapper;
import com.noncore.assessment.service.CourseDiscoveryService;
import com.noncore.assessment.service.CourseService;
import com.noncore.assessment.service.admin.AdminCourseService;
import com.noncore.assessment.util.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理员课程服务实现。
 *
 * @author System
 * @since 2026-02-14
 */
@Service
@Transactional(readOnly = true)
public class AdminCourseServiceImpl implements AdminCourseService {

    private final CourseDiscoveryService courseDiscoveryService;
    private final CourseService courseService;
    private final EnrollmentMapper enrollmentMapper;

    public AdminCourseServiceImpl(CourseDiscoveryService courseDiscoveryService,
                                  CourseService courseService,
                                  EnrollmentMapper enrollmentMapper) {
        this.courseDiscoveryService = courseDiscoveryService;
        this.courseService = courseService;
        this.enrollmentMapper = enrollmentMapper;
    }

    @Override
    public PageResult<Course> pageCourses(int page, int size, String query, String category, String difficulty, String status, Long teacherId) {
        int p = Math.max(1, page);
        int s = Math.min(Math.max(1, size), 100);
        return courseDiscoveryService.getCourses(p, s, query, category, difficulty, status, teacherId);
    }

    @Override
    public AdminCourseDetailResponse getCourseDetail(Long courseId) {
        Course c = courseService.getCourseById(courseId);
        if (c == null) throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        Long active = enrollmentMapper.countActiveByCourse(courseId);
        return AdminCourseDetailResponse.builder()
                .course(c)
                .activeEnrollments(active == null ? 0L : active)
                .build();
    }
}

