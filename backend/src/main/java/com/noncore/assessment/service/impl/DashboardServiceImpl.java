package com.noncore.assessment.service.impl;

import com.noncore.assessment.dto.response.StudentDashboardResponse;
import com.noncore.assessment.dto.response.TeacherDashboardResponse;
import com.noncore.assessment.mapper.*;
import com.noncore.assessment.service.DashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final CourseMapper courseMapper;
    private final AssignmentMapper assignmentMapper;
    private final GradeMapper gradeMapper;
    private final LessonProgressMapper lessonProgressMapper;
    private final NotificationMapper notificationMapper;
    private final StudentAbilityMapper studentAbilityMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final SubmissionMapper submissionMapper;
    private final UserMapper userMapper;

    public DashboardServiceImpl(CourseMapper courseMapper,
                                AssignmentMapper assignmentMapper,
                                GradeMapper gradeMapper,
                                LessonProgressMapper lessonProgressMapper,
                                NotificationMapper notificationMapper,
                                StudentAbilityMapper studentAbilityMapper,
                                EnrollmentMapper enrollmentMapper,
                                SubmissionMapper submissionMapper,
                                UserMapper userMapper) {
        this.courseMapper = courseMapper;
        this.assignmentMapper = assignmentMapper;
        this.gradeMapper = gradeMapper;
        this.lessonProgressMapper = lessonProgressMapper;
        this.notificationMapper = notificationMapper;
        this.studentAbilityMapper = studentAbilityMapper;
        this.enrollmentMapper = enrollmentMapper;
        this.submissionMapper = submissionMapper;
        this.userMapper = userMapper;
    }

    @Override
    public StudentDashboardResponse getStudentDashboardData(Long studentId) {
        long activeCourses = courseMapper.countActiveCoursesByStudent(studentId);
        if (activeCourses == 0L) {
            try {
                // 兼容兜底：若按课程联表统计为0，则按选课记录数量兜底
                long enrolled = enrollmentMapper.countEnrollments(studentId, null, null);
                if (enrolled > 0L) {
                    activeCourses = enrolled;
                }
            } catch (Exception ignore) {}
        }
        long pendingAssignmentsCount = assignmentMapper.countPendingAssignments(studentId);
        BigDecimal averageScore = Optional.ofNullable(gradeMapper.calculateAverageScore(studentId)).orElse(BigDecimal.ZERO);
        long totalStudyTime = Optional.ofNullable(lessonProgressMapper.calculateTotalStudyTime(studentId, null)).orElse(0);
        long weeklyStudyTime = Optional.ofNullable(lessonProgressMapper.calculateWeeklyStudyTime(studentId)).orElse(0L);
        Double overallProgress = Optional.ofNullable(lessonProgressMapper.calculateOverallProgress(studentId)).map(java.math.BigDecimal::doubleValue).orElse(0.0);
        long unreadNotifications = Optional.ofNullable(notificationMapper.countUnreadByRecipient(studentId))
                .map(Long::valueOf).orElse(0L);

        if (log.isDebugEnabled()) {
            log.debug("StudentDashboard stats studentId={}, activeCourses={}, pendingAssignments={}, avgScore={}, totalStudyTime(min)={}, weeklyStudyTime(min)={}, overallProgress={}, unreadNoti={}",
                    studentId, activeCourses, pendingAssignmentsCount, averageScore, totalStudyTime, weeklyStudyTime, overallProgress, unreadNotifications);
        }

        StudentDashboardResponse.Stats stats = StudentDashboardResponse.Stats.builder()
                .activeCourses(activeCourses)
                .pendingAssignments(pendingAssignmentsCount)
                .averageScore(averageScore.doubleValue())
                .totalStudyTime(totalStudyTime / 60) // to minutes
                .weeklyStudyTime(weeklyStudyTime / 60) // to minutes
                .overallProgress(overallProgress)
                .unreadNotifications(unreadNotifications)
                .build();

        List<StudentDashboardResponse.RecentCourseDto> recentCourses = courseMapper.findRecentCoursesByStudent(studentId, 5);
        // Fallback：若基于学习进度的最近课程为空，则退回到基于选课记录的最近课程
        if (recentCourses == null || recentCourses.isEmpty()) {
            List<Map<String, Object>> fallback = enrollmentMapper.selectStudentCoursesPaged(studentId, null);
            if (fallback != null && !fallback.isEmpty()) {
                recentCourses = fallback.stream().limit(5).map(m -> StudentDashboardResponse.RecentCourseDto.builder()
                        .id(m.get("id") != null ? Long.valueOf(String.valueOf(m.get("id"))) : null)
                        .title(String.valueOf(m.getOrDefault("title", "")))
                        .teacherName(String.valueOf(m.getOrDefault("teacherName", "")))
                        .progress(m.get("progress") instanceof Number ? ((Number) m.get("progress")).doubleValue() : 0.0)
                        .coverImage(m.get("coverImage") != null ? String.valueOf(m.get("coverImage")) : (m.get("coverImageUrl") != null ? String.valueOf(m.get("coverImageUrl")) : null))
                        .lastStudied(null)
                        .build()).collect(Collectors.toList());
            }
        }
        List<StudentDashboardResponse.PendingAssignmentDto> pendingAssignments = assignmentMapper.findPendingAssignments(studentId, 5);
        List<StudentDashboardResponse.RecentNotificationDto> recentNotifications = notificationMapper.findRecentNotifications(studentId, 5);
        List<StudentDashboardResponse.RecentGradeDto> recentGrades = gradeMapper.findRecentGradesByStudent(studentId, 5);
        Double abilityOverallScore = Optional.ofNullable(studentAbilityMapper.selectOverallScoreByStudentId(studentId)).map(java.math.BigDecimal::doubleValue).orElse(0.0);

        return StudentDashboardResponse.builder()
                .stats(stats)
                .recentCourses(recentCourses)
                .pendingAssignments(pendingAssignments)
                .recentNotifications(recentNotifications)
                .recentGrades(recentGrades)
                .abilityOverallScore(abilityOverallScore)
                .build();
    }

    @Override
    public TeacherDashboardResponse getTeacherDashboardData(Long teacherId) {
        long activeCoursesCount = courseMapper.countActiveByTeacher(teacherId);
        long totalStudents = enrollmentMapper.countStudentsByTeacher(teacherId);
        long pendingGradingsCount = submissionMapper.countPendingByTeacher(teacherId);
        long monthlyAssignments = assignmentMapper.countMonthlyByTeacher(teacherId);
        // TODO: The getAverageScoreByTeacher method was removed. A new, efficient query is needed.
        // For now, returning a placeholder value.
        double averageAssignmentScore = 0.0;
        double averageCourseRating = Optional.ofNullable(lessonProgressMapper.getAverageRatingByTeacher(teacherId)).orElse(0.0);
        double studentCompletionRate = Optional.ofNullable(lessonProgressMapper.getAverageCompletionRateByTeacher(teacherId)).orElse(0.0);
        long weeklyActiveStudents = enrollmentMapper.countWeeklyActiveStudentsByTeacher(teacherId);

        TeacherDashboardResponse.Stats stats = TeacherDashboardResponse.Stats.builder()
                .activeCourses(activeCoursesCount)
                .totalStudents(totalStudents)
                .pendingGradingsCount(pendingGradingsCount)
                .monthlyAssignments(monthlyAssignments)
                .averageAssignmentScore(averageAssignmentScore)
                .averageCourseRating(averageCourseRating)
                .studentCompletionRate(studentCompletionRate)
                .weeklyActiveStudents(weeklyActiveStudents)
                .build();

        List<TeacherDashboardResponse.ActiveCourseDto> activeCourses = courseMapper.findActiveWithStatsByTeacher(teacherId, 5);
        List<TeacherDashboardResponse.PendingGradingDto> pendingGradings = submissionMapper.findPendingByTeacher(teacherId, 5);
        List<TeacherDashboardResponse.StudentOverviewDto> studentOverviews = userMapper.findStudentOverviewsByTeacher(teacherId, 10);

        return TeacherDashboardResponse.builder()
                .stats(stats)
                .activeCourses(activeCourses)
                .pendingGradings(pendingGradings)
                .studentOverviews(studentOverviews)
                .build();
    }
} 