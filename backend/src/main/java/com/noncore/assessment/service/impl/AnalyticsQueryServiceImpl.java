package com.noncore.assessment.service.impl;

import com.noncore.assessment.dto.response.AssignmentAnalyticsResponse;
import com.noncore.assessment.dto.response.ClassPerformanceResponse;
import com.noncore.assessment.dto.response.CourseAnalyticsResponse;
import com.noncore.assessment.dto.response.StudentProgressReportResponse;
import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.Grade;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.*;
import com.noncore.assessment.service.AnalyticsQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsQueryServiceImpl implements AnalyticsQueryService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsQueryServiceImpl.class);

    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final AssignmentMapper assignmentMapper;
    private final GradeMapper gradeMapper;

    public AnalyticsQueryServiceImpl(UserMapper userMapper, CourseMapper courseMapper, AssignmentMapper assignmentMapper, GradeMapper gradeMapper) {
        this.userMapper = userMapper;
        this.courseMapper = courseMapper;
        this.assignmentMapper = assignmentMapper;
        this.gradeMapper = gradeMapper;
    }

    @Override
    public StudentProgressReportResponse getStudentProgressReport(Long teacherId, Long studentId, Long courseId) {
        logger.info("获取学生进度报告，教师ID: {}, 学生ID: {}, 课程ID: {}", teacherId, studentId, courseId);
        if (courseId != null) {
            Course course = courseMapper.selectCourseById(courseId);
            if (course == null || !teacherId.equals(course.getTeacherId())) {
                throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
            }
        }

        User student = userMapper.selectUserById(studentId);
        List<Grade> grades = gradeMapper.selectByStudentId(studentId);
        if (courseId != null) {
            // 优化N+1查询
            List<Long> assignmentIds = grades.stream().map(Grade::getAssignmentId).distinct().toList();
            if (assignmentIds.isEmpty()) {
                grades = new ArrayList<>();
            } else {
                List<Assignment> assignments = assignmentMapper.selectAssignmentsByCourseId(courseId);
                Set<Long> courseAssignmentIds = assignments.stream().map(Assignment::getId).collect(Collectors.toSet());
                grades = grades.stream()
                        .filter(grade -> courseAssignmentIds.contains(grade.getAssignmentId()))
                        .collect(Collectors.toList());
            }
        }
        
        BigDecimal averageGrade = grades.stream()
            .map(Grade::getScore)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(new BigDecimal(Math.max(grades.size(), 1)), 2, RoundingMode.HALF_UP);

        return StudentProgressReportResponse.builder()
            .student(student)
            .overallProgress(new BigDecimal("75.5")) // Simplified
            .completedLessons(18) // Simplified
            .totalLessons(24) // Simplified
            .studyTimeMinutes(360) // Simplified
            .averageGrade(averageGrade)
            .grades(grades)
            .build();
    }

    @Override
    public CourseAnalyticsResponse getCourseAnalytics(Long teacherId, Long courseId, String timeRange) {
        logger.info("获取课程分析数据，教师ID: {}, 课程ID: {}, 时间范围: {}", teacherId, courseId, timeRange);
        Course course = courseMapper.selectCourseById(courseId);
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        List<User> students = userMapper.selectStudentsByCourseId(courseId);
        List<Assignment> assignments = assignmentMapper.selectAssignmentsByCourseId(courseId);
        List<Map<String, Object>> timeSeriesData = generateTimeSeriesData(timeRange);

        return CourseAnalyticsResponse.builder()
                .course(course)
                .totalStudents(students.size())
                .activeStudents((int)(students.size() * 0.85)) // Simplified
                .totalAssignments(assignments.size())
                .completionRate(new BigDecimal("78.5")) // Simplified
                .averageScore(new BigDecimal("82.3")) // Simplified
                .timeSeriesData(timeSeriesData)
                .build();
    }

    @Override
    public AssignmentAnalyticsResponse getAssignmentAnalytics(Long teacherId, Long assignmentId) {
        logger.info("获取作业分析数据，教师ID: {}, 作业ID: {}", teacherId, assignmentId);
        Assignment assignment = assignmentMapper.selectAssignmentById(assignmentId);
        if (assignment == null) {
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }
        Course course = courseMapper.selectCourseById(assignment.getCourseId());
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new BusinessException(ErrorCode.ASSIGNMENT_ACCESS_DENIED);
        }
        
        List<Map<String, Object>> gradeDistribution = gradeMapper.getGradeDistribution(assignmentId);
        BigDecimal averageScore = gradeMapper.calculateAssignmentAverageScore(assignmentId);
        
        AssignmentAnalyticsResponse.SubmissionStats submissionStats = AssignmentAnalyticsResponse.SubmissionStats.builder()
            .totalSubmissions(35) // Simplified
            .pendingGrading(8) // Simplified
            .graded(27) // Simplified
            .submissionRate(new BigDecimal("87.5")) // Simplified
            .build();

        return AssignmentAnalyticsResponse.builder()
            .assignment(assignment)
            .submissionStats(submissionStats)
            .gradeDistribution(gradeDistribution)
            .averageScore(averageScore != null ? averageScore : BigDecimal.ZERO)
            .build();
    }

    @Override
    public ClassPerformanceResponse getClassPerformance(Long teacherId, Long courseId, String timeRange) {
        logger.info("获取班级表现数据，教师ID: {}, 课程ID: {}, 时间范围: {}", teacherId, courseId, timeRange);
        Course course = courseMapper.selectCourseById(courseId);
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }
        
        List<User> students = userMapper.selectStudentsByCourseId(courseId);
        Map<String, Object> gradeStats = gradeMapper.getCourseGradeStats(courseId);
        Map<String, Object> activityStats = generateActivityStats(courseId, timeRange);
        
        return ClassPerformanceResponse.builder()
            .course(course)
            .totalStudents(students.size())
            .gradeStats(gradeStats)
            .activityStats(activityStats)
            .build();
    }

    @Override
    public Map<String, Object> getTeachingAnalytics(Long teacherId, String timeRange) {
        // Simplified implementation
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> generateTeachingReport(Long teacherId, Long courseId, String reportType, String timeRange) {
        // Simplified implementation
        return new HashMap<>();
    }

    private List<Map<String, Object>> generateTimeSeriesData(String timeRange) {
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
        stats.put("averageSessionTime", 25); // minutes
        return stats;
    }
} 