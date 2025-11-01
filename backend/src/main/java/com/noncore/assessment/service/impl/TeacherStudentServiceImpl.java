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
import com.noncore.assessment.service.AbilityAnalyticsService;
import com.noncore.assessment.dto.request.AbilityRadarQuery;
import com.noncore.assessment.dto.response.AbilityRadarResponse;
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
    private final AbilityAnalyticsService abilityAnalyticsService;
    private final com.noncore.assessment.service.AiConversationService aiConversationService;
    private final com.noncore.assessment.service.CommunityService communityService;

    public TeacherStudentServiceImpl(UserMapper userMapper,
                                     EnrollmentMapper enrollmentMapper,
                                     GradeMapper gradeMapper,
                                     CourseMapper courseMapper,
                                     LessonProgressMapper lessonProgressMapper,
                                     LearningRecommendationMapper learningRecommendationMapper,
                                     com.noncore.assessment.service.AiConversationService aiConversationService,
                                     com.noncore.assessment.service.CommunityService communityService,
                                     AbilityAnalyticsService abilityAnalyticsService) {
        this.userMapper = userMapper;
        this.gradeMapper = gradeMapper;
        this.courseMapper = courseMapper;
        this.enrollmentMapper = enrollmentMapper;
        this.lessonProgressMapper = lessonProgressMapper;
        this.learningRecommendationMapper = learningRecommendationMapper;
        this.aiConversationService = aiConversationService;
        this.communityService = communityService;
        this.abilityAnalyticsService = abilityAnalyticsService;
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
            String displayName;
            boolean hasCnName = (u.getLastName() != null && !u.getLastName().isBlank()) || (u.getFirstName() != null && !u.getFirstName().isBlank());
            if (hasCnName) {
                // 中文姓名按 姓 + 名 拼接
                displayName = (u.getLastName() == null ? "" : u.getLastName()) + (u.getFirstName() == null ? "" : u.getFirstName());
            } else if (u.getNickname() != null && !u.getNickname().isBlank()) {
                displayName = u.getNickname();
            } else {
                displayName = u.getUsername();
            }
            items.add(CourseStudentBasicItem.builder()
                    .studentId(sid)
                    .studentName(displayName)
                    .firstName(u.getFirstName())
                    .lastName(u.getLastName())
                    .nickname(u.getNickname())
                    .username(u.getUsername())
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

        // 最近学习章节（已有）
        java.util.List<java.util.Map<String, Object>> recent = lessonProgressMapper.selectRecentStudiedLessons(studentId, (limit == null || limit <= 0) ? 5 : limit);
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

        // 统一事件流（第一期先拼装：lesson + visit + ai + community，不落库）
        java.util.List<TeacherStudentActivityResponse.ActivityEventDto> events = new java.util.ArrayList<>();
        for (TeacherStudentActivityResponse.RecentLessonDto rl : recentDtos) {
            events.add(TeacherStudentActivityResponse.ActivityEventDto.builder()
                    .eventType("lesson")
                    .title(rl.getLessonTitle())
                    .courseId(rl.getCourseId())
                    .courseTitle(rl.getCourseTitle())
                    .occurredAt(rl.getStudiedAt())
                    .durationSeconds(null)
                    .link(null)
                    .build());
        }
        if (maxAccess != null) {
            events.add(TeacherStudentActivityResponse.ActivityEventDto.builder()
                    .eventType("visit")
                    .title("访问课程")
                    .courseId(null)
                    .courseTitle(null)
                    .occurredAt(java.util.Date.from(maxAccess.atZone(java.time.ZoneId.systemDefault()).toInstant()))
                    .durationSeconds(null)
                    .link(null)
                    .build());
        }
        // 社区发帖/回复：取最近一条帖子与评论
        try {
            var myPosts = communityService.getUserPosts(studentId, 1, 1);
            if (myPosts != null && myPosts.getItems() != null && !myPosts.getItems().isEmpty()) {
                var p = myPosts.getItems().get(0);
                java.util.Date created = p.getCreatedAt() == null ? null : java.util.Date.from(p.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant());
                events.add(TeacherStudentActivityResponse.ActivityEventDto.builder()
                        .eventType("post")
                        .title(String.valueOf(p.getTitle()))
                        .courseId(null)
                        .courseTitle(null)
                        .occurredAt(created)
                        .durationSeconds(null)
                        .link(null)
                        .build());
            }
            var myComments = communityService.getUserComments(studentId, 1, 1);
            if (myComments != null && myComments.getItems() != null && !myComments.getItems().isEmpty()) {
                var c = myComments.getItems().get(0);
                String snippet = c.getContent();
                if (snippet != null && snippet.length() > 32) snippet = snippet.substring(0, 32);
                java.util.Date created = c.getCreatedAt() == null ? null : java.util.Date.from(c.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant());
                events.add(TeacherStudentActivityResponse.ActivityEventDto.builder()
                        .eventType("reply")
                        .title(snippet)
                        .courseId(null)
                        .courseTitle(null)
                        .occurredAt(created)
                        .durationSeconds(null)
                        .link(null)
                        .build());
            }
        } catch (Exception ignore) {}
        // AI 对话：无直接按学生过滤的接口，这里暂不调用，避免错误归属；后续若提供“按学生ID取会话/消息”，可在此加入 eventType='ai'
        // TODO: 若后续需要引入 submission/quiz/discussion，可在此处追加 Mapper 查询并合并排序
        events.sort((a, b) -> {
            long ta = a.getOccurredAt() == null ? 0L : a.getOccurredAt().getTime();
            long tb = b.getOccurredAt() == null ? 0L : b.getOccurredAt().getTime();
            return Long.compare(tb, ta);
        });

        return TeacherStudentActivityResponse.builder()
                .lastAccessTime(maxAccess == null ? null : java.util.Date.from(maxAccess.atZone(java.time.ZoneId.systemDefault()).toInstant()))
                .weeklyStudyMinutes(weeklyMinutes)
                .recentLessons(recentDtos)
                .recentEvents(events)
                .build();
    }

    @Override
    public com.noncore.assessment.dto.response.TeacherStudentAlertsResponse getStudentAlerts(Long teacherId, Long studentId) {
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
            java.util.Map<String, Object> evidence = new java.util.HashMap<>();
            evidence.put("daysInactive", java.time.Duration.between(maxAccess, java.time.LocalDateTime.now()).toDays());
            java.util.List<String> actions = java.util.Arrays.asList("send_reminder", "suggest_practice");
            alerts.add(TeacherStudentAlertsResponse.AlertItem.builder()
                    .code("INACTIVE_14D")
                    .message("近14天未活跃")
                    .severity("warn")
                    .evidence(evidence)
                    .actions(actions)
                    .build());
        }

        // 低平均分告警（平均分 < 60）
        java.math.BigDecimal avg = gradeMapper.calculateAverageScore(studentId);
        if (avg != null && avg.doubleValue() < 60.0) {
            java.util.Map<String, Object> evidence = new java.util.HashMap<>();
            evidence.put("avgScore", avg);
            java.util.List<String> actions = java.util.Arrays.asList("schedule_meeting", "suggest_practice");
            alerts.add(TeacherStudentAlertsResponse.AlertItem.builder()
                    .code("LOW_SCORE")
                    .message("平均成绩偏低")
                    .severity("warn")
                    .evidence(evidence)
                    .actions(actions)
                    .build());
        }

        // 完成率偏低告警（总体完成率 < 50%）
        java.math.BigDecimal overallProgress = lessonProgressMapper.calculateOverallProgress(studentId);
        if (overallProgress != null && overallProgress.doubleValue() < 50.0) {
            java.util.Map<String, Object> evidence = new java.util.HashMap<>();
            evidence.put("completionRate", overallProgress);
            java.util.List<String> actions = java.util.Arrays.asList("send_reminder");
            alerts.add(TeacherStudentAlertsResponse.AlertItem.builder()
                    .code("LOW_PROGRESS")
                    .message("学习完成率偏低")
                    .severity("info")
                    .evidence(evidence)
                    .actions(actions)
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
        // 1) 取库内高优先级建议
        java.math.BigDecimal minPriority = java.math.BigDecimal.valueOf(0.5);
        int max = (limit == null || limit <= 0) ? 6 : limit;
        java.util.List<LearningRecommendation> list = new java.util.ArrayList<>(
                learningRecommendationMapper.selectByPriority(studentId, minPriority, max)
        );

        // 2) 基于能力雷达即时生成建议（不落库）：选择与当前教师课程有交集的课程作为上下文
        Long courseIdCtx = null;
        if (courses != null) {
            for (Course c : courses) {
                if (c != null && c.getTeacherId() != null && c.getTeacherId().equals(teacherId)) {
                    courseIdCtx = c.getId();
                    break;
                }
            }
            if (courseIdCtx == null && !courses.isEmpty()) {
                courseIdCtx = courses.get(0).getId();
            }
        }
        try {
            if (courseIdCtx != null) {
                java.time.LocalDate end = java.time.LocalDate.now();
                java.time.LocalDate start = end.minusDays(29);
                AbilityRadarQuery q = new AbilityRadarQuery();
                q.setCourseId(courseIdCtx);
                q.setStartDate(start);
                q.setEndDate(end);
                AbilityRadarResponse radar = abilityAnalyticsService.getRadarForStudent(q, studentId);
                if (radar != null && radar.getDimensions() != null) {
                    java.util.List<String> dims = radar.getDimensions();
                    java.util.List<java.lang.Double> stu = radar.getStudentScores();
                    java.util.List<java.lang.Double> cls = radar.getClassAvgScores();
                    java.util.List<LearningRecommendation> generated = new java.util.ArrayList<>();
                    for (int i = 0; i < dims.size(); i++) {
                        String dim = dims.get(i);
                        double s = (stu != null && i < stu.size() && stu.get(i) != null) ? stu.get(i) : 0.0;
                        double cavg = (cls != null && i < cls.size() && cls.get(i) != null) ? cls.get(i) : 0.0;
                        double gap = cavg - s; // 正数表示低于班级
                        if (gap > 0) {
                            java.math.BigDecimal priority = java.math.BigDecimal.valueOf(Math.min(1.0, Math.max(0.1, gap / 100.0 * 2))); // 0.1~1.0 随差距线性放大
                            String title = dim + " 提升建议";
                            String desc = "该维度低于班级均值 " + String.format(java.util.Locale.ROOT, "%.1f", gap) + " 分，建议进行针对性训练。";
                            String difficulty = s >= 70 ? "intermediate" : (s >= 40 ? "beginner" : "beginner");
                            String eta = s >= 70 ? "1-2小时" : "2-3小时";
                            LearningRecommendation rec = LearningRecommendation.builder()
                                    .id(null)
                                    .studentId(studentId)
                                    .dimensionId(null)
                                    .title(title)
                                    .description(desc)
                                    .recommendationType("practice")
                                    .resourceUrl(null)
                                    .difficultyLevel(difficulty)
                                    .estimatedTime(eta)
                                    .priorityScore(priority)
                                    .isRead(false)
                                    .isAccepted(false)
                                    .createdAt(java.time.LocalDateTime.now())
                                    .expiresAt(null)
                                    .dimensionName(dim)
                                    .dimensionCategory(null)
                                    .studentName(null)
                                    .build();
                            generated.add(rec);
                        }
                    }
                    // 取差距最大的前2-3条
                    generated.sort((a, b) -> b.getPriorityScore().compareTo(a.getPriorityScore()));
                    int remain = Math.max(0, max - list.size());
                    if (remain > 0) {
                        list.addAll(generated.subList(0, Math.min(remain, generated.size())));
                    }

                    // 若仍为空（无任何维度低于班级或雷达差距过小），按学生得分最低的维度兜底生成 1-2 条建议
                    if ((list == null || list.isEmpty()) && dims != null && !dims.isEmpty()) {
                        java.util.List<int[]> order = new java.util.ArrayList<>();
                        for (int i = 0; i < dims.size(); i++) {
                            double sv = (stu != null && i < stu.size() && stu.get(i) != null) ? stu.get(i) : 0.0;
                            order.add(new int[]{ i, (int)Math.round(sv) });
                        }
                        order.sort((x, y) -> Integer.compare(x[1], y[1])); // 分数低的在前
                        int remain2 = Math.max(1, Math.min(2, max - list.size()));
                        for (int k = 0; k < Math.min(remain2, order.size()); k++) {
                            int idx = order.get(k)[0];
                            String dim = dims.get(idx);
                            double sVal = (stu != null && idx < stu.size() && stu.get(idx) != null) ? stu.get(idx) : 0.0;
                            double cavgVal = (cls != null && idx < cls.size() && cls.get(idx) != null) ? cls.get(idx) : 0.0;
                            java.math.BigDecimal priority2 = java.math.BigDecimal.valueOf(Math.min(1.0, Math.max(0.2, (100.0 - sVal) / 100.0)));
                            String title2 = dim + " 巩固建议";
                            String desc2 = "当前分数 " + String.format(java.util.Locale.ROOT, "%.1f", sVal)
                                    + "，班级均值 " + String.format(java.util.Locale.ROOT, "%.1f", cavgVal) + "。建议继续巩固，保持稳步提升。";
                            String difficulty2 = sVal >= 70 ? "intermediate" : "beginner";
                            String eta2 = sVal >= 70 ? "1-2小时" : "2-3小时";
                            LearningRecommendation rec2 = LearningRecommendation.builder()
                                    .id(null)
                                    .studentId(studentId)
                                    .dimensionId(null)
                                    .title(title2)
                                    .description(desc2)
                                    .recommendationType("practice")
                                    .resourceUrl(null)
                                    .difficultyLevel(difficulty2)
                                    .estimatedTime(eta2)
                                    .priorityScore(priority2)
                                    .isRead(false)
                                    .isAccepted(false)
                                    .createdAt(java.time.LocalDateTime.now())
                                    .expiresAt(null)
                                    .dimensionName(dim)
                                    .dimensionCategory(null)
                                    .studentName(null)
                                    .build();
                            list.add(rec2);
                        }
                    }
                }
            }
        } catch (Exception ignore) {}

        return list;
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


