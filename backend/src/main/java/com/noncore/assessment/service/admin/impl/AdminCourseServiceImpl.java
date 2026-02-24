package com.noncore.assessment.service.admin.impl;

import com.noncore.assessment.dto.response.admin.AdminCourseDetailResponse;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.EnrollmentMapper;
import com.noncore.assessment.mapper.UserMapper;
import com.noncore.assessment.service.CourseDiscoveryService;
import com.noncore.assessment.service.CourseService;
import com.noncore.assessment.service.admin.AdminCourseService;
import com.noncore.assessment.util.PageResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private final UserMapper userMapper;

    public AdminCourseServiceImpl(CourseDiscoveryService courseDiscoveryService,
                                  CourseService courseService,
                                  EnrollmentMapper enrollmentMapper,
                                  UserMapper userMapper) {
        this.courseDiscoveryService = courseDiscoveryService;
        this.courseService = courseService;
        this.enrollmentMapper = enrollmentMapper;
        this.userMapper = userMapper;
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

    @Override
    public PageResult<User> pageCourseStudents(Long courseId,
                                               Integer page,
                                               Integer size,
                                               String search,
                                               String sortBy,
                                               String activity,
                                               String grade,
                                               String progress) {
        Course c = courseService.getCourseById(courseId);
        if (c == null) throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);

        int p = page == null ? 1 : Math.max(1, page);
        int s = size == null ? 20 : Math.min(Math.max(1, size), 200);

        String kw = (search != null && !search.isBlank()) ? "%" + search.trim() + "%" : null;
        PageHelper.startPage(p, s);
        List<User> students = userMapper.selectStudentsByCourseIdAdvanced(courseId, kw, activity, grade, progress, sortBy);
        PageInfo<User> pi = new PageInfo<>(students);
        return PageResult.of(pi.getList(), pi.getPageNum(), pi.getPageSize(), pi.getTotal(), pi.getPages());
    }
}

