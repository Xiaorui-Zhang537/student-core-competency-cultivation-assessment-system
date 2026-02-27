package com.noncore.assessment.service.impl;

import com.noncore.assessment.dto.request.AbilityRadarQuery;
import com.noncore.assessment.dto.request.UpdateAbilityWeightsRequest;
import com.noncore.assessment.dto.request.AbilityCompareQuery;
import com.noncore.assessment.dto.response.AbilityRadarResponse;
import com.noncore.assessment.dto.response.AbilityWeightsResponse;
import com.noncore.assessment.dto.response.AbilityCompareResponse;
import com.noncore.assessment.dto.response.AbilityDimensionInsightsResponse;
import com.noncore.assessment.entity.AbilityReport;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.AbilityAnalyticsMapper;
import com.noncore.assessment.mapper.AbilityReportMapper;
import com.noncore.assessment.mapper.CourseMapper;
import com.noncore.assessment.mapper.EnrollmentMapper;
import com.noncore.assessment.mapper.UserMapper;
import com.noncore.assessment.service.AbilityAnalyticsService;
import com.noncore.assessment.service.CacheService;
import com.noncore.assessment.service.AbilityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class AbilityAnalyticsServiceImpl implements AbilityAnalyticsService {

    private static final List<String> DIMENSIONS = List.of(
            "道德认知", "学习态度", "学习能力", "学习方法", "学习成绩"
    );

    private static final Map<String, String> CODE_TO_NAME = Map.of(
            "MORAL_COGNITION", "道德认知",
            "LEARNING_ATTITUDE", "学习态度",
            "LEARNING_ABILITY", "学习能力",
            "LEARNING_METHOD", "学习方法",
            "ACADEMIC_GRADE", "学习成绩"
    );

    private final AbilityAnalyticsMapper analyticsMapper;
    private final CourseMapper courseMapper;
    private final CacheService cacheService;
    private final AbilityService abilityService;
    private final EnrollmentMapper enrollmentMapper;
    private final AbilityReportMapper abilityReportMapper;
    private final UserMapper userMapper;

    public AbilityAnalyticsServiceImpl(AbilityAnalyticsMapper analyticsMapper,
                                       CourseMapper courseMapper,
                                       CacheService cacheService,
                                       EnrollmentMapper enrollmentMapper,
                                       AbilityService abilityService,
                                       AbilityReportMapper abilityReportMapper,
                                       UserMapper userMapper) {
        this.analyticsMapper = analyticsMapper;
        this.courseMapper = courseMapper;
        this.cacheService = cacheService;
        this.enrollmentMapper = enrollmentMapper;
        this.abilityService = abilityService;
        this.abilityReportMapper = abilityReportMapper;
        this.userMapper = userMapper;
    }

    /**
     * 判断是否为管理员用户。
     *
     * <p>说明：教师端能力分析接口在 Controller 层可能允许 ADMIN 调用，
     * 但原先的 teacherId==course.teacherId 校验会拦截管理员。
     * 这里通过用户角色进行旁路处理，确保管理员能全局审计任意课程数据。</p>
     */
    private boolean isAdminUser(Long userId) {
        if (userId == null) return false;
        try {
            User u = userMapper.selectUserById(userId);
            return u != null && "admin".equalsIgnoreCase(u.getRole());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public AbilityRadarResponse getRadar(AbilityRadarQuery query, Long teacherId) {
        Course course = courseMapper.selectCourseById(query.getCourseId());
        boolean isAdmin = isAdminUser(teacherId);
        if (course == null || (!isAdmin && !Objects.equals(course.getTeacherId(), teacherId))) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        // 允许日期为空：为空则不做时间过滤
        validateOptionalDates(query.getStartDate(), query.getEndDate());
        String cacheKey = buildCacheKey("radar", teacherId, query);
        Optional<AbilityRadarResponse> cached = cacheService.get(cacheKey, AbilityRadarResponse.class);
        if (cached.isPresent()) {
            AbilityRadarResponse resp = cached.get();
            boolean classOk = resp.getClassAvgScores() != null && !isNonGradeAllZero(resp.getClassAvgScores());
            boolean studentRequired = query.getStudentId() != null;
            boolean studentOk = !studentRequired || (resp.getStudentScores() != null && !isNonGradeAllZero(resp.getStudentScores()));
            if (classOk && studentOk) {
                return resp;
            }
        }

        LocalDateTime start = query.getStartDate() == null ? null : query.getStartDate().atStartOfDay();
        LocalDateTime end = query.getEndDate() == null ? null : query.getEndDate().atTime(LocalTime.MAX);

        // 班级或课程均值
        Map<String, Double> classAvg = aggregateDimensions(null, query.getCourseId(), query.getClassId(), start, end);
        enrichClassScores(classAvg, query.getCourseId());

        // 学生成绩（若传 studentId）
        Map<String, Double> student = query.getStudentId() == null
                ? new HashMap<>()
                : aggregateDimensions(query.getStudentId(), query.getCourseId(), query.getClassId(), start, end);
        if (query.getStudentId() != null) {
            enrichStudentScores(student, query.getStudentId(), query.getCourseId(), query.getClassId());
        }

        // 成绩维度（课程/班级/学生）
        double classGrade = normalizeGradeAvg(query.getCourseId(), query.getClassId(), null, start, end);
        double studentGrade = query.getStudentId() == null ? 0.0 : normalizeGradeAvg(query.getCourseId(), query.getClassId(), query.getStudentId(), start, end);

        // 组装五维，顺序固定
        List<Double> classScores = new ArrayList<>();
        List<Double> studentScores = new ArrayList<>();
        for (String name : DIMENSIONS) {
            if ("学习成绩".equals(name)) {
                classScores.add(classGrade);
                studentScores.add(studentGrade);
            } else {
                classScores.add(classAvg.getOrDefault(name, 0.0));
                studentScores.add(student.getOrDefault(name, 0.0));
            }
        }

        // 取消兜底：教师端总体雷达不再用“任一作业的最近AI报告”填充，避免不同作业报告混淆

        List<Double> deltas = new ArrayList<>();
        for (int i = 0; i < DIMENSIONS.size(); i++) {
            deltas.add(round(studentScores.get(i) - classScores.get(i)));
        }

        Map<String, Double> weights = loadWeightsOrDefault(query.getCourseId());
        double studentComposite = weightedComposite(studentScores, weights);
        double classComposite = weightedComposite(classScores, weights);

        // 上一对等周期
        AbilityRadarResponse.PreviousPeriod prev = (query.getStartDate() == null || query.getEndDate() == null) ? null : buildPreviousPeriod(query);

        // 弱项：<60 或 delta<-10
        List<String> weak = new ArrayList<>();
        for (int i = 0; i < DIMENSIONS.size(); i++) {
            double s = studentScores.get(i);
            double d = deltas.get(i);
            if (s < 60 || d < -10) {
                weak.add(DIMENSIONS.get(i));
            }
        }

        AbilityRadarResponse resp = AbilityRadarResponse.builder()
                .dimensions(DIMENSIONS)
                .studentScores(studentScores)
                .classAvgScores(classScores)
                .deltas(deltas)
                .weights(weights)
                .studentComposite(round(studentComposite))
                .classComposite(round(classComposite))
                .compositeDelta(round(studentComposite - classComposite))
                .prevPeriod(prev)
                .weakDimensions(weak)
                .build();

        cacheService.set(cacheKey, resp, 600);
        return resp;
    }

    // ==== Student-facing variants ====
    @Override
    public AbilityRadarResponse getRadarForStudent(AbilityRadarQuery query, Long studentId) {
        // 学生态不校验课程归属教师，但要求 studentId 不为空
        Objects.requireNonNull(studentId, "studentId required");
        validateOptionalDates(query.getStartDate(), query.getEndDate());

        LocalDateTime start = query.getStartDate() == null ? null : query.getStartDate().atStartOfDay();
        LocalDateTime end = query.getEndDate() == null ? null : query.getEndDate().atTime(LocalTime.MAX);

        String studentCacheKey = buildCacheKey("student_radar", studentId, query);
        Optional<AbilityRadarResponse> studentCached = cacheService.get(studentCacheKey, AbilityRadarResponse.class);
        if (studentCached.isPresent()) {
            AbilityRadarResponse resp = studentCached.get();
            boolean classOk = resp.getClassAvgScores() != null && !isNonGradeAllZero(resp.getClassAvgScores());
            boolean studentOk = resp.getStudentScores() != null && !isNonGradeAllZero(resp.getStudentScores());
            if (classOk && studentOk) {
                return resp;
            }
        }

        Map<String, Double> classAvg = aggregateDimensions(null, query.getCourseId(), query.getClassId(), start, end);
        enrichClassScores(classAvg, query.getCourseId());

        Map<String, Double> student = aggregateDimensions(studentId, query.getCourseId(), query.getClassId(), start, end);
        enrichStudentScores(student, studentId, query.getCourseId(), query.getClassId());
        double classGrade = normalizeGradeAvg(query.getCourseId(), query.getClassId(), null, start, end);
        double studentGrade = normalizeGradeAvg(query.getCourseId(), query.getClassId(), studentId, start, end);

        List<Double> classScores = new ArrayList<>();
        List<Double> studentScores = new ArrayList<>();
        for (String name : DIMENSIONS) {
            if ("学习成绩".equals(name)) {
                classScores.add(classGrade);
                studentScores.add(studentGrade);
            } else {
                classScores.add(classAvg.getOrDefault(name, 0.0));
                studentScores.add(student.getOrDefault(name, 0.0));
            }
        }

        // 取消兜底：学生端总体雷达同样不使用“最近AI报告”填充
        List<Double> deltas = new ArrayList<>();
        for (int i = 0; i < DIMENSIONS.size(); i++) {
            deltas.add(round(studentScores.get(i) - classScores.get(i)));
        }
        Map<String, Double> weights = loadWeightsOrDefault(query.getCourseId());
        double studentComposite = round(weightedComposite(studentScores, weights));
        double classComposite = round(weightedComposite(classScores, weights));

        AbilityRadarResponse.PreviousPeriod prev = (query.getStartDate() == null || query.getEndDate() == null) ? null : buildPreviousPeriod(query);

        List<String> weak = new ArrayList<>();
        for (int i = 0; i < DIMENSIONS.size(); i++) {
            double s = studentScores.get(i);
            double d = deltas.get(i);
            if (s < 60 || d < -10) {
                weak.add(DIMENSIONS.get(i));
            }
        }

        AbilityRadarResponse resp = AbilityRadarResponse.builder()
                .dimensions(DIMENSIONS)
                .studentScores(studentScores)
                .classAvgScores(classScores)
                .deltas(deltas)
                .weights(weights)
                .studentComposite(studentComposite)
                .classComposite(classComposite)
                .compositeDelta(round(studentComposite - classComposite))
                .prevPeriod(prev)
                .weakDimensions(weak)
                .build();

        cacheService.set(studentCacheKey, resp, 600);
        return resp;
    }

    @Override
    public AbilityCompareResponse getRadarCompareForStudent(AbilityCompareQuery query, Long studentId) {
        Objects.requireNonNull(studentId, "studentId required");
        query.setStudentId(studentId);
        return getRadarCompare(query, null);
    }

    @Override
    public AbilityDimensionInsightsResponse getDimensionInsightsForStudent(AbilityCompareQuery query, Long studentId) {
        Objects.requireNonNull(studentId, "studentId required");
        query.setStudentId(studentId);
        return getDimensionInsights(query, null);
    }

    @Override
    public AbilityWeightsResponse getWeights(Long courseId, Long teacherId) {
        Course course = courseMapper.selectCourseById(courseId);
        boolean isAdmin = isAdminUser(teacherId);
        if (course == null || (!isAdmin && !Objects.equals(course.getTeacherId(), teacherId))) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }
        Map<String, Double> weights = loadWeightsOrDefault(courseId);
        return AbilityWeightsResponse.builder()
                .courseId(courseId)
                .weights(weights)
                .updatedAt(java.time.LocalDateTime.now())
                .build();
    }

    @Override
    public AbilityCompareResponse getRadarCompare(AbilityCompareQuery query, Long teacherId) {
        Objects.requireNonNull(query.getCourseId(), "courseId required");
        Objects.requireNonNull(query.getStudentId(), "studentId required");

        Course course = courseMapper.selectCourseById(query.getCourseId());
        if (course == null) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }
        // 教师侧：必须为课程任课老师；学生侧：必须为该课程已报名学生
        if (teacherId != null) {
            boolean isAdmin = isAdminUser(teacherId);
            if (!isAdmin && !Objects.equals(course.getTeacherId(), teacherId)) {
                throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
            }
        } else {
            // student context
            int exists = enrollmentMapper.checkEnrollmentExists(query.getStudentId(), query.getCourseId());
            if (exists <= 0) {
                throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
            }
        }

        // 校验日期跨度与区间合法性（允许为空；若存在则<=365天，end>=start）
        // 仅作业对比时可不校验日期
        if (query.getAssignmentIdsA() == null && query.getAssignmentIdsB() == null) {
            validateOptionalDates(query.getStartDateA(), query.getEndDateA());
            validateOptionalDates(query.getStartDateB(), query.getEndDateB());
        }

        String include = (query.getIncludeClassAvg() == null || query.getIncludeClassAvg().isBlank()) ? "both" : query.getIncludeClassAvg();

        // 计算A
        Aggregated aggA = aggregateForPeriod(query.getStudentId(), query.getCourseId(), query.getClassId(),
                query.getStartDateA(), query.getEndDateA(), query.getAssignmentIdsA());
        // 计算B
        Aggregated aggB = aggregateForPeriod(query.getStudentId(), query.getCourseId(), query.getClassId(),
                query.getStartDateB(), query.getEndDateB(), query.getAssignmentIdsB());

        Map<String, Double> weights = loadWeightsOrDefault(query.getCourseId());

        List<Double> deltasAB = new ArrayList<>();
        for (int i = 0; i < DIMENSIONS.size(); i++) {
            double a = nz(aggA.studentScores.get(i));
            double b = nz(aggB.studentScores.get(i));
            deltasAB.add(round(a - b));
        }

        double compA = round(weightedComposite(aggA.studentScores, weights));
        double compB = round(weightedComposite(aggB.studentScores, weights));

        AbilityCompareResponse resp = AbilityCompareResponse.builder()
                .dimensions(DIMENSIONS)
                .seriesA(AbilityCompareResponse.Series.builder()
                        .studentScores(aggA.studentScores)
                        .classAvgScores(("A".equalsIgnoreCase(include) || "both".equalsIgnoreCase(include)) ? aggA.classScores : null)
                        .studentComposite(compA)
                        .classComposite(("A".equalsIgnoreCase(include) || "both".equalsIgnoreCase(include)) ? round(weightedComposite(aggA.classScores, weights)) : null)
                        .build())
                .seriesB(AbilityCompareResponse.Series.builder()
                        .studentScores(aggB.studentScores)
                        .classAvgScores(("B".equalsIgnoreCase(include) || "both".equalsIgnoreCase(include)) ? aggB.classScores : null)
                        .studentComposite(compB)
                        .classComposite(("B".equalsIgnoreCase(include) || "both".equalsIgnoreCase(include)) ? round(weightedComposite(aggB.classScores, weights)) : null)
                        .build())
                .deltasAB(deltasAB)
                .compositeDeltaAB(round(compA - compB))
                .weights(weights)
                .meta(AbilityCompareResponse.Meta.builder()
                        .includeClassAvg(include)
                        .periodA(AbilityCompareResponse.Period.builder()
                                .startDate(query.getStartDateA() == null ? null : query.getStartDateA().toString())
                                .endDate(query.getEndDateA() == null ? null : query.getEndDateA().toString())
                                .assignmentIds(query.getAssignmentIdsA())
                                .build())
                        .periodB(AbilityCompareResponse.Period.builder()
                                .startDate(query.getStartDateB() == null ? null : query.getStartDateB().toString())
                                .endDate(query.getEndDateB() == null ? null : query.getEndDateB().toString())
                                .assignmentIds(query.getAssignmentIdsB())
                                .build())
                        .build())
                .build();

        return resp;
    }

    @Override
    public AbilityDimensionInsightsResponse getDimensionInsights(AbilityCompareQuery query, Long teacherId) {
        // 若未开启对比：允许只传 A 作业集合（或留空），后端用 A 对自己对比以便生成解释，但不显示 delta
        if ((query.getAssignmentIdsB() == null || query.getAssignmentIdsB().isEmpty()) &&
                (query.getAssignmentIdsA() != null && !query.getAssignmentIdsA().isEmpty())) {
            query.setAssignmentIdsB(new ArrayList<>(query.getAssignmentIdsA()));
        }
        AbilityCompareResponse cmp = getRadarCompare(query, teacherId);
        List<AbilityDimensionInsightsResponse.Item> items = new ArrayList<>();
        for (int i = 0; i < DIMENSIONS.size(); i++) {
            String name = DIMENSIONS.get(i);
            String code = nameToCode(name);
            Double a = safeIndex(cmp.getSeriesA().getStudentScores(), i);
            Double b = safeIndex(cmp.getSeriesB().getStudentScores(), i);
            Double delta = (a == null || b == null || Objects.equals(a, b)) ? null : round(a - b);
            Double w = cmp.getWeights() == null ? null : cmp.getWeights().get(code);
            String analysis = buildBasicAnalysis(a, b, delta, name);
            String suggestion = buildBasicSuggestion(a, b, delta, name);
            items.add(AbilityDimensionInsightsResponse.Item.builder()
                    .code(code).name(name).scoreA(a).scoreB(b).delta(delta).weight(w).analysis(analysis).suggestion(suggestion)
                    .build());
        }
        return AbilityDimensionInsightsResponse.builder().items(items).build();
    }

    private String buildBasicAnalysis(Double a, Double b, Double delta, String name) {
        double aa = a == null ? 0.0 : a;
        double bb = b == null ? 0.0 : b;
        double dd = delta == null ? aa - bb : delta;
        String trend = dd > 2 ? "上升" : (dd < -2 ? "下降" : "基本持平");
        return String.format("%s维度本期得分%.1f，对比期%.1f，变化为%.1f（%s）", name, aa, bb, dd, trend);
    }

    private String buildBasicSuggestion(Double a, Double b, Double delta, String name) {
        double aa = a == null ? 0.0 : a;
        if (aa < 60) {
            return String.format("%s偏弱，建议针对性训练与及时反馈。", name);
        }
        if (delta != null && delta < -5) {
            return String.format("%s较上期下降明显，建议复盘原因并调整学习策略。", name);
        }
        return String.format("%s保持良好趋势，建议继续巩固并拓展。", name);
    }

    private record Aggregated(List<Double> studentScores, List<Double> classScores) {}

    private Aggregated aggregateForPeriod(Long studentId, Long courseId, Long classId,
                                          java.time.LocalDate sd, java.time.LocalDate ed,
                                          java.util.List<Long> assignmentIds) {
        java.time.LocalDateTime start = (sd == null) ? null : sd.atStartOfDay();
        java.time.LocalDateTime end = (ed == null) ? null : ed.atTime(java.time.LocalTime.MAX);

        Map<String, Double> classAvg = aggregateDimensionsWithAssignments(null, courseId, classId, start, end, assignmentIds);
        Map<String, Double> student = aggregateDimensionsWithAssignments(studentId, courseId, classId, start, end, assignmentIds);
        enrichClassScores(classAvg, courseId);
        if (studentId != null) {
            enrichStudentScores(student, studentId, courseId, classId);
        }
        double classGrade = normalizeGradeAvgWithAssignments(courseId, classId, null, start, end, assignmentIds);
        double studentGrade = normalizeGradeAvgWithAssignments(courseId, classId, studentId, start, end, assignmentIds);

        List<Double> classScores = new ArrayList<>();
        List<Double> studentScores = new ArrayList<>();
        for (String name : DIMENSIONS) {
            if ("学习成绩".equals(name)) {
                classScores.add(classGrade);
                studentScores.add(studentGrade);
            } else {
                classScores.add(classAvg.getOrDefault(name, 0.0));
                studentScores.add(student.getOrDefault(name, 0.0));
            }
        }
        return new Aggregated(studentScores, classScores);
    }

    private Map<String, Double> aggregateDimensionsWithAssignments(Long studentId, Long courseId, Long classId,
                                                                   java.time.LocalDateTime start, java.time.LocalDateTime end,
                                                                   java.util.List<Long> assignmentIds) {
        List<Map<String, Object>> rows = (studentId == null)
                ? analyticsMapper.selectClassOrCourseDimensionAvgWithAssignments(courseId, classId, start, end, assignmentIds)
                : analyticsMapper.selectStudentDimensionAvgWithAssignments(studentId, courseId, classId, start, end, assignmentIds);
        Map<String, Double> base = reduceDimensionRows(rows, studentId == null);
        Map<String, Double> reportAvg = aggregateDimensionsFromReports(studentId, courseId, classId, start, end, assignmentIds);
        overrideDimensionScores(base, reportAvg);
        return base;
    }

    private double normalizeGradeAvgWithAssignments(Long courseId, Long classId, Long studentId,
                                                    java.time.LocalDateTime start, java.time.LocalDateTime end,
                                                    java.util.List<Long> assignmentIds) {
        Map<String, Object> row = analyticsMapper.selectCourseGradeAvgWithAssignments(courseId, classId, studentId, start, end, assignmentIds);
        if (row == null) return 0.0;
        Double ratio = toDouble(row.get("avgRatio"));
        if (ratio == null) {
            return 0.0;
        }
        return round(ratio * 100.0);
    }

    private Map<String, Double> reduceDimensionRows(List<Map<String, Object>> rows) {
        return reduceDimensionRows(rows, false);
    }

    private Map<String, Double> reduceDimensionRows(List<Map<String, Object>> rows, boolean equalWeightPerStudent) {
        Map<String, Map<Object, Map<Object, ScoreEntry>>> latestTree = new HashMap<>();
        Map<String, List<Double>> fallbackBucket = new HashMap<>();

        if (rows != null) {
            for (Map<String, Object> r : rows) {
                if (r == null) continue;
                Object nameObj = r.get("dimensionName");
                if (nameObj == null) continue;
                String name = String.valueOf(nameObj);
                if (name.isBlank()) continue;

                Double score = toDouble(r.get("score"));
                Double max = toDouble(r.get("maxScore"));
                if (score == null) continue;
                double denom = (max == null || max <= 0) ? (score <= 5.0 ? 5.0 : 100.0) : max;
                if (denom <= 0) continue;
                double pct = (score / denom) * 100.0;

                Object studentKey = r.get("studentId");
                Object relatedKey = r.get("relatedId");
                LocalDateTime assessedAt = toDateTime(r.get("assessedAt"));

                if (studentKey != null && relatedKey != null) {
                    Map<Object, Map<Object, ScoreEntry>> perStudent = latestTree.computeIfAbsent(name, k -> new HashMap<>());
                    Map<Object, ScoreEntry> perAssignment = perStudent.computeIfAbsent(studentKey, k -> new HashMap<>());
                    ScoreEntry existing = perAssignment.get(relatedKey);
                    if (shouldReplace(existing, assessedAt)) {
                        perAssignment.put(relatedKey, new ScoreEntry(pct, assessedAt));
                    }
                } else {
                    fallbackBucket.computeIfAbsent(name, k -> new ArrayList<>()).add(pct);
            }
        }
        }

        Map<String, Double> out = new HashMap<>();

        for (Map.Entry<String, Map<Object, Map<Object, ScoreEntry>>> entry : latestTree.entrySet()) {
            Map<Object, Map<Object, ScoreEntry>> perStudent = entry.getValue();
            if (perStudent == null || perStudent.isEmpty()) continue;

            List<Double> studentAverages = new ArrayList<>();
            for (Map<Object, ScoreEntry> perAssignment : perStudent.values()) {
                if (perAssignment == null || perAssignment.isEmpty()) continue;
                double sum = 0.0;
                int count = 0;
                for (ScoreEntry se : perAssignment.values()) {
                    if (se == null || se.value <= 0.0) continue;
                    sum += se.value;
                    count++;
                }
                if (count > 0) {
                    studentAverages.add(sum / count);
                }
            }
            if (studentAverages.isEmpty()) continue;

            double finalAvg = studentAverages.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            if (finalAvg > 0.0) {
                out.put(entry.getKey(), round(finalAvg));
            }
        }

        for (Map.Entry<String, List<Double>> entry : fallbackBucket.entrySet()) {
            if (out.containsKey(entry.getKey())) continue;
            List<Double> list = entry.getValue();
            if (list == null || list.isEmpty()) continue;
            double sum = 0.0;
            int count = 0;
            for (Double v : list) {
                if (v == null) continue;
                sum += v;
                count++;
            }
            if (count > 0) {
                out.put(entry.getKey(), round(sum / count));
            }
        }

        return out;
    }

    private void enrichClassScores(Map<String, Double> classScores, Long courseId) {
        if (classScores == null || courseId == null) {
            return;
        }
        if (!needsNonGradeSupplement(classScores)) {
            return;
        }
        List<Map<String, Object>> rows = analyticsMapper.selectClassAbilitySnapshot(courseId);
        Map<String, Double> snapshot = reduceDimensionRows(rows);
        mergeDimensionScores(classScores, snapshot);
    }

    private void enrichStudentScores(Map<String, Double> studentScores, Long studentId, Long courseId, Long classId) {
        if (studentScores == null || studentId == null) {
            return;
        }
        if (!needsNonGradeSupplement(studentScores)) {
            return;
        }
        if (courseId != null) {
            List<Map<String, Object>> rows = analyticsMapper.selectStudentAbilitySnapshot(studentId, courseId);
            Map<String, Double> snapshot = reduceDimensionRows(rows);
            mergeDimensionScores(studentScores, snapshot);
        }
        if (!needsNonGradeSupplement(studentScores)) {
            return;
        }
        Map<String, Double> latestReport = loadFourDimsFromLatestReport(studentId, courseId, null, null);
        if (latestReport == null || latestReport.isEmpty()) {
            return;
        }
        Map<String, Double> converted = new HashMap<>();
        for (Map.Entry<String, Double> entry : latestReport.entrySet()) {
            String name = CODE_TO_NAME.get(entry.getKey());
            if (name == null || "学习成绩".equals(name)) continue;
            Double val = entry.getValue();
            if (val == null || val <= 0.0) continue;
            converted.put(name, val);
        }
        mergeDimensionScores(studentScores, converted);
    }

    private void mergeDimensionScores(Map<String, Double> target, Map<String, Double> source) {
        if (target == null || source == null || source.isEmpty()) {
            return;
        }
        for (String name : DIMENSIONS) {
            if ("学习成绩".equals(name)) continue;
            Double incoming = source.get(name);
            if (incoming == null || incoming <= 0.0) continue;
            Double existing = target.get(name);
            if (existing == null || existing <= 0.0) {
                target.put(name, round(incoming));
            }
        }
    }

    private void overrideDimensionScores(Map<String, Double> target, Map<String, Double> source) {
        if (target == null || source == null || source.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Double> entry : source.entrySet()) {
            String name = entry.getKey();
            if (name == null || "学习成绩".equals(name)) {
                continue;
            }
            Double incoming = entry.getValue();
            if (incoming == null || incoming <= 0.0) {
                continue;
            }
            target.put(name, round(incoming));
        }
    }

    private Map<String, Double> aggregateDimensionsFromReports(Long studentId, Long courseId, Long classId,
                                                               LocalDateTime start, LocalDateTime end,
                                                               List<Long> assignmentIds) {
        if (courseId == null) {
            return Collections.emptyMap();
        }
        List<AbilityReport> reports = abilityReportMapper.selectReportsForRadar(studentId, courseId, start, end, assignmentIds);
        if (reports == null || reports.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<Double>> bucket = new HashMap<>();
        for (AbilityReport report : reports) {
            Map<String, Double> extracted = extractDimensionPercentages(report.getTrendsAnalysis());
            if (extracted.isEmpty()) {
                extracted = extractDimensionPercentages(report.getDimensionScores());
            }
            if (extracted.isEmpty()) {
                continue;
            }
            for (Map.Entry<String, Double> entry : extracted.entrySet()) {
                String code = entry.getKey();
                Double percent = entry.getValue();
                if (code == null || percent == null || percent <= 0.0) {
                    continue;
                }
                bucket.computeIfAbsent(code, k -> new ArrayList<>()).add(percent);
            }
        }
        Map<String, Double> out = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : bucket.entrySet()) {
            List<Double> values = entry.getValue();
            if (values == null || values.isEmpty()) {
                continue;
            }
            double sum = 0.0;
            int count = 0;
            for (Double v : values) {
                if (v == null || v <= 0.0) {
                    continue;
                }
                sum += v;
                count++;
            }
            if (count == 0) {
                continue;
            }
            double avg = sum / count;
            if (avg <= 0.0) {
                continue;
            }
            String name = CODE_TO_NAME.get(entry.getKey());
            if (name == null || "学习成绩".equals(name)) {
                continue;
            }
            out.put(name, round(avg));
        }
        return out;
    }

    private boolean shouldReplace(ScoreEntry existing, LocalDateTime assessedAt) {
        if (existing == null) {
            return true;
        }
        if (assessedAt == null) {
            return existing.assessedAt == null;
        }
        if (existing.assessedAt == null) {
            return true;
        }
        return !assessedAt.isBefore(existing.assessedAt);
    }

    private LocalDateTime toDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime dt) {
            return dt;
        }
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime();
        }
        if (value instanceof java.util.Date date) {
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        }
        return null;
    }

    private static final class ScoreEntry {
        final double value;
        final LocalDateTime assessedAt;

        ScoreEntry(double value, LocalDateTime assessedAt) {
            this.value = value;
            this.assessedAt = assessedAt;
        }
    }

    private boolean needsNonGradeSupplement(Map<String, Double> scores) {
        if (scores == null || scores.isEmpty()) {
            return true;
        }
        for (String name : DIMENSIONS) {
            if ("学习成绩".equals(name)) continue;
            Double val = scores.get(name);
            if (val == null || val <= 0.0) {
                return true;
            }
        }
        return false;
    }

    private void validateOptionalDates(java.time.LocalDate start, java.time.LocalDate end) {
        // 放宽：任一为空则视为不启用时间过滤，不做校验
        if (start == null || end == null) return;
        // 仅当两者都存在时校验顺序
        if (end.isBefore(start)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }
    }

    private double nz(Double v) { return v == null ? 0.0 : v; }

    private Double safeIndex(List<Double> list, int i) {
        if (list == null || i < 0 || i >= list.size()) return null;
        return list.get(i);
    }

    @Override
    @Transactional
    public AbilityWeightsResponse updateWeights(UpdateAbilityWeightsRequest request, Long teacherId) {
        Course course = courseMapper.selectCourseById(request.getCourseId());
        boolean isAdmin = isAdminUser(teacherId);
        if (course == null || (!isAdmin && !Objects.equals(course.getTeacherId(), teacherId))) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        Map<String, Double> incoming = request.getWeights();
        if (incoming == null || incoming.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }

        for (Map.Entry<String, Double> e : incoming.entrySet()) {
            String code = e.getKey();
            Double w = e.getValue();
            if (!CODE_TO_NAME.containsKey(code)) {
                throw new BusinessException(ErrorCode.INVALID_PARAMETER);
            }
            if (w == null || w < 0 || w > 10) {
                throw new BusinessException(ErrorCode.INVALID_PARAMETER);
            }
            analyticsMapper.upsertCourseWeight(request.getCourseId(), code, w);
        }
        // 清缓存（简单按前缀）
        // 可以扩展为批量keys匹配，这里省略实现，依赖短TTL

        Map<String, Double> weights = loadWeightsOrDefault(request.getCourseId());
        return AbilityWeightsResponse.builder()
                .courseId(request.getCourseId())
                .weights(weights)
                .updatedAt(java.time.LocalDateTime.now())
                .build();
    }

    private AbilityRadarResponse.PreviousPeriod buildPreviousPeriod(AbilityRadarQuery query) {
        LocalDate startDate = query.getStartDate();
        LocalDate endDate = query.getEndDate();
        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDate prevEnd = startDate.minusDays(1);
        LocalDate prevStart = prevEnd.minusDays(days - 1);
        LocalDateTime pStart = prevStart.atStartOfDay();
        LocalDateTime pEnd = prevEnd.atTime(LocalTime.MAX);

        Map<String, Double> classAvgPrev = aggregateDimensions(null, query.getCourseId(), query.getClassId(), pStart, pEnd);
        Map<String, Double> studentPrev = query.getStudentId() == null ? Collections.emptyMap()
                : aggregateDimensions(query.getStudentId(), query.getCourseId(), query.getClassId(), pStart, pEnd);
        double classGradePrev = normalizeGradeAvg(query.getCourseId(), query.getClassId(), null, pStart, pEnd);
        double studentGradePrev = query.getStudentId() == null ? 0.0 : normalizeGradeAvg(query.getCourseId(), query.getClassId(), query.getStudentId(), pStart, pEnd);

        List<Double> classScoresPrev = new ArrayList<>();
        List<Double> studentScoresPrev = new ArrayList<>();
        for (String name : DIMENSIONS) {
            if ("学习成绩".equals(name)) {
                classScoresPrev.add(classGradePrev);
                studentScoresPrev.add(studentGradePrev);
            } else {
                classScoresPrev.add(classAvgPrev.getOrDefault(name, 0.0));
                studentScoresPrev.add(studentPrev.getOrDefault(name, 0.0));
            }
        }

        Map<String, Double> weights = loadWeightsOrDefault(query.getCourseId());
        return AbilityRadarResponse.PreviousPeriod.builder()
                .studentScores(studentScoresPrev)
                .classAvgScores(classScoresPrev)
                .studentComposite(round(weightedComposite(studentScoresPrev, weights)))
                .classComposite(round(weightedComposite(classScoresPrev, weights)))
                .build();
    }

    private Map<String, Double> aggregateDimensions(Long studentId, Long courseId, Long classId,
                                                    LocalDateTime start, LocalDateTime end) {
        List<Map<String, Object>> rows = (studentId == null)
                ? analyticsMapper.selectClassOrCourseDimensionAvg(courseId, classId, start, end)
                : analyticsMapper.selectStudentDimensionAvg(studentId, courseId, classId, start, end);
        Map<String, Double> base = reduceDimensionRows(rows, studentId == null);
        Map<String, Double> reportAvg = aggregateDimensionsFromReports(studentId, courseId, classId, start, end, null);
        overrideDimensionScores(base, reportAvg);
        return base;
    }

    private double normalizeGradeAvg(Long courseId, Long classId, Long studentId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> row = analyticsMapper.selectCourseGradeAvg(courseId, classId, studentId, start, end);
        if (row == null) return 0.0;
        Double ratio = toDouble(row.get("avgRatio"));
        if (ratio == null) {
            return 0.0;
        }
        return round(ratio * 100.0);
    }

    private double weightedComposite(List<Double> scores, Map<String, Double> weights) {
        double sum = 0.0;
        double wsum = 0.0;
        for (int i = 0; i < DIMENSIONS.size(); i++) {
            String code = nameToCode(DIMENSIONS.get(i));
            double w = weights.getOrDefault(code, 1.0);
            double s = scores.get(i) == null ? 0.0 : scores.get(i);
            sum += s * w;
            wsum += w;
        }
        return wsum <= 0 ? 0.0 : sum / wsum;
    }

    private Map<String, Double> loadWeightsOrDefault(Long courseId) {
        List<Map<String, Object>> list = analyticsMapper.selectCourseWeights(courseId);
        Map<String, Double> map = new HashMap<>();
        // 默认等权
        for (String code : CODE_TO_NAME.keySet()) {
            map.put(code, 1.0);
        }
        for (Map<String, Object> r : list) {
            String code = String.valueOf(r.get("dimensionCode"));
            Double w = toDouble(r.get("weight"));
            if (w != null) map.put(code, w);
        }
        return map;
    }

    private String buildCacheKey(String prefix, Long teacherId, AbilityRadarQuery q) {
        return String.format("%s:%d:%d:%s:%s:%s:%s", prefix, teacherId, q.getCourseId(),
                String.valueOf(q.getClassId()), String.valueOf(q.getStudentId()),
                q.getStartDate(), q.getEndDate());
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }
        long span = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        if (span > 180) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }
    }

    private Double toDouble(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return null; }
    }

    private String nameToCode(String name) {
        for (Map.Entry<String, String> e : CODE_TO_NAME.entrySet()) {
            if (Objects.equals(e.getValue(), name)) return e.getKey();
        }
        return name;
    }

    private double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private boolean isNonGradeAllZero(List<Double> scores) {
        if (scores == null || scores.size() != DIMENSIONS.size()) return true;
        for (int i = 0; i < DIMENSIONS.size(); i++) {
            if ("学习成绩".equals(DIMENSIONS.get(i))) continue;
            double s = scores.get(i) == null ? 0.0 : scores.get(i);
            if (s > 0.0) return false;
        }
        return true;
    }

    // 从最新 AI 报告读取四维（返回百分制0..100）。仅在学生四维均为0时作为显示兜底使用
    private Map<String, Double> loadFourDimsFromLatestReport(Long studentId, Long courseId, Long assignmentId, Long submissionId) {
        Map<String, Double> out = new HashMap<>();
        try {
            com.noncore.assessment.entity.AbilityReport rep = abilityService.getLatestAbilityReportByContext(studentId, courseId, assignmentId, submissionId);
            if (rep == null) {
                return out;
            }
            boolean merged = mergeReportDimensionJson(out, rep.getTrendsAnalysis());
            if (!merged) {
                mergeReportDimensionJson(out, rep.getDimensionScores());
            }
        } catch (Exception ignored) {}
        return out;
    }

    private boolean mergeReportDimensionJson(Map<String, Double> target, String rawJson) {
        Map<String, Double> extracted = extractDimensionPercentages(rawJson);
        if (extracted.isEmpty()) {
            return false;
        }
        boolean updated = false;
        for (Map.Entry<String, Double> entry : extracted.entrySet()) {
            Double value = entry.getValue();
            if (value == null || value <= 0) {
                continue;
            }
            double rounded = round(value);
            Double existing = target.get(entry.getKey());
            if (existing == null || rounded > existing) {
                target.put(entry.getKey(), rounded);
                updated = true;
            }
        }
        return updated;
    }

    private Map<String, Double> extractDimensionPercentages(String rawJson) {
        Map<String, Double> result = new HashMap<>();
        if (rawJson == null || rawJson.isBlank()) {
            return result;
        }
        try {
            Object parsed = new com.fasterxml.jackson.databind.ObjectMapper().readValue(rawJson, Object.class);
            Map<?, ?> dims = resolveDimensionMap(parsed);
            mergeDimensionEntries(result, dims);
        } catch (Exception ignored) {}
        return result;
    }

    private Map<?, ?> resolveDimensionMap(Object parsed) {
        if (!(parsed instanceof Map<?, ?> map)) {
            return null;
        }
        Object overall = map.get("overall");
        if (overall instanceof Map<?, ?> overallMap) {
            Object dm = overallMap.get("dimension_averages");
            if (dm instanceof Map<?, ?> dims) {
                return dims;
            }
        }
        Object direct = map.get("dimension_averages");
        if (direct instanceof Map<?, ?> dims) {
            return dims;
        }
        Object camel = map.get("dimensionAverages");
        if (camel instanceof Map<?, ?> dims) {
            return dims;
        }
        Object snake = map.get("dimension_scores");
        if (snake instanceof Map<?, ?> dims) {
            return dims;
        }
        Object camelScore = map.get("dimensionScores");
        if (camelScore instanceof Map<?, ?> dims) {
            return dims;
        }
        boolean looksLikeDim = map.keySet().stream().anyMatch(key -> normalizeDimensionKey(key) != null);
        return looksLikeDim ? map : null;
    }

    private void mergeDimensionEntries(Map<String, Double> target, Map<?, ?> dims) {
        if (dims == null || dims.isEmpty()) {
            return;
        }
        for (Map.Entry<?, ?> entry : dims.entrySet()) {
            String code = normalizeDimensionKey(entry.getKey());
            if (code == null) {
                continue;
            }
            Double rawValue = toDouble(entry.getValue());
            if (rawValue == null) {
                continue;
            }
            double percent = toPercentScale(rawValue);
            if (percent <= 0) {
                continue;
            }
            double rounded = round(percent);
            target.merge(code, rounded, (prev, curr) -> prev.doubleValue() >= curr.doubleValue() ? prev : curr);
        }
    }

    private String normalizeDimensionKey(Object rawKey) {
        if (rawKey == null) {
            return null;
        }
        String key = String.valueOf(rawKey).trim();
        if (key.isEmpty()) {
            return null;
        }
        if ("道德认知".equals(key)) return "MORAL_COGNITION";
        if ("学习态度".equals(key)) return "LEARNING_ATTITUDE";
        if ("学习能力".equals(key)) return "LEARNING_ABILITY";
        if ("学习方法".equals(key)) return "LEARNING_METHOD";
        if ("学习成绩".equals(key)) return "ACADEMIC_GRADE";
        String upper = key.toUpperCase(java.util.Locale.ROOT);
        String ascii = upper.replaceAll("[^A-Z]", "");
        return switch (ascii) {
            case "MORALCOGNITION", "MORALREASONING", "MORALREASONINGSCORE", "MORALITY" -> "MORAL_COGNITION";
            case "LEARNINGATTITUDE", "ATTITUDE", "ATTITUDEDEVELOPMENT" -> "LEARNING_ATTITUDE";
            case "LEARNINGABILITY", "ABILITY", "ABILITYGROWTH" -> "LEARNING_ABILITY";
            case "LEARNINGMETHOD", "STRATEGY", "STRATEGYOPTIMIZATION" -> "LEARNING_METHOD";
            case "ACADEMICGRADE", "LEARNINGGRADE", "GRADE", "SCORE" -> "ACADEMIC_GRADE";
            default -> null;
        };
    }

    private double toPercentScale(double raw) {
        if (Double.isNaN(raw)) {
            return 0.0;
        }
        double value = raw;
        double abs = Math.abs(value);
        if (abs <= 5.5) {
            value = value * 20.0;
        }
        if (value < 0) {
            value = 0;
        }
        if (value > 100) {
            value = 100;
        }
        return value;
    }
}

