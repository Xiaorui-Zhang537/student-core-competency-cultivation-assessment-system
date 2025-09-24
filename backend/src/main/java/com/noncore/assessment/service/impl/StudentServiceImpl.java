package com.noncore.assessment.service.impl;

import com.noncore.assessment.dto.response.CourseParticipantsResponse;
import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.mapper.AssignmentMapper;
import com.noncore.assessment.mapper.CourseMapper;
import com.noncore.assessment.mapper.EnrollmentMapper;
import com.noncore.assessment.mapper.UserMapper;
import com.noncore.assessment.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    private final AssignmentMapper assignmentMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final EnrollmentMapper enrollmentMapper;

    public StudentServiceImpl(UserMapper userMapper,
                              CourseMapper courseMapper,
                              AssignmentMapper assignmentMapper,
                              EnrollmentMapper enrollmentMapper) {
        this.assignmentMapper = assignmentMapper;
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
        this.enrollmentMapper = enrollmentMapper; // 预留：权限校验与统计
    }

    @Override
    public List<Assignment> getPendingAssignments(Long studentId) {
        logger.info("获取待处理作业, studentId: {}", studentId);
        return assignmentMapper.findPendingAssignmentsForStudent(studentId);
    }

    @Override
    @Transactional
    public void createStudentProfile(User user) {
        // 当前无独立 student_profiles 表，先记录初始化日志；后续可在此初始化能力画像/学习计划等
        logger.info("Initializing student profile for new user, ID: {}", user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public CourseParticipantsResponse getCourseParticipants(Long currentStudentId, Long courseId, String keyword) {
        logger.info("获取课程参与者: courseId={}, currentStudentId={}, keyword={}", courseId, currentStudentId, keyword);

        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            return CourseParticipantsResponse.builder()
                    .teachers(java.util.Collections.emptyList())
                    .classmates(java.util.Collections.emptyList())
                    .build();
        }

        // 仅允许任课教师或已选该课的学生查看
        boolean isTeacher = course.getTeacherId() != null && course.getTeacherId().equals(currentStudentId);
        boolean isEnrolled = isTeacher || enrollmentMapper.checkEnrollmentExists(currentStudentId, courseId) > 0;
        if (!isEnrolled) {
            return CourseParticipantsResponse.builder()
                    .teachers(java.util.Collections.emptyList())
                    .classmates(java.util.Collections.emptyList())
                    .build();
        }

        // 教师信息
        User teacher = userMapper.selectUserById(course.getTeacherId());
        CourseParticipantsResponse.Person teacherDto = (teacher == null) ? null :
                CourseParticipantsResponse.Person.builder()
                        .id(teacher.getId())
                        .name(teacher.getUsername())
                        .username(teacher.getUsername())
                        .nickname(teacher.getNickname())
                        .avatar(teacher.getAvatar())
                        .role("teacher")
                        .email(teacher.getEmail())
                        .phone(teacher.getPhone())
                        .school(teacher.getSchool())
                        .subject(teacher.getSubject())
                        .studentNo(teacher.getStudentNo())
                        .teacherNo(teacher.getTeacherNo())
                        .birthday(teacher.getBirthday() == null ? null : new java.text.SimpleDateFormat("yyyy-MM-dd").format(teacher.getBirthday()))
                        .country(teacher.getCountry())
                        .province(teacher.getProvince())
                        .city(teacher.getCity())
                        .gender(teacher.getGender())
                        .mbti(teacher.getMbti())
                        .bio(teacher.getBio())
                        .build();

        // 同学信息（支持关键字搜索）
        String kw = (keyword != null && !keyword.isBlank()) ? ("%" + keyword.trim() + "%") : null;
        List<User> classmates = (kw == null)
                ? userMapper.selectStudentsByCourseId(courseId)
                : userMapper.selectStudentsByCourseIdWithSearch(courseId, kw);

        List<CourseParticipantsResponse.Person> classmatesDto = classmates.stream()
                .filter(u -> !u.getId().equals(currentStudentId))
                .map(u -> CourseParticipantsResponse.Person.builder()
                        .id(u.getId())
                        .name(u.getUsername())
                        .username(u.getUsername())
                        .nickname(u.getNickname())
                        .avatar(u.getAvatar())
                        .role("student")
                        .email(u.getEmail())
                        .phone(u.getPhone())
                        .school(u.getSchool())
                        .subject(u.getSubject())
                        .studentNo(u.getStudentNo())
                        .teacherNo(u.getTeacherNo())
                        .birthday(u.getBirthday() == null ? null : new java.text.SimpleDateFormat("yyyy-MM-dd").format(u.getBirthday()))
                        .country(u.getCountry())
                        .province(u.getProvince())
                        .city(u.getCity())
                        .gender(u.getGender())
                        .mbti(u.getMbti())
                        .bio(u.getBio())
                        .build())
                .collect(Collectors.toList());

        return CourseParticipantsResponse.builder()
                .teachers(teacherDto == null ? java.util.Collections.emptyList() : java.util.List.of(teacherDto))
                .classmates(classmatesDto)
                .build();
    }
}