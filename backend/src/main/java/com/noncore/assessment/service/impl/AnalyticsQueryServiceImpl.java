package com.noncore.assessment.service.impl;

import com.noncore.assessment.dto.response.AssignmentAnalyticsResponse;
import com.noncore.assessment.dto.response.ClassPerformanceResponse;
import com.noncore.assessment.dto.response.CourseAnalyticsResponse;
import com.noncore.assessment.dto.response.CourseStudentPerformanceItem;
import com.noncore.assessment.dto.response.CourseStudentPerformanceResponse;
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
    private final LessonProgressMapper lessonProgressMapper;
    private final LessonMapper lessonMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final SubmissionMapper submissionMapper;

    public AnalyticsQueryServiceImpl(UserMapper userMapper, CourseMapper courseMapper, AssignmentMapper assignmentMapper, GradeMapper gradeMapper, LessonProgressMapper lessonProgressMapper, LessonMapper lessonMapper, EnrollmentMapper enrollmentMapper, SubmissionMapper submissionMapper) {
        this.userMapper = userMapper;
        this.courseMapper = courseMapper;
        this.assignmentMapper = assignmentMapper;
        this.gradeMapper = gradeMapper;
        this.lessonProgressMapper = lessonProgressMapper;
        this.lessonMapper = lessonMapper;
        this.enrollmentMapper = enrollmentMapper;
        this.submissionMapper = submissionMapper;
    }

    // Removed legacy single-student progress report implementation

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

        // 课程成绩统计（用于平均分与完成率计算）
        Map<String, Object> gradeStats = gradeMapper.getCourseGradeStats(courseId);
        BigDecimal avgScore = BigDecimal.ZERO;
        long totalGrades = 0L;
        if (gradeStats != null) {
            Object avgObj = gradeStats.get("averageScore");
            if (avgObj instanceof BigDecimal) {
                avgScore = (BigDecimal) avgObj;
            } else if (avgObj instanceof Number) {
                avgScore = BigDecimal.valueOf(((Number) avgObj).doubleValue());
            }
            Object tg = gradeStats.get("totalGrades");
            if (tg instanceof Number) {
                totalGrades = ((Number) tg).longValue();
            }
        }

        int totalStudents = students.size();
        int totalAssignments = assignments.size();
        BigDecimal completionRate = BigDecimal.ZERO;
        long denominator = (long) totalStudents * (long) totalAssignments;
        if (denominator > 0) {
            completionRate = BigDecimal.valueOf(totalGrades)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(denominator), 2, java.math.RoundingMode.HALF_UP);
        }

        return CourseAnalyticsResponse.builder()
                .course(course)
                .totalStudents(totalStudents)
                .activeStudents((int) Math.round(totalStudents * 0.85)) // 可后续用真实活跃度替换
                .totalAssignments(totalAssignments)
                .completionRate(completionRate)
                .averageScore(avgScore)
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
        List<Map<String, Object>> gradeDistribution = gradeMapper.getCourseGradeDistribution(courseId);
        Map<String, Object> activityStats = generateActivityStats(courseId, timeRange);
        
        return ClassPerformanceResponse.builder()
            .course(course)
            .totalStudents(students.size())
            .gradeStats(gradeStats)
            .activityStats(activityStats)
            .gradeDistribution(gradeDistribution)
            .build();
    }

    // Removed unused teaching analytics/report methods

    @Override
    public CourseStudentPerformanceResponse getCourseStudentPerformance(Long teacherId, Long courseId, Integer page, Integer size,
                                                                        String search, String sortBy, String activityFilter, String gradeFilter, String progressFilter) {
        logger.info("获取课程学生表现列表，教师ID: {}, 课程ID: {}, page: {}, size: {}", teacherId, courseId, page, size);
        Course course = courseMapper.selectCourseById(courseId);
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        List<User> students = userMapper.selectStudentsByCourseId(courseId);
        List<Assignment> assignments = assignmentMapper.selectAssignmentsByCourseId(courseId);
        final Set<Long> courseAssignmentIds = assignments.stream().map(Assignment::getId).collect(Collectors.toSet());

        // 构建条目并计算平均分（基于该课程的作业）
        List<CourseStudentPerformanceItem> items = new ArrayList<>();
        for (User s : students) {
            List<Grade> grades = gradeMapper.selectByStudentId(s.getId());
            grades = grades.stream().filter(g -> courseAssignmentIds.contains(g.getAssignmentId())).toList();
            BigDecimal avg = grades.stream()
                    .map(Grade::getScore)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            double averageScore = grades.isEmpty() ? 0.0 : avg.divide(new BigDecimal(grades.size()), 2, RoundingMode.HALF_UP).doubleValue();

            // 进度与活跃度（简化聚合示例）
            Long weeklyVal = lessonProgressMapper.calculateWeeklyStudyTime(s.getId());
            int weekly = weeklyVal == null ? 0 : weeklyVal.intValue();
            String activity = weekly >= 300 ? "high" : (weekly >= 120 ? "medium" : (weekly > 0 ? "low" : "inactive"));

            Integer totalLessons = lessonMapper.countLessonsByCourse(courseId);
            // 统计完成章节数
            List<com.noncore.assessment.entity.LessonProgress> lpList = lessonProgressMapper.selectByStudentAndCourse(s.getId(), courseId);
            Integer completedLessons = 0;
            java.time.LocalDateTime lpLastStudiedAt = null;
            if (lpList != null && !lpList.isEmpty()) {
                completedLessons = (int) lpList.stream().filter(p -> Boolean.TRUE.equals(p.getCompleted())).count();
                lpLastStudiedAt = lpList.stream()
                        .map(com.noncore.assessment.entity.LessonProgress::getLastStudiedAt)
                        .filter(Objects::nonNull)
                        .max(java.time.LocalDateTime::compareTo)
                        .orElse(null);
            }

            // 最近提交时间（限定为本课程的作业）
            java.time.LocalDateTime lastSubmissionAt = null;
            java.util.List<com.noncore.assessment.entity.Submission> submissions = submissionMapper.selectByStudentId(s.getId());
            if (submissions != null && !submissions.isEmpty()) {
                lastSubmissionAt = submissions.stream()
                        .filter(sub -> sub.getAssignmentId() != null && courseAssignmentIds.contains(sub.getAssignmentId()))
                        .map(com.noncore.assessment.entity.Submission::getSubmittedAt)
                        .filter(Objects::nonNull)
                        .max(java.time.LocalDateTime::compareTo)
                        .orElse(null);
            }

            // enrollment 回退（进入课程/访问行为）
            java.time.LocalDateTime enrollmentLastAccess = null;
            com.noncore.assessment.entity.Enrollment enr = enrollmentMapper.selectEnrollmentByStudentAndCourse(s.getId(), courseId);
            if (enr != null) {
                enrollmentLastAccess = enr.getLastAccessAt();
            }

            // 统一 lastActiveAt：取三者最大
            java.time.LocalDateTime lastActiveAt = null;
            if (lpLastStudiedAt != null) lastActiveAt = lpLastStudiedAt;
            if (lastSubmissionAt != null && (lastActiveAt == null || lastSubmissionAt.isAfter(lastActiveAt))) {
                lastActiveAt = lastSubmissionAt;
            }
            if (enrollmentLastAccess != null && (lastActiveAt == null || enrollmentLastAccess.isAfter(lastActiveAt))) {
                lastActiveAt = enrollmentLastAccess;
            }
            CourseStudentPerformanceItem item = CourseStudentPerformanceItem.builder()
                    .studentId(s.getId())
                    .studentName(s.getUsername())
                    .studentNo(s.getUsername())
                    .avatar(s.getAvatar())
                    .progress(lessonProgressMapper.calculateCourseCompletionRate(s.getId(), courseId) != null ?
                            lessonProgressMapper.calculateCourseCompletionRate(s.getId(), courseId).intValue() : 0)
                    .completedLessons(completedLessons)
                    .totalLessons(totalLessons)
                    .averageGrade(averageScore)
                    .activityLevel(activity)
                    .studyTimePerWeek(weekly / 60)
                    .lastActiveAt(lastActiveAt != null ? lastActiveAt.toString() : null)
                    .build();
            items.add(item);
        }

        // 搜索过滤
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase();
            items = items.stream().filter(i ->
                    (i.getStudentName() != null && i.getStudentName().toLowerCase().contains(q)) ||
                    (i.getStudentNo() != null && i.getStudentNo().toLowerCase().contains(q))
            ).toList();
        }

        // 服务器端筛选
        if (activityFilter != null && !activityFilter.isBlank()) {
            String af = activityFilter.toLowerCase();
            items = items.stream().filter(i -> af.equals(String.valueOf(i.getActivityLevel()).toLowerCase())).toList();
        }
        if (gradeFilter != null && !gradeFilter.isBlank()) {
            String gf = gradeFilter.toLowerCase();
            items = items.stream().filter(i -> {
                double g = i.getAverageGrade() == null ? 0.0 : i.getAverageGrade();
                return switch (gf) {
                    case "excellent" -> g >= 90.0;
                    case "good" -> g >= 80.0 && g < 90.0;
                    case "average" -> g >= 70.0 && g < 80.0;
                    case "below" -> g < 70.0;
                    default -> true;
                };
            }).toList();
        }
        if (progressFilter != null && !progressFilter.isBlank()) {
            String pf = progressFilter.toLowerCase();
            items = items.stream().filter(i -> {
                int p = i.getProgress() == null ? 0 : i.getProgress();
                return switch (pf) {
                    case "not-started" -> p == 0;
                    case "in-progress" -> p > 0 && p < 100;
                    case "completed" -> p >= 100;
                    default -> true;
                };
            }).toList();
        }

        // 排序扩展：grade | progress | lastActive | activity | name(默认)
        if ("grade".equalsIgnoreCase(sortBy)) {
            items = items.stream().sorted((a, b) -> Double.compare(b.getAverageGrade() == null ? 0 : b.getAverageGrade(), a.getAverageGrade() == null ? 0 : a.getAverageGrade())).toList();
        } else if ("progress".equalsIgnoreCase(sortBy)) {
            items = items.stream().sorted((a, b) -> Integer.compare(b.getProgress() == null ? 0 : b.getProgress(), a.getProgress() == null ? 0 : a.getProgress())).toList();
        } else if ("lastActive".equalsIgnoreCase(sortBy)) {
            java.util.function.Function<String, Long> toEpoch = (s) -> {
                if (s == null || s.isBlank()) return 0L;
                try { return java.time.LocalDateTime.parse(s).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(); } catch (Exception e) { return 0L; }
            };
            items = items.stream().sorted((a, b) -> Long.compare(toEpoch.apply(b.getLastActiveAt()), toEpoch.apply(a.getLastActiveAt()))).toList();
        } else if ("activity".equalsIgnoreCase(sortBy)) {
            java.util.Map<String, Integer> rank = new java.util.HashMap<>();
            rank.put("high", 4); rank.put("medium", 3); rank.put("low", 2); rank.put("inactive", 1);
            java.util.function.Function<String, Integer> score = (lv) -> rank.getOrDefault(lv == null ? "inactive" : lv.toLowerCase(), 1);
            items = items.stream().sorted((a, b) -> Integer.compare(score.apply(b.getActivityLevel()), score.apply(a.getActivityLevel()))).toList();
        } else {
            items = items.stream().sorted((a, b) -> {
                String an = a.getStudentName() == null ? "" : a.getStudentName();
                String bn = b.getStudentName() == null ? "" : b.getStudentName();
                return an.compareToIgnoreCase(bn);
            }).toList();
        }

        // 分页
        int p = (page == null || page < 1) ? 1 : page;
        int sz = (size == null || size < 1) ? 20 : size;
        int from = Math.min((p - 1) * sz, items.size());
        int to = Math.min(from + sz, items.size());
        List<CourseStudentPerformanceItem> pageItems = items.subList(from, to);

        // 汇总统计（基于过滤后的全集）
        int avgProgress = 0;
        double avgGrade = 0.0;
        int activeCount = 0;
        double passRate = 0.0;
        if (!items.isEmpty()) {
            int sumProgress = items.stream().map(i -> i.getProgress() == null ? 0 : i.getProgress()).reduce(0, Integer::sum);
            avgProgress = Math.round(sumProgress / (float) items.size());
            double sumGrade = items.stream().map(i -> i.getAverageGrade() == null ? 0.0 : i.getAverageGrade()).reduce(0.0, Double::sum);
            avgGrade = Math.round((sumGrade / items.size()) * 10.0) / 10.0;
            activeCount = (int) items.stream().filter(i -> i.getActivityLevel() != null && !"inactive".equalsIgnoreCase(i.getActivityLevel())).count();
            int passCount = (int) items.stream().filter(i -> (i.getAverageGrade() != null ? i.getAverageGrade() : 0.0) >= 60.0).count();
            passRate = Math.round((passCount * 100.0 / items.size()) * 10.0) / 10.0;
        }

        return CourseStudentPerformanceResponse.builder()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .total(items.size())
                .page(p)
                .size(sz)
                .items(pageItems)
                .averageProgress(avgProgress)
                .averageGrade(avgGrade)
                .activeStudents(activeCount)
                .passRate(passRate)
                .build();
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