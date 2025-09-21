package com.noncore.assessment.service.impl;

import com.noncore.assessment.dto.response.TeacherStudentProfileResponse;
import com.noncore.assessment.dto.response.TeacherStudentActivityResponse;
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
import com.noncore.assessment.mapper.LessonProgressMapper;
import com.noncore.assessment.mapper.LearningRecommendationMapper;
import com.noncore.assessment.dto.response.TeacherStudentAlertsResponse;
import com.noncore.assessment.entity.LearningRecommendation;
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
    private final LessonProgressMapper lessonProgressMapper;
    private final LearningRecommendationMapper learningRecommendationMapper;

    public TeacherStudentServiceImpl(UserMapper userMapper,
                                     EnrollmentMapper enrollmentMapper,
                                     GradeMapper gradeMapper,
                                     CourseMapper courseMapper,
                                     LessonProgressMapper lessonProgressMapper,
                                     LearningRecommendationMapper learningRecommendationMapper) {
        this.userMapper = userMapper;
        this.gradeMapper = gradeMapper;
        this.courseMapper = courseMapper;
        this.enrollmentMapper = enrollmentMapper;
        this.lessonProgressMapper = lessonProgressMapper;
        this.learningRecommendationMapper = learningRecommendationMapper;
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

        // 计算总体完成率（可通过 LessonProgressMapper 的整体进度方法）
        java.math.BigDecimal overallProgress = lessonProgressMapper.calculateOverallProgress(studentId);
        Double completionRate = overallProgress == null ? null : overallProgress.doubleValue();

        // 最近访问时间：从 enrollment 中取最大 lastAccessAt
        java.time.LocalDateTime maxAccess = null;
        java.util.List<com.noncore.assessment.entity.Enrollment> enrollmentList = enrollmentMapper.selectEnrollmentsByStudentId(studentId);
        if (enrollmentList != null) {
            for (com.noncore.assessment.entity.Enrollment e : enrollmentList) {
                if (e.getLastAccessAt() != null) {
                    if (maxAccess == null || e.getLastAccessAt().isAfter(maxAccess)) {
                        maxAccess = e.getLastAccessAt();
                    }
                }
            }
        }

        String displayName = user.getNickname() != null ? user.getNickname() :
                ((user.getLastName() != null || user.getFirstName() != null)
                        ? ((user.getLastName() == null ? "" : user.getLastName()) + (user.getFirstName() == null ? "" : user.getFirstName()))
                        : user.getUsername());

        return TeacherStudentProfileResponse.builder()
                .id(user.getId())
                .name(displayName)
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .emailVerified(user.isEmailVerified())
                .studentNo(user.getStudentNo())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .bio(user.getBio())
                .birthday(user.getBirthday())
                .country(user.getCountry())
                .province(user.getProvince())
                .city(user.getCity())
                .phone(user.getPhone())
                .school(user.getSchool())
                .subject(user.getSubject())
                .grade(user.getGrade())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .className(user.getSchool())
                .enrolledCourseCount(courseCount)
                .averageScore(avg != null ? avg.doubleValue() : null)
                .completionRate(completionRate)
                .lastAccessTime(maxAccess == null ? null : java.util.Date.from(maxAccess.atZone(java.time.ZoneId.systemDefault()).toInstant()))
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

    @Override
    public TeacherStudentActivityResponse getStudentActivity(Long teacherId, Long studentId, Integer days, Integer limit) {
        // 权限校验：必须存在课程交集
        List<Course> courses = courseMapper.findCoursesByStudentId(studentId);
        boolean hasIntersection = courses != null && courses.stream().anyMatch(c -> java.util.Objects.equals(c.getTeacherId(), teacherId));
        if (!hasIntersection) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        // 最近访问时间
        java.time.LocalDateTime maxAccess = null;
        java.util.List<com.noncore.assessment.entity.Enrollment> enrollmentList = enrollmentMapper.selectEnrollmentsByStudentId(studentId);
        if (enrollmentList != null) {
            for (com.noncore.assessment.entity.Enrollment e : enrollmentList) {
                if (e.getLastAccessAt() != null) {
                    if (maxAccess == null || e.getLastAccessAt().isAfter(maxAccess)) {
                        maxAccess = e.getLastAccessAt();
                    }
                }
            }
        }

        // 近一周学习总时长（分钟）
        Long weeklyMinutes = lessonProgressMapper.calculateWeeklyStudyTime(studentId);

        // 最近学习章节记录（Mapper 返回 Map 列表，这里做轻度映射）
        java.util.List<java.util.Map<String, Object>> recent = lessonProgressMapper.selectRecentStudiedLessons(studentId, limit == null ? 5 : limit);
        java.util.List<TeacherStudentActivityResponse.RecentLessonDto> recentDtos = new java.util.ArrayList<>();
        if (recent != null) {
            for (java.util.Map<String, Object> r : recent) {
                TeacherStudentActivityResponse.RecentLessonDto dto = TeacherStudentActivityResponse.RecentLessonDto.builder()
                        .lessonId(asLong(r.get("lessonId")))
                        .lessonTitle(asString(r.get("lessonTitle")))
                        .courseId(asLong(r.get("courseId")))
                        .courseTitle(asString(r.get("courseTitle")))
                        .studiedAt(asDate(r.get("studiedAt")))
                        .build();
                recentDtos.add(dto);
            }
        }

        return TeacherStudentActivityResponse.builder()
                .lastAccessTime(maxAccess == null ? null : java.util.Date.from(maxAccess.atZone(java.time.ZoneId.systemDefault()).toInstant()))
                .weeklyStudyMinutes(weeklyMinutes)
                .recentLessons(recentDtos)
                .build();
    }

    @Override
    public TeacherStudentAlertsResponse getStudentAlerts(Long teacherId, Long studentId) {
        // 权限校验
        List<Course> courses = courseMapper.findCoursesByStudentId(studentId);
        boolean hasIntersection = courses != null && courses.stream().anyMatch(c -> java.util.Objects.equals(c.getTeacherId(), teacherId));
        if (!hasIntersection) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        java.util.List<TeacherStudentAlertsResponse.AlertItem> alerts = new java.util.ArrayList<>();

        // 最近访问时间告警（14天未活跃）
        java.time.LocalDateTime maxAccess = null;
        java.util.List<com.noncore.assessment.entity.Enrollment> enrollmentList = enrollmentMapper.selectEnrollmentsByStudentId(studentId);
        if (enrollmentList != null) {
            for (com.noncore.assessment.entity.Enrollment e : enrollmentList) {
                if (e.getLastAccessAt() != null) {
                    if (maxAccess == null || e.getLastAccessAt().isAfter(maxAccess)) {
                        maxAccess = e.getLastAccessAt();
                    }
                }
            }
        }
        if (maxAccess != null && maxAccess.isBefore(java.time.LocalDateTime.now().minusDays(14))) {
            alerts.add(TeacherStudentAlertsResponse.AlertItem.builder()
                    .code("INACTIVE_14D")
                    .message("近14天未活跃")
                    .severity("warn")
                    .build());
        }

        // 低平均分告警（平均分 < 60）
        java.math.BigDecimal avg = gradeMapper.calculateAverageScore(studentId);
        if (avg != null && avg.doubleValue() < 60.0) {
            alerts.add(TeacherStudentAlertsResponse.AlertItem.builder()
                    .code("LOW_SCORE")
                    .message("平均成绩偏低")
                    .severity("warn")
                    .build());
        }

        // 完成率偏低告警（总体完成率 < 50%）
        java.math.BigDecimal overallProgress = lessonProgressMapper.calculateOverallProgress(studentId);
        if (overallProgress != null && overallProgress.doubleValue() < 50.0) {
            alerts.add(TeacherStudentAlertsResponse.AlertItem.builder()
                    .code("LOW_PROGRESS")
                    .message("学习完成率偏低")
                    .severity("info")
                    .build());
        }

        return TeacherStudentAlertsResponse.builder().alerts(alerts).build();
    }

    @Override
    public java.util.List<LearningRecommendation> getStudentRecommendations(Long teacherId, Long studentId, Integer limit) {
        // 权限校验
        List<Course> courses = courseMapper.findCoursesByStudentId(studentId);
        boolean hasIntersection = courses != null && courses.stream().anyMatch(c -> java.util.Objects.equals(c.getTeacherId(), teacherId));
        if (!hasIntersection) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        // 取高优先级建议，limit 默认 6
        java.math.BigDecimal minPriority = java.math.BigDecimal.valueOf(0.5);
        return learningRecommendationMapper.selectByPriority(studentId, minPriority, (limit == null || limit <= 0) ? 6 : limit);
    }

    private static Long asLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return null; }
    }
    private static String asString(Object v) { return v == null ? null : String.valueOf(v); }
    private static java.util.Date asDate(Object v) {
        if (v == null) return null;
        if (v instanceof java.util.Date) return (java.util.Date) v;
        if (v instanceof java.time.LocalDateTime ldt) {
            return java.util.Date.from(ldt.atZone(java.time.ZoneId.systemDefault()).toInstant());
        }
        if (v instanceof java.time.LocalDate ld) {
            return java.util.Date.from(ld.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
        }
        try { return new java.util.Date(Long.parseLong(String.valueOf(v))); } catch (Exception ignore) {}
        return null;
    }
}


