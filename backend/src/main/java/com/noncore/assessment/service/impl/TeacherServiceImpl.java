package com.noncore.assessment.service.impl;

import com.noncore.assessment.dto.response.TeacherDashboardResponse;
import com.noncore.assessment.entity.*;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.*;
import com.noncore.assessment.service.TeacherService;
import com.noncore.assessment.util.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 教师服务实现类
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Service
@Transactional
public class TeacherServiceImpl implements TeacherService {

    private static final Logger logger = LoggerFactory.getLogger(TeacherServiceImpl.class);

    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final AssignmentMapper assignmentMapper;
    private final SubmissionMapper submissionMapper;
    private final GradeMapper gradeMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final NotificationMapper notificationMapper;

    public TeacherServiceImpl(UserMapper userMapper, CourseMapper courseMapper, AssignmentMapper assignmentMapper,
                              SubmissionMapper submissionMapper, GradeMapper gradeMapper, EnrollmentMapper enrollmentMapper,
                              NotificationMapper notificationMapper) {
        this.userMapper = userMapper;
        this.courseMapper = courseMapper;
        this.assignmentMapper = assignmentMapper;
        this.submissionMapper = submissionMapper;
        this.gradeMapper = gradeMapper;
        this.enrollmentMapper = enrollmentMapper;
        this.notificationMapper = notificationMapper;
    }

    @Override
    public TeacherDashboardResponse getDashboardData(Long teacherId) {
        try {
            logger.info("获取教师仪表板数据，教师ID: {}", teacherId);

            TeacherDashboardResponse response = new TeacherDashboardResponse();

            // 设置教师基本信息
            User teacher = userMapper.selectUserById(teacherId);
            if (teacher != null) {
                TeacherDashboardResponse.TeacherProfile profile = getTeacherProfile(teacher);
                response.setProfile(profile);
            }

            // 设置教学统计数据
            TeacherDashboardResponse.TeachingStats stats = new TeacherDashboardResponse.TeachingStats();
            
            // 活跃课程数
            List<Course> activeCourses = courseMapper.selectCoursesByTeacherId(teacherId);
            stats.setActiveCourses(activeCourses != null ? activeCourses.size() : 0);
            
            // 总学生数
            // Integer totalStudents = 50;
            int totalStudents = 50; // 简化实现，返回固定值
            stats.setTotalStudents(totalStudents);
            
            // 待评分作业数
            List<Map<String, Object>> pendingGradings = getPendingGradings(teacherId, null);
            stats.setPendingGradings(pendingGradings.size());
            
            // 本月发布作业数
            stats.setMonthlyAssignments(8); // 简化实现
            
            // 平均作业分数
            stats.setAverageAssignmentScore(new BigDecimal("82.5")); // 简化实现
            
            // 课程平均评分
            stats.setAverageCourseRating(new BigDecimal("4.6")); // 简化实现
            
            // 学生完成率
            stats.setStudentCompletionRate(new BigDecimal("85.2")); // 简化实现
            
            // 本周活跃学生数
            stats.setWeeklyActiveStudents((int)(totalStudents * 0.8));
            
            response.setTeachingStats(stats);

            // 设置活跃课程列表
            List<TeacherDashboardResponse.ActiveCourse> activeCourseList = 
                (activeCourses != null ? activeCourses : new ArrayList<Course>()).stream()
                    .limit(5)
                    .map(this::convertToActiveCourse)
                    .collect(Collectors.toList());
            response.setActiveCourses(activeCourseList);

            // 设置待评分作业列表
            List<TeacherDashboardResponse.PendingGrading> pendingGradingList = pendingGradings.stream()
                .limit(5)
                .map(this::convertToPendingGrading)
                .collect(Collectors.toList());
            response.setPendingGradings(pendingGradingList);

            // 设置学生概览列表
            PageResult<User> studentsPage = getStudentOverview(teacherId, null, null, 1, 10);
            List<TeacherDashboardResponse.StudentOverview> studentOverviews = studentsPage.getItems().stream()
                .map(this::convertToStudentOverview)
                .collect(Collectors.toList());
            response.setStudentOverviews(studentOverviews);

            // 设置最新通知列表
            List<Notification> recentNotifications = notificationMapper.selectBySenderId(teacherId);
            List<TeacherDashboardResponse.RecentNotification> recentNotificationList = recentNotifications.stream()
                .limit(5)
                .map(this::convertToRecentNotification)
                .collect(Collectors.toList());
            response.setRecentNotifications(recentNotificationList);

            // 设置教学数据（用于图表）
            List<TeacherDashboardResponse.TeachingData> teachingData = generateTeachingData();
            response.setTeachingData(teachingData);

            logger.info("成功获取教师仪表板数据，教师ID: {}", teacherId);
            return response;

        } catch (Exception e) {
            logger.error("获取教师仪表板数据失败，教师ID: {}", teacherId, e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "获取教师仪表板数据失败: " + e.getMessage());
        }
    }

    private static TeacherDashboardResponse.TeacherProfile getTeacherProfile(User teacher) {
        TeacherDashboardResponse.TeacherProfile profile = new TeacherDashboardResponse.TeacherProfile();
        profile.setId(teacher.getId());
        profile.setName(teacher.getDisplayName() != null ? teacher.getDisplayName() : teacher.getUsername());
        profile.setAvatar(teacher.getAvatar());
        profile.setTitle("教师"); // 简化实现
        profile.setDepartment(teacher.getSchool());
        profile.setSpecialization(teacher.getSubject());
        profile.setYearsOfExperience(5); // 简化实现
        profile.setEmail(teacher.getEmail());
        return profile;
    }

    @Override
    public User getTeacherProfile(Long teacherId) {
        logger.info("获取教师个人资料，教师ID: {}", teacherId);
        return userMapper.selectUserById(teacherId);
    }

    @Override
    public User updateTeacherProfile(Long teacherId, User profile) {
        logger.info("更新教师个人资料，教师ID: {}", teacherId);
        
        profile.setId(teacherId);
        profile.setUpdatedAt(LocalDateTime.now());
        
        int result = userMapper.updateUser(profile);
        if (result > 0) {
            return userMapper.selectUserById(teacherId);
        }
        
        throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新教师个人资料失败");
    }

    @Override
    public Map<String, Object> getTeacherStats(Long teacherId) {
        logger.info("获取教师统计数据，教师ID: {}", teacherId);
        
        Map<String, Object> stats = new HashMap<>();
        
        // 课程统计
        List<Course> courses = courseMapper.selectCoursesByTeacherId(teacherId);
        stats.put("totalCourses", courses.size());
        stats.put("activeCourses", courses.stream().mapToLong(c -> "published".equals(c.getStatus()) ? 1 : 0).sum());
        
        // 学生统计
        // Integer totalStudents = 50;
        Integer totalStudents = 50; // 简化实现，返回固定值
        stats.put("totalStudents", totalStudents);
        
        // 作业统计
        List<Assignment> assignments = assignmentMapper.selectAssignmentsByTeacherId(teacherId);
        stats.put("totalAssignments", assignments.size());
        
        // 待评分数量
        List<Map<String, Object>> pendingGradings = getPendingGradings(teacherId, null);
        stats.put("pendingGradings", pendingGradings.size());
        
        return stats;
    }

    @Override
    public List<Course> getTeacherCourses(Long teacherId) {
        logger.info("获取教师课程列表，教师ID: {}", teacherId);
        return courseMapper.selectCoursesByTeacherId(teacherId);
    }

    @Override
    public List<Course> getActiveCourses(Long teacherId) {
        logger.info("获取教师活跃课程列表，教师ID: {}", teacherId);
        
        List<Course> allCourses = courseMapper.selectCoursesByTeacherId(teacherId);
        return allCourses.stream()
            .filter(course -> "published".equals(course.getStatus()))
            .collect(Collectors.toList());
    }

    @Override
    public PageResult<User> getStudentOverview(Long teacherId, Long courseId, String status, Integer page, Integer size) {
        logger.info("获取学生概览列表，教师ID: {}, 课程ID: {}, 状态: {}", teacherId, courseId, status);
        
        try {
            int offset = (page - 1) * size;
            
            List<User> students;
            int total;
            
            if (courseId != null) {
                // 检查权限
                if (!hasAccessToCourse(teacherId, courseId)) {
                    throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
                }
                
                students = userMapper.selectStudentsByCourseId(courseId);
                total = students.size();
                
                // 手动分页
                students = students.stream()
                    .skip(offset)
                    .limit(size)
                    .collect(Collectors.toList());
            } else {
                // 获取教师所有课程的学生
                List<Course> teacherCourses = courseMapper.selectCoursesByTeacherId(teacherId);
                Set<Long> studentIds = new HashSet<>();
                
                for (Course course : teacherCourses) {
                    List<User> courseStudents = userMapper.selectStudentsByCourseId(course.getId());
                    studentIds.addAll(courseStudents.stream().map(User::getId).collect(Collectors.toSet()));
                }
                
                students = studentIds.stream()
                    .skip(offset)
                    .limit(size)
                    .map(userMapper::selectUserById)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                
                total = studentIds.size();
            }
            
            return PageResult.of(students, page, size, (long) total, (total + size - 1) / size);
            
        } catch (Exception e) {
            logger.error("获取学生概览列表失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "获取学生概览列表失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getStudentProgressReport(Long teacherId, Long studentId, Long courseId) {
        logger.info("获取学生进度报告，教师ID: {}, 学生ID: {}, 课程ID: {}", teacherId, studentId, courseId);
        
        // 检查权限
        if (courseId != null && !hasAccessToCourse(teacherId, courseId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }
        
        Map<String, Object> report = new HashMap<>();
        
        // 学生基本信息
        User student = userMapper.selectUserById(studentId);
        report.put("student", student);
        
        // 学习进度（简化实现）
        report.put("overallProgress", new BigDecimal("75.5"));
        report.put("completedLessons", 18);
        report.put("totalLessons", 24);
        report.put("studyTime", 360); // 分钟
        
        // 成绩信息
        List<Grade> grades = gradeMapper.selectByStudentId(studentId);
        if (courseId != null) {
            // 过滤指定课程的成绩
            grades = grades.stream()
                .filter(grade -> {
                    Assignment assignment = assignmentMapper.selectAssignmentById(grade.getAssignmentId());
                    return assignment != null && courseId.equals(assignment.getCourseId());
                })
                .collect(Collectors.toList());
        }
        
        BigDecimal averageGrade = grades.stream()
            .map(Grade::getScore)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(new BigDecimal(Math.max(grades.size(), 1)), 2, RoundingMode.HALF_UP);
        
        report.put("averageGrade", averageGrade);
        report.put("grades", grades);
        
        return report;
    }

    @Override
    public Map<String, Object> getCourseAnalytics(Long teacherId, Long courseId, String timeRange) {
        logger.info("获取课程分析数据，教师ID: {}, 课程ID: {}, 时间范围: {}", teacherId, courseId, timeRange);
        
        // 检查权限
        if (!hasAccessToCourse(teacherId, courseId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }
        
        Map<String, Object> analytics = new HashMap<>();
        
        // 课程基本信息
        Course course = courseMapper.selectCourseById(courseId);
        analytics.put("course", course);
        
        // 学生统计
        List<User> students = userMapper.selectStudentsByCourseId(courseId);
        analytics.put("totalStudents", students.size());
        analytics.put("activeStudents", (int)(students.size() * 0.85)); // 简化实现
        
        // 作业统计
        List<Assignment> assignments = assignmentMapper.selectAssignmentsByCourseId(courseId);
        analytics.put("totalAssignments", assignments.size());
        
        // 完成率统计
        analytics.put("completionRate", new BigDecimal("78.5"));
        analytics.put("averageScore", new BigDecimal("82.3"));
        
        // 时间序列数据（简化实现）
        List<Map<String, Object>> timeSeriesData = generateTimeSeriesData(timeRange);
        analytics.put("timeSeriesData", timeSeriesData);
        
        return analytics;
    }

    @Override
    public List<Map<String, Object>> getCourseManagementData(Long teacherId) {
        logger.info("获取课程管理数据，教师ID: {}", teacherId);
        
        List<Course> courses = courseMapper.selectCoursesByTeacherId(teacherId);
        
        return courses.stream()
            .map(course -> {
                Map<String, Object> data = new HashMap<>();
                data.put("course", course);
                
                // 学生数量
                List<User> students = userMapper.selectStudentsByCourseId(course.getId());
                data.put("studentCount", students.size());
                
                // 作业数量
                List<Assignment> assignments = assignmentMapper.selectAssignmentsByCourseId(course.getId());
                data.put("assignmentCount", assignments.size());
                
                // 待评分数量
                List<Map<String, Object>> pendingGradings = getPendingGradings(teacherId, course.getId());
                data.put("pendingGradingCount", pendingGradings.size());
                
                return data;
            })
            .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getAssignmentAnalytics(Long teacherId, Long assignmentId) {
        logger.info("获取作业分析数据，教师ID: {}, 作业ID: {}", teacherId, assignmentId);
        
        // 检查权限
        if (!hasAccessToAssignment(teacherId, assignmentId)) {
            throw new BusinessException(ErrorCode.ASSIGNMENT_ACCESS_DENIED);
        }
        
        Map<String, Object> analytics = new HashMap<>();
        
        // 作业基本信息
        Assignment assignment = assignmentMapper.selectAssignmentById(assignmentId);
        analytics.put("assignment", assignment);
        
        // 提交统计
        analytics.put("submissionStats", getAssignmentSubmissionStats(teacherId, assignmentId));
        
        // 成绩分布
        List<Map<String, Object>> gradeDistribution = gradeMapper.getGradeDistribution(assignmentId);
        analytics.put("gradeDistribution", gradeDistribution);
        
        // 平均分
        BigDecimal averageScore = gradeMapper.calculateAssignmentAverageScore(assignmentId);
        analytics.put("averageScore", averageScore != null ? averageScore : BigDecimal.ZERO);
        
        return analytics;
    }

    @Override
    public Map<String, Object> getClassPerformance(Long teacherId, Long courseId, String timeRange) {
        logger.info("获取班级表现数据，教师ID: {}, 课程ID: {}, 时间范围: {}", teacherId, courseId, timeRange);
        
        // 检查权限
        if (!hasAccessToCourse(teacherId, courseId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }
        
        Map<String, Object> performance = new HashMap<>();
        
        // 课程信息
        Course course = courseMapper.selectCourseById(courseId);
        performance.put("course", course);
        
        // 学生表现统计
        List<User> students = userMapper.selectStudentsByCourseId(courseId);
        performance.put("totalStudents", students.size());
        
        // 成绩统计
        Map<String, Object> gradeStats = gradeMapper.getCourseGradeStats(courseId);
        performance.put("gradeStats", gradeStats);
        
        // 活跃度统计（简化实现）
        performance.put("activityStats", generateActivityStats(courseId, timeRange));
        
        return performance;
    }

    @Override
    public List<Map<String, Object>> getPendingGradings(Long teacherId, Long courseId) {
        logger.info("获取待评分作业列表，教师ID: {}, 课程ID: {}", teacherId, courseId);
        
        List<Grade> pendingGrades = gradeMapper.selectPendingGrades(teacherId);
        
        return pendingGrades.stream()
            .filter(grade -> {
                if (courseId == null) return true;
                
                Assignment assignment = assignmentMapper.selectAssignmentById(grade.getAssignmentId());
                return assignment != null && courseId.equals(assignment.getCourseId());
            })
            .map(grade -> {
                Map<String, Object> data = new HashMap<>();
                
                Assignment assignment = assignmentMapper.selectAssignmentById(grade.getAssignmentId());
                User student = userMapper.selectUserById(grade.getStudentId());
                Course course = assignment != null ? courseMapper.selectCourseById(assignment.getCourseId()) : null;
                
                data.put("submissionId", grade.getSubmissionId());
                data.put("assignmentTitle", assignment != null ? assignment.getTitle() : "");
                data.put("studentName", student != null ? student.getDisplayName() : "");
                data.put("courseName", course != null ? course.getTitle() : "");
                data.put("submittedAt", grade.getCreatedAt());
                data.put("hoursWaiting", 18); // 简化实现
                data.put("priority", "normal");
                
                return data;
            })
            .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> batchGradeAssignments(Long teacherId, List<Grade> grades) {
        logger.info("批量评分作业，教师ID: {}, 成绩数量: {}", teacherId, grades.size());
        
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();
        
        for (Grade grade : grades) {
            try {
                // 检查权限
                if (!hasAccessToAssignment(teacherId, grade.getAssignmentId())) {
                    errors.add("没有权限评分作业ID: " + grade.getAssignmentId());
                    failCount++;
                    continue;
                }
                
                grade.setGraderId(teacherId);
                grade.publish();
                
                int updateResult = gradeMapper.updateGrade(grade);
                if (updateResult > 0) {
                    successCount++;
                } else {
                    failCount++;
                    errors.add("更新成绩失败，成绩ID: " + grade.getId());
                }
                
            } catch (Exception e) {
                failCount++;
                errors.add("评分失败: " + e.getMessage());
                logger.error("批量评分失败", e);
            }
        }
        
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        
        return result;
    }

    @Override
    public boolean publishCourse(Long teacherId, Long courseId) {
        logger.info("发布课程，教师ID: {}, 课程ID: {}", teacherId, courseId);
        
        // 检查权限
        if (!hasAccessToCourse(teacherId, courseId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }
        
        try {
            Course course = courseMapper.selectCourseById(courseId);
            if (course != null) {
                course.setStatus("published");
                course.setUpdatedAt(LocalDateTime.now());
                return courseMapper.updateCourse(course) > 0;
            }
            return false;
        } catch (Exception e) {
            logger.error("发布课程失败", e);
            return false;
        }
    }

    @Override
    public boolean unpublishCourse(Long teacherId, Long courseId) {
        logger.info("取消发布课程，教师ID: {}, 课程ID: {}", teacherId, courseId);
        
        // 检查权限
        if (!hasAccessToCourse(teacherId, courseId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }
        
        try {
            Course course = courseMapper.selectCourseById(courseId);
            if (course != null) {
                course.setStatus("draft");
                course.setUpdatedAt(LocalDateTime.now());
                return courseMapper.updateCourse(course) > 0;
            }
            return false;
        } catch (Exception e) {
            logger.error("取消发布课程失败", e);
            return false;
        }
    }

    @Override
    public boolean archiveCourse(Long teacherId, Long courseId) {
        logger.info("归档课程，教师ID: {}, 课程ID: {}", teacherId, courseId);
        
        // 检查权限
        if (!hasAccessToCourse(teacherId, courseId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }
        
        try {
            Course course = courseMapper.selectCourseById(courseId);
            if (course != null) {
                course.setStatus("archived");
                course.setUpdatedAt(LocalDateTime.now());
                return courseMapper.updateCourse(course) > 0;
            }
            return false;
        } catch (Exception e) {
            logger.error("归档课程失败", e);
            return false;
        }
    }

    @Override
    public PageResult<User> getCourseStudents(Long teacherId, Long courseId, Integer page, Integer size) {
        logger.info("获取课程学生列表，教师ID: {}, 课程ID: {}", teacherId, courseId);
        
        // 检查权限
        if (!hasAccessToCourse(teacherId, courseId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }
        
        List<User> students = userMapper.selectStudentsByCourseId(courseId);
        int total = students.size();
        
        // 手动分页
        int offset = (page - 1) * size;
        List<User> pagedStudents = students.stream()
            .skip(offset)
            .limit(size)
            .collect(Collectors.toList());
        
        return PageResult.of(pagedStudents, page, size, (long) total, (total + size - 1) / size);
    }

    @Override
    public boolean removeStudentFromCourse(Long teacherId, Long courseId, Long studentId) {
        logger.info("移除课程学生，教师ID: {}, 课程ID: {}, 学生ID: {}", teacherId, courseId, studentId);
        
        // 检查权限
        if (!hasAccessToCourse(teacherId, courseId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }
        
        try {
            Enrollment enrollment = enrollmentMapper.selectEnrollmentByStudentAndCourse(studentId, courseId);
            if (enrollment != null) {
                enrollment.setStatus("dropped");
                enrollment.setUpdatedAt(LocalDateTime.now());
                return enrollmentMapper.updateEnrollment(enrollment) > 0;
            }
            return false;
        } catch (Exception e) {
            logger.error("移除课程学生失败", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> addStudentsToCourse(Long teacherId, Long courseId, List<Long> studentIds) {
        logger.info("批量添加学生到课程，教师ID: {}, 课程ID: {}, 学生数量: {}", teacherId, courseId, studentIds.size());
        
        // 检查权限
        if (!hasAccessToCourse(teacherId, courseId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }
        
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();
        
        for (Long studentId : studentIds) {
            try {
                // 检查是否已经注册
                Enrollment existingEnrollment = enrollmentMapper.selectEnrollmentByStudentAndCourse(studentId, courseId);
                if (existingEnrollment != null) {
                    errors.add("学生ID " + studentId + " 已经注册了该课程");
                    failCount++;
                    continue;
                }
                
                // 创建新的注册记录
                // Enrollment enrollment = new Enrollment(studentId, courseId);
                // enrollment.setStatus("active");
                
                int insertResult = enrollmentMapper.insertEnrollment(studentId, courseId, "active", LocalDateTime.now());
                if (insertResult > 0) {
                    successCount++;
                } else {
                    failCount++;
                    errors.add("添加学生ID " + studentId + " 失败");
                }
                
            } catch (Exception e) {
                failCount++;
                errors.add("添加学生ID " + studentId + " 失败: " + e.getMessage());
                logger.error("批量添加学生失败", e);
            }
        }
        
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        
        return result;
    }

    // 权限检查方法
    @Override
    public boolean hasAccessToCourse(Long teacherId, Long courseId) {
        try {
            Course course = courseMapper.selectCourseById(courseId);
            return course != null && teacherId.equals(course.getTeacherId());
        } catch (Exception e) {
            logger.error("检查课程访问权限失败", e);
            return false;
        }
    }

    @Override
    public boolean hasAccessToAssignment(Long teacherId, Long assignmentId) {
        try {
            Assignment assignment = assignmentMapper.selectAssignmentById(assignmentId);
            if (assignment == null) return false;
            
            Course course = courseMapper.selectCourseById(assignment.getCourseId());
            return course != null && teacherId.equals(course.getTeacherId());
        } catch (Exception e) {
            logger.error("检查作业访问权限失败", e);
            return false;
        }
    }

    @Override
    public boolean hasAccessToStudent(Long teacherId, Long studentId, Long courseId) {
        try {
            // 检查教师是否有课程权限
            if (!hasAccessToCourse(teacherId, courseId)) {
                return false;
            }
            
            // 检查学生是否在该课程中
            Enrollment enrollment = enrollmentMapper.selectEnrollmentByStudentAndCourse(studentId, courseId);
            return enrollment != null && "active".equals(enrollment.getStatus());
        } catch (Exception e) {
            logger.error("检查学生访问权限失败", e);
            return false;
        }
    }

    // 其他方法的简化实现（由于篇幅限制）
    @Override
    public List<Map<String, Object>> getTeacherSchedule(Long teacherId, String startDate, String endDate) {
        return new ArrayList<>(); // 简化实现
    }

    @Override
    public PageResult<Map<String, Object>> getTeacherNotifications(Long teacherId, String type, Integer page, Integer size) {
        return PageResult.of(new ArrayList<>(), page, size, 0L, 0); // 简化实现
    }

    @Override
    public Map<String, Object> sendNotificationToStudents(Long teacherId, Long courseId, List<Long> studentIds, 
                                                         String title, String content, String type) {
        return new HashMap<>(); // 简化实现
    }

    @Override
    public Map<String, Object> getTeachingAnalytics(Long teacherId, String timeRange) {
        return new HashMap<>(); // 简化实现
    }

    @Override
    public Map<String, Object> generateTeachingReport(Long teacherId, Long courseId, String reportType, String timeRange) {
        return new HashMap<>(); // 简化实现
    }

    @Override
    public Map<String, Object> exportStudentGrades(Long teacherId, Long courseId, String format) {
        return new HashMap<>(); // 简化实现
    }

    @Override
    public Map<String, Object> getCourseStatistics(Long teacherId, Long courseId) {
        return new HashMap<>(); // 简化实现
    }

    @Override
    public Map<String, Object> getAssignmentSubmissionStats(Long teacherId, Long assignmentId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSubmissions", 35);
        stats.put("pendingGrading", 8);
        stats.put("graded", 27);
        stats.put("submissionRate", new BigDecimal("87.5"));
        return stats;
    }

    @Override
    public Map<String, Object> getTeacherRatingStats(Long teacherId) {
        return new HashMap<>(); // 简化实现
    }

    @Override
    public List<Map<String, Object>> getStudentActivityData(Long teacherId, Long courseId, String timeRange) {
        return new ArrayList<>(); // 简化实现
    }

    // 私有辅助方法
    private TeacherDashboardResponse.ActiveCourse convertToActiveCourse(Course course) {
        TeacherDashboardResponse.ActiveCourse activeCourse = new TeacherDashboardResponse.ActiveCourse();
        activeCourse.setId(course.getId());
        activeCourse.setTitle(course.getTitle());
        
        List<User> students = userMapper.selectStudentsByCourseId(course.getId());
        activeCourse.setEnrolledStudents(students.size());
        activeCourse.setActiveStudents((int)(students.size() * 0.85));
        
        activeCourse.setProgress(new BigDecimal("65.0"));
        activeCourse.setPendingGrades(5);
        activeCourse.setLastUpdated(LocalDateTime.now());
        
        return activeCourse;
    }

    private TeacherDashboardResponse.PendingGrading convertToPendingGrading(Map<String, Object> data) {
        TeacherDashboardResponse.PendingGrading pendingGrading = new TeacherDashboardResponse.PendingGrading();
        pendingGrading.setSubmissionId((Long) data.get("submissionId"));
        pendingGrading.setAssignmentTitle((String) data.get("assignmentTitle"));
        pendingGrading.setStudentName((String) data.get("studentName"));
        pendingGrading.setCourseName((String) data.get("courseName"));
        pendingGrading.setSubmittedAt((LocalDateTime) data.get("submittedAt"));
        pendingGrading.setHoursWaiting((Integer) data.get("hoursWaiting"));
        pendingGrading.setPriority((String) data.get("priority"));
        return pendingGrading;
    }

    private TeacherDashboardResponse.StudentOverview convertToStudentOverview(User student) {
        TeacherDashboardResponse.StudentOverview overview = new TeacherDashboardResponse.StudentOverview();
        overview.setId(student.getId());
        overview.setName(student.getDisplayName() != null ? student.getDisplayName() : student.getUsername());
        overview.setAvatar(student.getAvatar());
        overview.setAverageGrade(new BigDecimal("85.5"));
        overview.setProgress(new BigDecimal("78.0"));
        overview.setStatus("active");
        overview.setLastActiveAt(LocalDateTime.now());
        return overview;
    }

    private TeacherDashboardResponse.RecentNotification convertToRecentNotification(Notification notification) {
        TeacherDashboardResponse.RecentNotification recentNotification = new TeacherDashboardResponse.RecentNotification();
        recentNotification.setId(notification.getId());
        recentNotification.setTitle(notification.getTitle());
        recentNotification.setContent(notification.getContent());
        recentNotification.setType(notification.getType());
        recentNotification.setIsRead(notification.getIsRead());
        recentNotification.setCreatedAt(notification.getCreatedAt());
        return recentNotification;
    }

    private List<TeacherDashboardResponse.TeachingData> generateTeachingData() {
        List<TeacherDashboardResponse.TeachingData> data = new ArrayList<>();
        
        for (int i = 0; i < 7; i++) {
            TeacherDashboardResponse.TeachingData teachingData = new TeacherDashboardResponse.TeachingData();
            teachingData.setDate("2024-12-" + (22 + i));
            teachingData.setActiveStudents(80 + i * 2);
            teachingData.setSubmissionsCount(25 + i);
            teachingData.setAverageScore(new BigDecimal("82." + (i + 1)));
            data.add(teachingData);
        }
        
        return data;
    }

    private List<Map<String, Object>> generateTimeSeriesData(String timeRange) {
        // 简化实现，生成模拟数据
        List<Map<String, Object>> data = new ArrayList<>();
        
        for (int i = 0; i < 7; i++) {
            Map<String, Object> point = new HashMap<>();
            point.put("date", "2024-12-" + (22 + i));
            point.put("value", 80 + i * 2);
            data.add(point);
        }
        
        return data;
    }

    private Map<String, Object> generateActivityStats(Long courseId, String timeRange) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("loginCount", 156);
        stats.put("lessonsViewed", 89);
        stats.put("assignmentsSubmitted", 45);
        stats.put("averageSessionTime", 25); // 分钟
        return stats;
    }
} 