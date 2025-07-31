package com.noncore.assessment.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.CourseMapper;
import com.noncore.assessment.mapper.EnrollmentMapper;
import com.noncore.assessment.mapper.UserMapper;
import com.noncore.assessment.service.EnrollmentService;
import com.noncore.assessment.util.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.noncore.assessment.entity.Enrollment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final Logger logger = LoggerFactory.getLogger(EnrollmentServiceImpl.class);

    private final EnrollmentMapper enrollmentMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    public EnrollmentServiceImpl(EnrollmentMapper enrollmentMapper, CourseMapper courseMapper, UserMapper userMapper) {
        this.enrollmentMapper = enrollmentMapper;
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
    }

    @Override
    public void enrollCourse(Long courseId, Long studentId) {
        logger.info("学生选课: courseId={}, studentId={}", courseId, studentId);
        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }
        if (!"published".equals(course.getStatus())) {
            throw new BusinessException(ErrorCode.COURSE_NOT_PUBLISHED);
        }
        if (course.getMaxStudents() != null && course.getEnrollmentCount() >= course.getMaxStudents()) {
            throw new BusinessException(ErrorCode.COURSE_FULL);
        }
        if (isStudentEnrolled(courseId, studentId)) {
            throw new BusinessException(ErrorCode.STUDENT_ALREADY_ENROLLED);
        }
        int result = enrollmentMapper.insertEnrollment(studentId, courseId, "enrolled", LocalDateTime.now());
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "选课失败");
        }
        courseMapper.updateEnrollmentCount(courseId, 1);
        logger.info("选课成功: courseId={}, studentId={}", courseId, studentId);
    }

    @Override
    public void unenrollCourse(Long courseId, Long studentId) {
        logger.info("学生退课: courseId={}, studentId={}", courseId, studentId);
        if (!isStudentEnrolled(courseId, studentId)) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_ENROLLED);
        }
        int result = enrollmentMapper.deleteEnrollment(studentId, courseId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "退课失败");
        }
        courseMapper.updateEnrollmentCount(courseId, -1);
        logger.info("退课成功: courseId={}, studentId={}", courseId, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStudentEnrolled(Long courseId, Long studentId) {
        return enrollmentMapper.checkEnrollmentExists(studentId, courseId) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<User> getCourseStudents(Long teacherId, Long courseId, Integer page, Integer size) {
        logger.info("获取课程学生列表，课程ID: {}, 操作教师ID: {}", courseId, teacherId);
        checkCourseOwnership(courseId, teacherId);
        PageHelper.startPage(page, size);
        List<User> students = userMapper.selectStudentsByCourseId(courseId);
        PageInfo<User> pageInfo = new PageInfo<>(students);
        return PageResult.of(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), pageInfo.getPages());
    }

    @Override
    public void removeStudentFromCourse(Long teacherId, Long courseId, Long studentId) {
        logger.info("移除课程学生，课程ID: {}, 学生ID: {}, 操作教师ID: {}", courseId, studentId, teacherId);
        checkCourseOwnership(courseId, teacherId);
        int result = enrollmentMapper.deleteEnrollment(studentId, courseId);
        if (result > 0) {
            courseMapper.updateEnrollmentCount(courseId, -1);
            logger.info("学生 {} 已从课程 {} 中移除", studentId, courseId);
        } else {
            logger.warn("移除学生失败，未找到对应的选课记录: courseId={}, studentId={}", courseId, studentId);
            throw new BusinessException(ErrorCode.STUDENT_NOT_ENROLLED);
        }
    }

    @Override
    public void addStudentsToCourse(Long teacherId, Long courseId, List<Long> studentIds) {
        logger.info("批量添加学生到课程，课程ID: {}, 学生数量: {}, 操作教师ID: {}", courseId, studentIds.size(), teacherId);
        checkCourseOwnership(courseId, teacherId);

        if (studentIds == null || studentIds.isEmpty()) {
            return;
        }

        // 1. 一次性查询出已注册的学生
        List<Long> alreadyEnrolledIds = enrollmentMapper.selectEnrolledStudentIds(courseId, studentIds);

        // 2. 在内存中过滤出需要新注册的学生
        List<Long> toEnrollIds = studentIds.stream()
                .filter(id -> !alreadyEnrolledIds.contains(id))
                .collect(Collectors.toList());

        if (toEnrollIds.isEmpty()) {
            logger.info("所有提供的学生都已注册课程 {}", courseId);
            return;
        }

        // 3. 准备批量插入的数据
        List<Enrollment> enrollmentsToInsert = new ArrayList<>();
        for (Long studentId : toEnrollIds) {
            Enrollment enrollment = new Enrollment();
            enrollment.setStudentId(studentId);
            enrollment.setCourseId(courseId);
            enrollment.setStatus("enrolled");
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollmentsToInsert.add(enrollment);
        }

        // 4. 执行批量插入
        enrollmentMapper.batchInsertEnrollments(enrollmentsToInsert);
        
        // 5. 更新课程总人数
        courseMapper.updateEnrollmentCount(courseId, toEnrollIds.size());
        logger.info("成功添加 {} 名学生到课程 {}", toEnrollIds.size(), courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getEnrolledCourses(Long studentId) {
        logger.info("获取学生已选课程列表: studentId={}", studentId);
        return courseMapper.selectCoursesByStudentId(studentId);
    }

    private void checkCourseOwnership(Long courseId, Long teacherId) {
        Course course = courseMapper.selectCourseById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }
        if (!teacherId.equals(course.getTeacherId())) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED, "教师没有权限操作该课程");
        }
    }
} 