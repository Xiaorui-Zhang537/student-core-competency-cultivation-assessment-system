package com.noncore.assessment.service.impl;

import com.noncore.assessment.dto.response.TeacherStudentProfileResponse;
import com.noncore.assessment.dto.response.CourseStudentBasicResponse;
import com.noncore.assessment.dto.response.CourseStudentBasicItem;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.CourseMapper;
import com.noncore.assessment.mapper.EnrollmentMapper;
import com.noncore.assessment.mapper.GradeMapper;
import com.noncore.assessment.mapper.UserMapper;
import com.noncore.assessment.service.TeacherStudentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class TeacherStudentServiceImpl implements TeacherStudentService {

    private final UserMapper userMapper;
    private final GradeMapper gradeMapper;
    private final CourseMapper courseMapper;
    private final EnrollmentMapper enrollmentMapper;

    public TeacherStudentServiceImpl(UserMapper userMapper,
                                     EnrollmentMapper enrollmentMapper,
                                     GradeMapper gradeMapper,
                                     CourseMapper courseMapper) {
        this.userMapper = userMapper;
        this.gradeMapper = gradeMapper;
        this.courseMapper = courseMapper;
        this.enrollmentMapper = enrollmentMapper;
    }

    @Override
    public TeacherStudentProfileResponse getStudentProfile(Long teacherId, Long studentId) {
        User user = userMapper.selectUserById(studentId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        // 仅允许查看与当前教师存在课程交集的学生
        List<Course> courses = courseMapper.findCoursesByStudentId(studentId);
        boolean hasIntersection = courses != null && courses.stream().anyMatch(c -> Objects.equals(c.getTeacherId(), teacherId));
        if (!hasIntersection) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        int courseCount = courses != null ? courses.size() : 0;
        BigDecimal avg = gradeMapper.calculateAverageScore(studentId);

        return TeacherStudentProfileResponse.builder()
                .id(user.getId())
                .name(user.getNickname() != null ? user.getNickname() : user.getUsername())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .className(user.getSchool())
                .enrolledCourseCount(courseCount)
                .averageScore(avg != null ? avg.doubleValue() : null)
                .rank(null)
                .percentile(null)
                .build();
    }

    @Override
    public List<Course> getStudentCourses(Long teacherId, Long studentId) {
        List<Course> courses = courseMapper.findCoursesByStudentId(studentId);
        if (courses == null || courses.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        courses.removeIf(c -> c.getTeacherId() != null && !teacherId.equals(c.getTeacherId()));
        return courses;
    }

    @Override
    public CourseStudentBasicResponse getCourseStudentsBasic(Long teacherId, Long courseId, Integer page, Integer size, String keyword) {
        if (courseId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }
        // 课程归属校验
        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }
        if (course.getTeacherId() != null && !teacherId.equals(course.getTeacherId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        // 基于 Enrollment 获取选课学生ID列表
        List<com.noncore.assessment.entity.Enrollment> enrollments = enrollmentMapper.selectEnrollmentsByCourseId(courseId);
        if (enrollments == null || enrollments.isEmpty()) {
            return CourseStudentBasicResponse.builder()
                    .courseId(courseId)
                    .courseTitle(course.getTitle())
                    .total(0L)
                    .page(page == null ? 1 : page)
                    .size(size == null ? 10000 : size)
                    .items(java.util.Collections.emptyList())
                    .build();
        }

        // 过滤关键字（按学生姓名/学号）
        List<Long> studentIds = new java.util.ArrayList<>();
        for (com.noncore.assessment.entity.Enrollment e : enrollments) {
            if (e.getStudentId() != null) studentIds.add(e.getStudentId());
        }
        java.util.List<CourseStudentBasicItem> items = new java.util.ArrayList<>();
        for (Long sid : studentIds) {
            com.noncore.assessment.entity.User u = userMapper.selectUserById(sid);
            if (u == null) continue;
            if (keyword != null && !keyword.trim().isEmpty()) {
                String name = u.getNickname() != null ? u.getNickname() : u.getUsername();
                String no = u.getStudentNo();
                String kw = keyword.toLowerCase();
                String nm = name == null ? "" : name.toLowerCase();
                String sno = no == null ? "" : no.toLowerCase();
                if (!nm.contains(kw) && !sno.contains(kw)) {
                    continue;
                }
            }
            java.math.BigDecimal avg = gradeMapper.calculateCourseAverageScore(sid, courseId);
            // progress 可从 Enrollment 冗余获取（若表有 progress 字段）
            Double progress = null;
            for (com.noncore.assessment.entity.Enrollment e : enrollments) {
                if (sid.equals(e.getStudentId())) {
                    progress = e.getProgress();
                    break;
                }
            }
            items.add(CourseStudentBasicItem.builder()
                    .studentId(sid)
                    .studentName(u.getNickname() != null ? u.getNickname() : u.getUsername())
                    .studentNo(u.getStudentNo())
                    .avatar(u.getAvatar())
                    .progress(progress)
                    .averageGrade(avg == null ? null : avg.doubleValue())
                    .lastActiveAt(null)
                    .joinedAt(null)
                    .build());
        }

        // 简单分页
        int p = (page == null || page < 1) ? 1 : page;
        int s = (size == null || size < 1) ? 10000 : size;
        int from = (p - 1) * s;
        int to = Math.min(from + s, items.size());
        java.util.List<CourseStudentBasicItem> paged = from >= items.size() ? java.util.Collections.emptyList() : items.subList(from, to);

        return CourseStudentBasicResponse.builder()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .total((long) items.size())
                .page(p)
                .size(s)
                .items(paged)
                .build();
    }
}


