package com.noncore.assessment.service.impl;

<<<<<<< HEAD
import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.mapper.AssignmentMapper;
import com.noncore.assessment.mapper.CourseMapper;
=======
import com.noncore.assessment.dto.response.CourseParticipantsResponse;
import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.mapper.AssignmentMapper;
import com.noncore.assessment.mapper.CourseMapper;
import com.noncore.assessment.mapper.EnrollmentMapper;
>>>>>>> fd13d2c (ver 1.7 实现学生端工作台的互联，同时重构了进度的实现逻辑，现在教师可以设置每一节课的视频资料还有作业，只有当学生完成后，进度条才会增加。同时新增/docx目录用于让小白能够学习同时由浅入深的理解该项目。)
import com.noncore.assessment.mapper.UserMapper;
import com.noncore.assessment.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
<<<<<<< HEAD
=======
import java.util.Objects;
import java.util.stream.Collectors;
>>>>>>> fd13d2c (ver 1.7 实现学生端工作台的互联，同时重构了进度的实现逻辑，现在教师可以设置每一节课的视频资料还有作业，只有当学生完成后，进度条才会增加。同时新增/docx目录用于让小白能够学习同时由浅入深的理解该项目。)

@Service
@Transactional
public class StudentServiceImpl implements StudentService {
    private final AssignmentMapper assignmentMapper;
<<<<<<< HEAD

    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    public StudentServiceImpl(UserMapper userMapper, CourseMapper courseMapper, AssignmentMapper assignmentMapper) {
        this.assignmentMapper = assignmentMapper;
=======
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final EnrollmentMapper enrollmentMapper;

    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    public StudentServiceImpl(UserMapper userMapper, CourseMapper courseMapper, AssignmentMapper assignmentMapper, EnrollmentMapper enrollmentMapper) {
        this.assignmentMapper = assignmentMapper;
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
        this.enrollmentMapper = enrollmentMapper; // future use: permission checks and stats
>>>>>>> fd13d2c (ver 1.7 实现学生端工作台的互联，同时重构了进度的实现逻辑，现在教师可以设置每一节课的视频资料还有作业，只有当学生完成后，进度条才会增加。同时新增/docx目录用于让小白能够学习同时由浅入深的理解该项目。)
    }

    @Override
    public List<Assignment> getPendingAssignments(Long studentId) {
        logger.info("获取待处理作业, studentId: {}", studentId);
<<<<<<< HEAD
        return assignmentMapper.findPendingAssignmentsForStudent(studentId); 
=======
        return assignmentMapper.findPendingAssignmentsForStudent(studentId);
>>>>>>> fd13d2c (ver 1.7 实现学生端工作台的互联，同时重构了进度的实现逻辑，现在教师可以设置每一节课的视频资料还有作业，只有当学生完成后，进度条才会增加。同时新增/docx目录用于让小白能够学习同时由浅入深的理解该项目。)
    }

    @Override
    @Transactional
    public void createStudentProfile(User user) {
<<<<<<< HEAD
        // This method is now responsible for creating student-specific records
        // after the core user record has been created by AuthService.
        // For now, as there's no separate student_profiles table,
        // we can just log the action.
        logger.info("Initializing student profile for new user, ID: {}", user.getId());
        // In the future, you might add logic here to create entries in
        // other tables like student_abilities, initial learning_plans, etc.
    }
} 
=======
        logger.info("Initializing student profile for new user, ID: {}", user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public CourseParticipantsResponse getCourseParticipants(Long currentStudentId, Long courseId, String keyword) {
        logger.info("获取课程参与者: courseId={}, currentStudentId={}, keyword={} ", courseId, currentStudentId, keyword);
        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            return CourseParticipantsResponse.builder()
                    .teachers(java.util.Collections.emptyList())
                    .classmates(java.util.Collections.emptyList())
                    .build();
        }

        // 权限校验：教师本人或已选该课程的学生
        boolean isTeacher = course.getTeacherId() != null && course.getTeacherId().equals(currentStudentId);
        boolean isEnrolled = isTeacher || enrollmentMapper.checkEnrollmentExists(currentStudentId, courseId) > 0;
        if (!isEnrolled) {
            return CourseParticipantsResponse.builder()
                    .teachers(java.util.Collections.emptyList())
                    .classmates(java.util.Collections.emptyList())
                    .build();
        }

        // 教师
        User teacher = userMapper.selectUserById(course.getTeacherId());
        CourseParticipantsResponse.Person teacherDto = teacher == null ? null : CourseParticipantsResponse.Person.builder()
                .id(teacher.getId())
                .name(Objects.toString(teacher.getNickname(), teacher.getUsername()))
                .avatar(teacher.getAvatar())
                .role("teacher")
                .build();

        // 同学（同课程）
        String kw = (keyword != null && !keyword.isBlank()) ? ("%" + keyword.trim() + "%") : null;
        List<User> classmates = kw == null
                ? userMapper.selectStudentsByCourseId(courseId)
                : userMapper.selectStudentsByCourseIdWithSearch(courseId, kw);

        List<CourseParticipantsResponse.Person> classmatesDto = classmates.stream()
                .filter(u -> !u.getId().equals(currentStudentId))
                .map(u -> CourseParticipantsResponse.Person.builder()
                        .id(u.getId())
                        .name(Objects.toString(u.getNickname(), u.getUsername()))
                        .avatar(u.getAvatar())
                        .role("student")
                        .build())
                .collect(Collectors.toList());

        return CourseParticipantsResponse.builder()
                .teachers(teacherDto == null ? java.util.Collections.emptyList() : java.util.List.of(teacherDto))
                .classmates(classmatesDto)
                .build();
    }
}
>>>>>>> fd13d2c (ver 1.7 实现学生端工作台的互联，同时重构了进度的实现逻辑，现在教师可以设置每一节课的视频资料还有作业，只有当学生完成后，进度条才会增加。同时新增/docx目录用于让小白能够学习同时由浅入深的理解该项目。)
