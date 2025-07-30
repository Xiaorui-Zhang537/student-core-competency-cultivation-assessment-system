package com.noncore.assessment.service.impl;

import com.noncore.assessment.dto.response.StudentDashboardResponse;
import com.noncore.assessment.entity.*;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.*;
import com.noncore.assessment.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生服务实现类
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final AssignmentMapper assignmentMapper;
    private final SubmissionMapper submissionMapper;
    private final GradeMapper gradeMapper;
    private final LessonProgressMapper lessonProgressMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final NotificationMapper notificationMapper;

    public StudentServiceImpl(UserMapper userMapper, CourseMapper courseMapper, AssignmentMapper assignmentMapper,
                              SubmissionMapper submissionMapper, GradeMapper gradeMapper,
                              LessonProgressMapper lessonProgressMapper, EnrollmentMapper enrollmentMapper,
                              NotificationMapper notificationMapper) {
        this.userMapper = userMapper;
        this.courseMapper = courseMapper;
        this.assignmentMapper = assignmentMapper;
        this.submissionMapper = submissionMapper;
        this.gradeMapper = gradeMapper;
        this.lessonProgressMapper = lessonProgressMapper;
        this.enrollmentMapper = enrollmentMapper;
        this.notificationMapper = notificationMapper;
    }

    @Override
    public StudentDashboardResponse getDashboardData(Long studentId) {
        try {
            logger.info("获取学生仪表板数据，学生ID: {}", studentId);

            StudentDashboardResponse response = new StudentDashboardResponse();

            // 设置学生基本信息
            User student = userMapper.selectUserById(studentId);
            if (student != null) {
                StudentDashboardResponse.StudentProfile profile = getStudentProfile(student);
                response.setProfile(profile);
            }

            // 设置学习统计数据
            StudentDashboardResponse.StudyStats stats = new StudentDashboardResponse.StudyStats();
            
            // 活跃课程数
            List<Course> activeCourses = courseMapper.selectCoursesByStudentId(studentId);
            stats.setActiveCourses(activeCourses != null ? activeCourses.size() : 0);
            
            // 待完成作业数
            // List<Assignment> pendingAssignments = assignmentMapper.selectPendingByStudentId(studentId);
            List<Assignment> pendingAssignments = assignmentMapper.selectAssignmentsByCourseId(null); // 简化替代
            stats.setPendingAssignments(pendingAssignments != null ? pendingAssignments.size() : 0);
            
            // 平均成绩
            BigDecimal averageScore = gradeMapper.calculateAverageScore(studentId);
            stats.setAverageScore(averageScore != null ? averageScore : BigDecimal.ZERO);
            
            // 学习时长统计
            Integer totalStudyTime = lessonProgressMapper.calculateTotalStudyTime(studentId, null);
            stats.setTotalStudyTime(totalStudyTime != null ? totalStudyTime : 0);
            
            // 本周学习时长
            Integer weeklyStudyTime = getWeeklyStudyTime(studentId);
            stats.setWeeklyStudyTime(weeklyStudyTime);
            
            // 整体进度
            BigDecimal overallProgress = lessonProgressMapper.calculateOverallProgress(studentId);
            stats.setOverallProgress(overallProgress != null ? overallProgress : BigDecimal.ZERO);
            
            // 已完成课程数（简化实现，可以后续完善）
            stats.setCompletedCourses(0);
            
            // 未读通知数
            Integer unreadNotifications = notificationMapper.countUnreadNotifications(studentId);
            stats.setUnreadNotifications(unreadNotifications != null ? unreadNotifications : 0);
            
            response.setStudyStats(stats);

            // 设置最近课程列表
            List<Course> recentCourses = getRecentCourses(studentId, 5);
            List<StudentDashboardResponse.RecentCourse> recentCourseList = recentCourses.stream()
                .map(this::convertToRecentCourse)
                .collect(Collectors.toList());
            response.setRecentCourses(recentCourseList);

            // 设置待完成作业列表
            List<StudentDashboardResponse.PendingAssignment> pendingAssignmentList = 
                (pendingAssignments != null ? pendingAssignments : new ArrayList<Assignment>()).stream()
                .limit(5)
                .map(this::convertToPendingAssignment)
                .collect(Collectors.toList());
            response.setPendingAssignments(pendingAssignmentList);

            // 设置最新通知列表
            List<Notification> recentNotifications = notificationMapper.selectRecentNotifications(studentId, 5);
            List<StudentDashboardResponse.RecentNotification> recentNotificationList = recentNotifications.stream()
                .map(this::convertToRecentNotification)
                .collect(Collectors.toList());
            response.setRecentNotifications(recentNotificationList);

            // 设置学习进度数据（用于图表）
            List<Map<String, Object>> progressData = getProgressData(studentId, 7);
            List<StudentDashboardResponse.ProgressData> progressDataList = progressData.stream()
                .map(this::convertToProgressData)
                .collect(Collectors.toList());
            response.setProgressData(progressDataList);

            // 设置能力评估概览
            Map<String, Object> abilityData = getAbilityOverview(studentId);
            StudentDashboardResponse.AbilityOverview abilityOverview = convertToAbilityOverview(abilityData);
            response.setAbilityOverview(abilityOverview);

            logger.info("成功获取学生仪表板数据，学生ID: {}", studentId);
            return response;

        } catch (Exception e) {
            logger.error("获取学生仪表板数据失败，学生ID: {}", studentId, e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "获取学生仪表板数据失败: " + e.getMessage());
        }
    }

    private static StudentDashboardResponse.StudentProfile getStudentProfile(User student) {
        StudentDashboardResponse.StudentProfile profile = new StudentDashboardResponse.StudentProfile();
        profile.setId(student.getId());
        profile.setName(student.getDisplayName() != null ? student.getDisplayName() : student.getUsername());
        profile.setAvatar(student.getAvatar());
        profile.setGrade(student.getGrade());
        profile.setMajor(student.getSubject());
        profile.setStudentNumber(student.getUsername()); // 简化处理
        profile.setEnrollmentDate(student.getCreatedAt());
        return profile;
    }

    @Override
    public User getStudentProfile(Long studentId) {
        logger.info("获取学生个人资料，学生ID: {}", studentId);
        return userMapper.selectUserById(studentId);
    }

    @Override
    public User updateStudentProfile(Long studentId, User profile) {
        logger.info("更新学生个人资料，学生ID: {}", studentId);
        
        profile.setId(studentId);
        profile.updateTimestamp();
        
        int result = userMapper.updateUser(profile);
        if (result > 0) {
            return userMapper.selectUserById(studentId);
        }
        
        throw new BusinessException(ErrorCode.OPERATION_FAILED, "更新学生个人资料失败");
    }

    @Override
    public List<Course> getStudentCourses(Long studentId) {
        logger.info("获取学生课程列表，学生ID: {}", studentId);
        return courseMapper.selectCoursesByStudentId(studentId);
    }

    @Override
    public List<Assignment> getStudentAssignments(Long studentId, Long courseId) {
        logger.info("获取学生作业列表，学生ID: {}, 课程ID: {}", studentId, courseId);
        
        if (courseId != null) {
            // return assignmentMapper.selectByCourseIdForStudent(courseId, studentId);
            return assignmentMapper.selectAssignmentsByCourseId(courseId); // 简化替代
        } else {
            // return assignmentMapper.selectByStudentId(studentId);
            return new ArrayList<>(); // 简化替代
        }
    }

    @Override
    public List<Assignment> getPendingAssignments(Long studentId) {
        logger.info("获取学生待完成作业，学生ID: {}", studentId);
        // return assignmentMapper.selectPendingByStudentId(studentId);
        return new ArrayList<>(); // 简化替代
    }

    @Override
    public List<LessonProgress> getStudentProgress(Long studentId, Long courseId) {
        logger.info("获取学生学习进度，学生ID: {}, 课程ID: {}", studentId, courseId);
        return lessonProgressMapper.selectByStudentAndCourse(studentId, courseId);
    }

    @Override
    public boolean updateLearningProgress(Long studentId, Long courseId, Long lessonId, 
                                        BigDecimal progress, Integer lastPosition) {
        logger.info("更新学习进度，学生ID: {}, 课程ID: {}, 章节ID: {}, 进度: {}%", 
                   studentId, courseId, lessonId, progress);
        
        try {
            // 查找现有进度记录
            LessonProgress existingProgress = lessonProgressMapper
                .selectByStudentCourseLesson(studentId, courseId, lessonId);
            
            if (existingProgress != null) {
                // 更新现有记录
                return lessonProgressMapper.updateProgress(studentId, lessonId, progress, lastPosition) > 0;
            } else {
                // 创建新记录
                LessonProgress newProgress = new LessonProgress(studentId, courseId, lessonId);
                newProgress.setProgress(progress);
                newProgress.setLastPosition(lastPosition);
                newProgress.startLearning();
                
                return lessonProgressMapper.insertLessonProgress(newProgress) > 0;
            }
        } catch (Exception e) {
            logger.error("更新学习进度失败", e);
            return false;
        }
    }

    @Override
    public boolean markLessonCompleted(Long studentId, Long courseId, Long lessonId) {
        logger.info("标记章节完成，学生ID: {}, 课程ID: {}, 章节ID: {}", studentId, courseId, lessonId);
        
        try {
            return lessonProgressMapper.markLessonCompleted(studentId, lessonId) > 0;
        } catch (Exception e) {
            logger.error("标记章节完成失败", e);
            return false;
        }
    }

    @Override
    public BigDecimal calculateAverageGrade(Long studentId) {
        logger.info("计算学生平均成绩，学生ID: {}", studentId);
        BigDecimal average = gradeMapper.calculateAverageScore(studentId);
        return average != null ? average : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateCourseAverageGrade(Long studentId, Long courseId) {
        logger.info("计算学生课程平均成绩，学生ID: {}, 课程ID: {}", studentId, courseId);
        BigDecimal average = gradeMapper.calculateCourseAverageScore(studentId, courseId);
        return average != null ? average : BigDecimal.ZERO;
    }

    @Override
    public Map<String, Object> getStudentStats(Long studentId) {
        logger.info("获取学生学习统计，学生ID: {}", studentId);
        return lessonProgressMapper.getStudentStudyStats(studentId);
    }

    @Override
    public Integer getStudyTimeStats(Long studentId, Long courseId) {
        logger.info("获取学生学习时长统计，学生ID: {}, 课程ID: {}", studentId, courseId);
        Integer studyTime = lessonProgressMapper.calculateTotalStudyTime(studentId, courseId);
        return studyTime != null ? studyTime : 0;
    }

    @Override
    public Integer getWeeklyStudyTime(Long studentId) {
        logger.info("获取学生本周学习时长，学生ID: {}", studentId);
        // 简化实现，返回固定值。实际应该查询本周的学习记录
        return 180; // 3小时
    }

    @Override
    public BigDecimal calculateOverallProgress(Long studentId) {
        logger.info("计算学生整体学习进度，学生ID: {}", studentId);
        BigDecimal progress = lessonProgressMapper.calculateOverallProgress(studentId);
        return progress != null ? progress : BigDecimal.ZERO;
    }

    @Override
    public List<Course> getRecentCourses(Long studentId, Integer limit) {
        logger.info("获取学生最近学习课程，学生ID: {}, 限制数量: {}", studentId, limit);
        
        List<Course> allCourses = courseMapper.selectCoursesByStudentId(studentId);
        
        // 简化实现，返回前几个课程。实际应该按最后学习时间排序
        return allCourses.stream()
            .limit(limit != null ? limit : 5)
            .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getProgressData(Long studentId, Integer days) {
        logger.info("获取学生学习进度数据，学生ID: {}, 天数: {}", studentId, days);
        return lessonProgressMapper.getStudyHeatmapData(studentId, days);
    }

    @Override
    public Map<String, Object> getAbilityOverview(Long studentId) {
        logger.info("获取学生能力评估概览，学生ID: {}", studentId);
        
        // 简化实现，返回模拟数据
        Map<String, Object> abilityData = new HashMap<>();
        abilityData.put("overallScore", new BigDecimal("85.0"));
        abilityData.put("ranking", 15);
        
        List<Map<String, Object>> dimensions = Arrays.asList(
            createDimensionScore("逻辑思维", "88.0"),
            createDimensionScore("创新能力", "82.0"),
            createDimensionScore("沟通表达", "85.0"),
            createDimensionScore("团队协作", "80.0")
        );
        abilityData.put("dimensionScores", dimensions);
        
        return abilityData;
    }

    @Override
    public boolean addStudyNotes(Long studentId, Long lessonId, String notes) {
        logger.info("添加学习笔记，学生ID: {}, 章节ID: {}", studentId, lessonId);
        
        try {
            LessonProgress progress = lessonProgressMapper
                .selectByStudentCourseLesson(studentId, null, lessonId);
            
            if (progress != null) {
                progress.setNotes(notes);
                progress.updateTimestamp();
                return lessonProgressMapper.updateLessonProgress(progress) > 0;
            }
            
            return false;
        } catch (Exception e) {
            logger.error("添加学习笔记失败", e);
            return false;
        }
    }

    @Override
    public boolean rateLesion(Long studentId, Long lessonId, Integer rating) {
        logger.info("给章节评分，学生ID: {}, 章节ID: {}, 评分: {}", studentId, lessonId, rating);
        
        try {
            return lessonProgressMapper.updateRating(studentId, lessonId, rating) > 0;
        } catch (Exception e) {
            logger.error("给章节评分失败", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getStudentRanking(Long studentId, Long courseId) {
        logger.info("获取学生学习排名，学生ID: {}, 课程ID: {}", studentId, courseId);
        
        // 简化实现，返回模拟数据
        Map<String, Object> ranking = new HashMap<>();
        ranking.put("rank", 15);
        ranking.put("totalStudents", 120);
        ranking.put("percentile", 87.5);
        
        return ranking;
    }

    @Override
    public List<Map<String, Object>> getStudyHeatmapData(Long studentId, Integer days) {
        logger.info("获取学生学习热力图数据，学生ID: {}, 天数: {}", studentId, days);
        return lessonProgressMapper.getStudyHeatmapData(studentId, days);
    }

    @Override
    public boolean resetCourseProgress(Long studentId, Long courseId) {
        logger.info("重置课程进度，学生ID: {}, 课程ID: {}", studentId, courseId);
        
        try {
            return lessonProgressMapper.resetCourseProgress(studentId, courseId) > 0;
        } catch (Exception e) {
            logger.error("重置课程进度失败", e);
            return false;
        }
    }

    @Override
    public BigDecimal getCourseCompletionRate(Long studentId, Long courseId) {
        logger.info("获取学生课程完成度，学生ID: {}, 课程ID: {}", studentId, courseId);
        BigDecimal rate = lessonProgressMapper.calculateCourseCompletionRate(studentId, courseId);
        return rate != null ? rate : BigDecimal.ZERO;
    }

    @Override
    public boolean hasAccessToCourse(Long studentId, Long courseId) {
        logger.info("检查学生课程访问权限，学生ID: {}, 课程ID: {}", studentId, courseId);
        
        try {
            Enrollment enrollment = enrollmentMapper.selectEnrollmentByStudentAndCourse(studentId, courseId);
            return enrollment != null && "active".equals(enrollment.getStatus());
        } catch (Exception e) {
            logger.error("检查课程访问权限失败", e);
            return false;
        }
    }

    @Override
    public boolean hasAccessToLesson(Long studentId, Long lessonId) {
        logger.info("检查学生章节访问权限，学生ID: {}, 章节ID: {}", studentId, lessonId);
        
        // 简化实现，实际应该检查课程注册状态和章节权限
        return true;
    }

    // 私有辅助方法
    private StudentDashboardResponse.RecentCourse convertToRecentCourse(Course course) {
        StudentDashboardResponse.RecentCourse recentCourse = new StudentDashboardResponse.RecentCourse();
        recentCourse.setId(course.getId());
        recentCourse.setTitle(course.getTitle());
        recentCourse.setTeacherName("教师名称"); // 简化实现
        recentCourse.setProgress(new BigDecimal("75.0")); // 简化实现
        recentCourse.setLastStudiedAt(LocalDateTime.now());
        recentCourse.setCoverImage(course.getCoverImage());
        return recentCourse;
    }

    private StudentDashboardResponse.PendingAssignment convertToPendingAssignment(Assignment assignment) {
        StudentDashboardResponse.PendingAssignment pendingAssignment = new StudentDashboardResponse.PendingAssignment();
        pendingAssignment.setId(assignment.getId());
        pendingAssignment.setTitle(assignment.getTitle());
        pendingAssignment.setCourseName("课程名称"); // 简化实现
        pendingAssignment.setDueDate(assignment.getDueDate());
        pendingAssignment.setDaysLeft(3); // 简化实现
        pendingAssignment.setUrgency("normal");
        return pendingAssignment;
    }

    private StudentDashboardResponse.RecentNotification convertToRecentNotification(Notification notification) {
        StudentDashboardResponse.RecentNotification recentNotification = new StudentDashboardResponse.RecentNotification();
        recentNotification.setId(notification.getId());
        recentNotification.setTitle(notification.getTitle());
        recentNotification.setContent(notification.getContent());
        recentNotification.setType(notification.getType());
        recentNotification.setIsRead(notification.getIsRead());
        recentNotification.setCreatedAt(notification.getCreatedAt());
        return recentNotification;
    }

    private StudentDashboardResponse.ProgressData convertToProgressData(Map<String, Object> data) {
        StudentDashboardResponse.ProgressData progressData = new StudentDashboardResponse.ProgressData();
        progressData.setDate(data.get("date").toString());
        progressData.setStudyTime((Integer) data.get("studyTime"));
        progressData.setCompletedLessons((Integer) data.get("completedLessons"));
        return progressData;
    }

    private StudentDashboardResponse.AbilityOverview convertToAbilityOverview(Map<String, Object> abilityData) {
        StudentDashboardResponse.AbilityOverview overview = new StudentDashboardResponse.AbilityOverview();
        overview.setOverallScore((BigDecimal) abilityData.get("overallScore"));
        overview.setRanking((Integer) abilityData.get("ranking"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dimensions = (List<Map<String, Object>>) abilityData.get("dimensionScores");
        
        List<StudentDashboardResponse.AbilityOverview.DimensionScore> dimensionScores = dimensions.stream()
            .map(this::convertToDimensionScore)
            .collect(Collectors.toList());
        
        overview.setDimensionScores(dimensionScores);
        return overview;
    }

    private StudentDashboardResponse.AbilityOverview.DimensionScore convertToDimensionScore(Map<String, Object> data) {
        StudentDashboardResponse.AbilityOverview.DimensionScore score = new StudentDashboardResponse.AbilityOverview.DimensionScore();
        score.setName((String) data.get("name"));
        score.setScore(new BigDecimal(data.get("score").toString()));
        return score;
    }

    private Map<String, Object> createDimensionScore(String name, String score) {
        Map<String, Object> dimension = new HashMap<>();
        dimension.put("name", name);
        dimension.put("score", score);
        return dimension;
    }
} 