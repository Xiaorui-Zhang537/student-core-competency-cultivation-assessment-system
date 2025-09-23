package com.noncore.assessment.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.dto.response.StudentCourseResponse;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.CourseMapper;
import com.noncore.assessment.mapper.EnrollmentMapper;
import com.noncore.assessment.mapper.UserMapper;
import com.noncore.assessment.mapper.LessonProgressMapper;
import com.noncore.assessment.service.EnrollmentService;
import com.noncore.assessment.util.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.noncore.assessment.entity.Enrollment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final Logger logger = LoggerFactory.getLogger(EnrollmentServiceImpl.class);

    private final EnrollmentMapper enrollmentMapper;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;
    private final LessonProgressMapper lessonProgressMapper;
    private final PasswordEncoder passwordEncoder;

    public EnrollmentServiceImpl(EnrollmentMapper enrollmentMapper, CourseMapper courseMapper, UserMapper userMapper, LessonProgressMapper lessonProgressMapper, PasswordEncoder passwordEncoder) {
        this.enrollmentMapper = enrollmentMapper;
        this.courseMapper = courseMapper;
        this.userMapper = userMapper;
        this.lessonProgressMapper = lessonProgressMapper;
        this.passwordEncoder = passwordEncoder;
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
        if (Boolean.TRUE.equals(course.getRequireEnrollKey())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "该课程需要入课密钥");
        }
        if (course.getMaxStudents() != null && course.getEnrollmentCount() >= course.getMaxStudents()) {
            throw new BusinessException(ErrorCode.COURSE_FULL);
        }
        if (isStudentEnrolled(courseId, studentId)) {
            throw new BusinessException(ErrorCode.STUDENT_ALREADY_ENROLLED);
        }
        int result = enrollmentMapper.insertEnrollment(studentId, courseId, "active", LocalDateTime.now());
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "选课失败");
        }
        courseMapper.updateEnrollmentCount(courseId, 1);
        logger.info("选课成功: courseId={}, studentId={}", courseId, studentId);
    }

    @Override
    public void enrollCourseWithKey(Long courseId, Long studentId, String enrollKeyPlaintext) {
        logger.info("学生选课(密钥): courseId={}, studentId={}", courseId, studentId);
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
        if (!Boolean.TRUE.equals(course.getRequireEnrollKey())) {
            // 不需要密钥时，允许走无密钥流程
            enrollCourse(courseId, studentId);
            return;
        }
        String storedHash = course.getEnrollKeyHash();
        if (storedHash == null || storedHash.isBlank() || enrollKeyPlaintext == null || enrollKeyPlaintext.isBlank()) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "入课密钥不正确");
        }
        if (!passwordEncoder.matches(enrollKeyPlaintext, storedHash)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "入课密钥不正确");
        }
        int result = enrollmentMapper.insertEnrollment(studentId, courseId, "active", LocalDateTime.now());
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "选课失败");
        }
        courseMapper.updateEnrollmentCount(courseId, 1);
        logger.info("选课成功(密钥): courseId={}, studentId={}", courseId, studentId);
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
    public PageResult<User> getCourseStudents(Long teacherId, Long courseId, Integer page, Integer size, String search, String sortBy, String activity, String grade, String progress) {
        logger.info("获取课程学生列表，课程ID: {}, 操作教师ID: {}", courseId, teacherId);
        checkCourseOwnership(courseId, teacherId);
        PageHelper.startPage(page, size);
        // 优先使用高级查询（已支持keyword与部分排序白名单；其余筛选后续扩展）
        String kw = (search != null && !search.isBlank()) ? "%" + search.trim() + "%" : null;
        List<User> students = userMapper.selectStudentsByCourseIdAdvanced(courseId, kw, activity, grade, progress, sortBy);
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

        if (studentIds.isEmpty()) {
            return;
        }

        // 1. 一次性查询出已注册的学生
        List<Long> alreadyEnrolledIds = enrollmentMapper.selectEnrolledStudentIds(courseId, studentIds);

        // 2. 在内存中过滤出需要新注册的学生
        List<Long> toEnrollIds = studentIds.stream()
                .filter(id -> !alreadyEnrolledIds.contains(id))
                .toList();

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
            enrollment.setStatus("active");
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

    @Override
    @Transactional(readOnly = true)
    public PageResult<StudentCourseResponse> getStudentCoursesPaged(Long studentId, Integer page, Integer size, String keyword) {
        logger.info("分页获取学生课程: studentId={}, page={}, size={}, q={}", studentId, page, size, keyword);
        PageHelper.startPage(page != null ? page : 1, size != null ? size : 12);
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        List<java.util.Map<String, Object>> rows = enrollmentMapper.selectStudentCoursesPaged(studentId, kw);
        // PageInfo 需要以列表构造
        PageInfo<java.util.Map<String, Object>> pageInfo = new PageInfo<>(rows);

        List<StudentCourseResponse> items = rows.stream().map(m -> StudentCourseResponse.builder()
                .id(castLong(m.get("id")))
                .title((String) m.get("title"))
                .description((String) m.get("description"))
                .category((String) m.get("category"))
                .coverImageUrl((String) m.get("coverImageUrl"))
                .teacherName((String) m.get("teacherName"))
                .progress(castDouble(m.get("progress")))
                .enrolledAt(castDateTime(m.get("enrolledAt")))
                .build()).toList();

        return PageResult.of(items, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), pageInfo.getPages());
    }

    private static Long castLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        return Long.valueOf(v.toString());
    }

    private static Double castDouble(Object v) {
        if (v == null) return 0.0;
        if (v instanceof Number) return ((Number) v).doubleValue();
        return Double.valueOf(v.toString());
    }

    private static java.time.LocalDateTime castDateTime(Object v) {
        if (v == null) return null;
        if (v instanceof java.time.LocalDateTime) return (java.time.LocalDateTime) v;
        if (v instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        return java.time.LocalDateTime.parse(v.toString().replace(" ", "T"));
    }

    @Override
    public void resetStudentCourseProgress(Long teacherId, Long courseId, Long studentId) {
        logger.info("重置课程进度: courseId={}, studentId={}, teacherId={}", courseId, studentId, teacherId);
        checkCourseOwnership(courseId, teacherId);
        // 重置章节进度
        lessonProgressMapper.resetCourseProgress(studentId, courseId);
        // 重置选课冗余进度
        enrollmentMapper.updateProgress(studentId, courseId, 0.0);
    }

    @Override
    public int syncStudentCourseProgress(Long studentId) {
        logger.info("批量同步学生课程进度: studentId={}", studentId);
        List<Course> courses = courseMapper.selectCoursesByStudentId(studentId);
        int updated = 0;
        if (courses != null) {
            for (Course c : courses) {
                try {
                    java.math.BigDecimal rate = lessonProgressMapper.calculateCourseCompletionRate(studentId, c.getId());
                    double pct = rate == null ? 0.0 : rate.doubleValue();
                    enrollmentMapper.updateProgress(studentId, c.getId(), pct);
                    updated++;
                } catch (Exception e) {
                    logger.warn("同步课程进度失败: courseId={}, studentId={}", c.getId(), studentId, e);
                }
            }
        }
        return updated;
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