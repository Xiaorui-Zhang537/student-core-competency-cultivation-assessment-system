package com.noncore.assessment.service.admin.impl;

import com.noncore.assessment.dto.response.admin.AdminStudentDetailResponse;
import com.noncore.assessment.dto.response.admin.AdminTeacherDetailResponse;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.AdminPeopleMapper;
import com.noncore.assessment.mapper.UserMapper;
import com.noncore.assessment.service.admin.AdminPeopleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理员-学生/教师数据中心服务实现。
 *
 * @author System
 * @since 2026-02-14
 */
@Service
@Transactional(readOnly = true)
public class AdminPeopleServiceImpl implements AdminPeopleService {

    private final UserMapper userMapper;
    private final AdminPeopleMapper adminPeopleMapper;

    public AdminPeopleServiceImpl(UserMapper userMapper, AdminPeopleMapper adminPeopleMapper) {
        this.userMapper = userMapper;
        this.adminPeopleMapper = adminPeopleMapper;
    }

    @Override
    public AdminStudentDetailResponse getStudentDetail(Long studentId) {
        User u = userMapper.selectUserById(studentId);
        if (u == null || u.isDeleted()) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        if (!"student".equalsIgnoreCase(u.getRole())) throw new BusinessException(ErrorCode.INVALID_PARAMETER, "该用户不是学生");

        Long enrolled = adminPeopleMapper.countEnrolledCourses(studentId);
        Double avg = adminPeopleMapper.avgGradePercentage(studentId);
        String lastActiveAt = adminPeopleMapper.lastActiveAt(studentId);
        Long reports = adminPeopleMapper.countAbilityReports(studentId);

        u.setPassword(null);
        return AdminStudentDetailResponse.builder()
                .student(u)
                .enrolledCourses(enrolled == null ? 0L : enrolled)
                .avgGradePercentage(avg)
                .lastActiveAt(lastActiveAt)
                .abilityReports(reports == null ? 0L : reports)
                .build();
    }

    @Override
    public AdminTeacherDetailResponse getTeacherDetail(Long teacherId) {
        User u = userMapper.selectUserById(teacherId);
        if (u == null || u.isDeleted()) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        if (!"teacher".equalsIgnoreCase(u.getRole())) throw new BusinessException(ErrorCode.INVALID_PARAMETER, "该用户不是教师");

        Long courses = adminPeopleMapper.countCoursesByTeacher(teacherId);
        Long assignments = adminPeopleMapper.countAssignmentsByTeacher(teacherId);
        Long activeEnrollments = adminPeopleMapper.countActiveEnrollmentsForTeacher(teacherId);

        u.setPassword(null);
        return AdminTeacherDetailResponse.builder()
                .teacher(u)
                .courses(courses == null ? 0L : courses)
                .assignments(assignments == null ? 0L : assignments)
                .activeEnrollments(activeEnrollments == null ? 0L : activeEnrollments)
                .build();
    }
}

