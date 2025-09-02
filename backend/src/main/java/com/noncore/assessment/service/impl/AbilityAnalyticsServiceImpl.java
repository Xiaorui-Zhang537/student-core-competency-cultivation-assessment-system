package com.noncore.assessment.service.impl;

import com.noncore.assessment.dto.request.AbilityRadarQuery;
import com.noncore.assessment.dto.request.UpdateAbilityWeightsRequest;
import com.noncore.assessment.dto.request.AbilityCompareQuery;
import com.noncore.assessment.dto.response.AbilityRadarResponse;
import com.noncore.assessment.dto.response.AbilityWeightsResponse;
import com.noncore.assessment.dto.response.AbilityCompareResponse;
import com.noncore.assessment.dto.response.AbilityDimensionInsightsResponse;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.AbilityAnalyticsMapper;
import com.noncore.assessment.mapper.CourseMapper;
import com.noncore.assessment.service.AbilityAnalyticsService;
import com.noncore.assessment.service.CacheService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public AbilityAnalyticsServiceImpl(AbilityAnalyticsMapper analyticsMapper,
                                       CourseMapper courseMapper,
                                       CacheService cacheService) {
        this.analyticsMapper = analyticsMapper;
        this.courseMapper = courseMapper;
        this.cacheService = cacheService;
    }

    @Override
    public AbilityRadarResponse getRadar(AbilityRadarQuery query, Long teacherId) {
        Course course = courseMapper.selectCourseById(query.getCourseId());
        if (course == null || !Objects.equals(course.getTeacherId(), teacherId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
        }

        validateDates(query.getStartDate(), query.getEndDate());
        String cacheKey = buildCacheKey("radar", teacherId, query);
        Optional<AbilityRadarResponse> cached = cacheService.get(cacheKey, AbilityRadarResponse.class);
        if (cached.isPresent()) {
            return cached.get();
        }

        LocalDateTime start = query.getStartDate().atStartOfDay();
        LocalDateTime end = query.getEndDate().atTime(LocalTime.MAX);

        // 班级或课程均值
        Map<String, Double> classAvg = aggregateDimensions(null, query.getCourseId(), query.getClassId(), start, end);

        // 学生成绩（若传 studentId）
        Map<String, Double> student = query.getStudentId() == null
                ? Collections.emptyMap()
                : aggregateDimensions(query.getStudentId(), query.getCourseId(), query.getClassId(), start, end);

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

        List<Double> deltas = new ArrayList<>();
        for (int i = 0; i < DIMENSIONS.size(); i++) {
            deltas.add(round(studentScores.get(i) - classScores.get(i)));
        }

        Map<String, Double> weights = loadWeightsOrDefault(query.getCourseId());
        double studentComposite = weightedComposite(studentScores, weights);
        double classComposite = weightedComposite(classScores, weights);

        // 上一对等周期
        AbilityRadarResponse.PreviousPeriod prev = buildPreviousPeriod(query);

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
        validateDates(query.getStartDate(), query.getEndDate());

        LocalDateTime start = query.getStartDate().atStartOfDay();
        LocalDateTime end = query.getEndDate().atTime(LocalTime.MAX);

        Map<String, Double> classAvg = aggregateDimensions(null, query.getCourseId(), query.getClassId(), start, end);
        Map<String, Double> student = aggregateDimensions(studentId, query.getCourseId(), query.getClassId(), start, end);
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
        List<Double> deltas = new ArrayList<>();
        for (int i = 0; i < DIMENSIONS.size(); i++) {
            deltas.add(round(studentScores.get(i) - classScores.get(i)));
        }
        Map<String, Double> weights = loadWeightsOrDefault(query.getCourseId());
        double studentComposite = round(weightedComposite(studentScores, weights));
        double classComposite = round(weightedComposite(classScores, weights));

        AbilityRadarResponse.PreviousPeriod prev = buildPreviousPeriod(query);

        List<String> weak = new ArrayList<>();
        for (int i = 0; i < DIMENSIONS.size(); i++) {
            double s = studentScores.get(i);
            double d = deltas.get(i);
            if (s < 60 || d < -10) {
                weak.add(DIMENSIONS.get(i));
            }
        }

        return AbilityRadarResponse.builder()
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
        if (course == null || !Objects.equals(course.getTeacherId(), teacherId)) {
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
        if (course == null || !Objects.equals(course.getTeacherId(), teacherId)) {
            throw new BusinessException(ErrorCode.COURSE_ACCESS_DENIED);
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
        Map<String, Double> result = new java.util.HashMap<>();
        for (Map<String, Object> r : rows) {
            String name = String.valueOf(r.get("dimensionName"));
            double score = toDouble(r.get("avgScore"));
            double max = toDouble(r.get("maxScore"));
            result.put(name, normalize(score, max));
        }
        return result;
    }

    private double normalizeGradeAvgWithAssignments(Long courseId, Long classId, Long studentId,
                                                    java.time.LocalDateTime start, java.time.LocalDateTime end,
                                                    java.util.List<Long> assignmentIds) {
        Map<String, Object> row = analyticsMapper.selectCourseGradeAvgWithAssignments(courseId, classId, studentId, start, end, assignmentIds);
        if (row == null) return 0.0;
        double score = toDouble(row.get("avgScore"));
        double max = toDouble(row.get("maxScore"));
        return normalize(score, max);
    }

    private void validateOptionalDates(java.time.LocalDate start, java.time.LocalDate end) {
        if (start == null && end == null) return;
        if (start == null || end == null || end.isBefore(start)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }
        long span = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        if (span > 365) {
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
        if (course == null || !Objects.equals(course.getTeacherId(), teacherId)) {
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
        Map<String, Double> result = new HashMap<>();
        for (Map<String, Object> r : rows) {
            String name = String.valueOf(r.get("dimensionName"));
            double score = toDouble(r.get("avgScore"));
            double max = toDouble(r.get("maxScore"));
            result.put(name, normalize(score, max));
        }
        return result;
    }

    private double normalizeGradeAvg(Long courseId, Long classId, Long studentId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> row = analyticsMapper.selectCourseGradeAvg(courseId, classId, studentId, start, end);
        if (row == null) return 0.0;
        double score = toDouble(row.get("avgScore"));
        double max = toDouble(row.get("maxScore"));
        return normalize(score, max);
    }

    private double normalize(double score, double max) {
        if (max <= 0) return 0.0;
        return round(score * 100.0 / max);
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
}

