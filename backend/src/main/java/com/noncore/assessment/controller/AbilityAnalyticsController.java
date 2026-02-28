package com.noncore.assessment.controller;

import com.noncore.assessment.dto.request.AbilityRadarQuery;
import com.noncore.assessment.dto.request.UpdateAbilityWeightsRequest;
import com.noncore.assessment.dto.request.AbilityCompareQuery;
import com.noncore.assessment.dto.response.AbilityRadarResponse;
import com.noncore.assessment.dto.response.AbilityWeightsResponse;
import com.noncore.assessment.dto.response.AbilityCompareResponse;
import com.noncore.assessment.dto.response.AbilityDimensionInsightsResponse;
import com.noncore.assessment.dto.response.CourseStudentPerformanceItem;
import com.noncore.assessment.dto.response.CourseStudentPerformanceResponse;
import com.noncore.assessment.service.AbilityAnalyticsService;
import com.noncore.assessment.service.AnalyticsQueryService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.CsvUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/teachers/ability")
@Tag(name = "Teacher Ability Analytics")
@PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
public class AbilityAnalyticsController extends BaseController {

    private final AbilityAnalyticsService abilityAnalyticsService;
    private final AnalyticsQueryService analyticsQueryService;
    private static final List<String> DIMENSION_CODES = List.of(
            "MORAL_COGNITION", "LEARNING_ATTITUDE", "LEARNING_ABILITY", "LEARNING_METHOD", "ACADEMIC_GRADE"
    );

    public AbilityAnalyticsController(AbilityAnalyticsService abilityAnalyticsService, AnalyticsQueryService analyticsQueryService, UserService userService) {
        super(userService);
        this.abilityAnalyticsService = abilityAnalyticsService;
        this.analyticsQueryService = analyticsQueryService;
    }

    @GetMapping("/radar")
    @Operation(summary = "获取五维能力雷达数据（学生 vs 班级）")
    public ResponseEntity<ApiResponse<AbilityRadarResponse>> getRadar(AbilityRadarQuery query) {
        Long teacherId = getCurrentUserId();
        AbilityRadarResponse resp = abilityAnalyticsService.getRadar(query, teacherId);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @GetMapping("/weights")
    @Operation(summary = "读取课程五维权重")
    public ResponseEntity<ApiResponse<AbilityWeightsResponse>> getWeights(@RequestParam Long courseId) {
        Long teacherId = getCurrentUserId();
        AbilityWeightsResponse resp = abilityAnalyticsService.getWeights(courseId, teacherId);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @PutMapping("/weights")
    @Operation(summary = "更新课程五维权重")
    public ResponseEntity<ApiResponse<AbilityWeightsResponse>> updateWeights(@Valid @RequestBody UpdateAbilityWeightsRequest request) {
        Long teacherId = getCurrentUserId();
        AbilityWeightsResponse resp = abilityAnalyticsService.updateWeights(request, teacherId);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @GetMapping(value = "/radar/export", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "导出雷达数据（CSV）")
    public ResponseEntity<byte[]> exportRadarCsv(AbilityRadarQuery query, @RequestParam(defaultValue = "single") String scope) {
        Long teacherId = getCurrentUserId();
        StringBuilder sb = new StringBuilder();
        sb.append("studentId,studentName,studentNo,radarArea,radarClassification");
        for (String code : DIMENSION_CODES) {
            sb.append(',').append(code).append("_score");
            sb.append(',').append(code).append("_rating");
            sb.append(',').append(code).append("_classAvg");
        }
        sb.append('\n');

        List<CourseStudentPerformanceItem> targets = resolveExportStudents(scope, teacherId, query.getCourseId(), query.getStudentId());
        for (CourseStudentPerformanceItem target : targets) {
            if (target == null || target.getStudentId() == null) continue;
            AbilityRadarQuery one = new AbilityRadarQuery();
            one.setCourseId(query.getCourseId());
            one.setClassId(query.getClassId());
            one.setStudentId(target.getStudentId());
            one.setStartDate(query.getStartDate());
            one.setEndDate(query.getEndDate());

            AbilityRadarResponse resp = abilityAnalyticsService.getRadar(one, teacherId);
            Map<String, Double> studentScores = toScoreMap(resp.getDimensions(), resp.getStudentScores());
            Map<String, Double> classScores = toScoreMap(resp.getDimensions(), resp.getClassAvgScores());
            List<Double> scoreVector = new ArrayList<>();
            for (String code : DIMENSION_CODES) {
                scoreVector.add(studentScores.getOrDefault(code, 0.0));
            }

            sb.append(CsvUtils.nullToEmpty(target.getStudentId()))
                    .append(',').append(CsvUtils.escape(target.getStudentName()))
                    .append(',').append(CsvUtils.escape(target.getStudentNo()))
                    .append(',').append(CsvUtils.nullToEmpty(calculateRadarAreaPercent(scoreVector)))
                    .append(',').append(CsvUtils.escape(classifyRadar(scoreVector)));

            for (String code : DIMENSION_CODES) {
                Double score = studentScores.get(code);
                Double classAvg = classScores.get(code);
                sb.append(',').append(CsvUtils.nullToEmpty(score));
                sb.append(',').append(CsvUtils.escape(dimensionRating(score)));
                sb.append(',').append(CsvUtils.nullToEmpty(classAvg));
            }
            sb.append('\n');
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ability_radar_" + query.getCourseId() + "_" + scope + ".csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    private static String n(Double v) { return v == null ? "" : String.valueOf(v); }

    @PostMapping("/radar/compare")
    @Operation(summary = "对比两段区间/作业集合的五维能力（同一学生）")
    public ResponseEntity<ApiResponse<AbilityCompareResponse>> compare(@Valid @RequestBody AbilityCompareQuery body) {
        Long teacherId = getCurrentUserId();
        AbilityCompareResponse resp = abilityAnalyticsService.getRadarCompare(body, teacherId);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @PostMapping(value = "/radar/compare/export", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "导出对比雷达数据（CSV）")
    public ResponseEntity<byte[]> exportCompareCsv(@Valid @RequestBody AbilityCompareQuery body, @RequestParam(defaultValue = "single") String scope) {
        Long teacherId = getCurrentUserId();
        StringBuilder sb = new StringBuilder();
        sb.append("studentId,studentName,studentNo,radarAreaA,radarClassificationA,radarAreaB,radarClassificationB,compositeDeltaAB");
        for (String code : DIMENSION_CODES) {
            sb.append(',').append(code).append("_A_score");
            sb.append(',').append(code).append("_A_rating");
            sb.append(',').append(code).append("_A_classAvg");
            sb.append(',').append(code).append("_B_score");
            sb.append(',').append(code).append("_B_rating");
            sb.append(',').append(code).append("_B_classAvg");
            sb.append(',').append(code).append("_deltaAB");
        }
        sb.append('\n');

        List<CourseStudentPerformanceItem> targets = resolveExportStudents(scope, teacherId, body.getCourseId(), body.getStudentId());
        for (CourseStudentPerformanceItem target : targets) {
            if (target == null || target.getStudentId() == null) continue;
            AbilityCompareQuery one = copyCompareBody(body);
            one.setStudentId(target.getStudentId());
            AbilityCompareResponse resp = abilityAnalyticsService.getRadarCompare(one, teacherId);

            Map<String, Double> scoresA = toScoreMap(resp.getDimensions(), resp.getSeriesA() == null ? null : resp.getSeriesA().getStudentScores());
            Map<String, Double> scoresB = toScoreMap(resp.getDimensions(), resp.getSeriesB() == null ? null : resp.getSeriesB().getStudentScores());
            Map<String, Double> classA = toScoreMap(resp.getDimensions(), resp.getSeriesA() == null ? null : resp.getSeriesA().getClassAvgScores());
            Map<String, Double> classB = toScoreMap(resp.getDimensions(), resp.getSeriesB() == null ? null : resp.getSeriesB().getClassAvgScores());

            List<Double> vectorA = new ArrayList<>();
            List<Double> vectorB = new ArrayList<>();
            for (String code : DIMENSION_CODES) {
                vectorA.add(scoresA.getOrDefault(code, 0.0));
                vectorB.add(scoresB.getOrDefault(code, 0.0));
            }

            sb.append(CsvUtils.nullToEmpty(target.getStudentId()))
                    .append(',').append(CsvUtils.escape(target.getStudentName()))
                    .append(',').append(CsvUtils.escape(target.getStudentNo()))
                    .append(',').append(CsvUtils.nullToEmpty(calculateRadarAreaPercent(vectorA)))
                    .append(',').append(CsvUtils.escape(classifyRadar(vectorA)))
                    .append(',').append(CsvUtils.nullToEmpty(calculateRadarAreaPercent(vectorB)))
                    .append(',').append(CsvUtils.escape(classifyRadar(vectorB)))
                    .append(',').append(CsvUtils.nullToEmpty(resp.getCompositeDeltaAB()));

            for (String code : DIMENSION_CODES) {
                Double aScore = scoresA.get(code);
                Double bScore = scoresB.get(code);
                Double dScore = (aScore == null || bScore == null) ? null : round(aScore - bScore);
                sb.append(',').append(CsvUtils.nullToEmpty(aScore));
                sb.append(',').append(CsvUtils.escape(dimensionRating(aScore)));
                sb.append(',').append(CsvUtils.nullToEmpty(classA.get(code)));
                sb.append(',').append(CsvUtils.nullToEmpty(bScore));
                sb.append(',').append(CsvUtils.escape(dimensionRating(bScore)));
                sb.append(',').append(CsvUtils.nullToEmpty(classB.get(code)));
                sb.append(',').append(CsvUtils.nullToEmpty(dScore));
            }
            sb.append('\n');
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ability_radar_compare_" + body.getCourseId() + "_" + scope + ".csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @PostMapping("/dimension-insights")
    @Operation(summary = "维度解析（预留：后续可由AI生成）")
    public ResponseEntity<ApiResponse<AbilityDimensionInsightsResponse>> insights(@Valid @RequestBody AbilityCompareQuery body) {
        Long teacherId = getCurrentUserId();
        AbilityDimensionInsightsResponse resp = abilityAnalyticsService.getDimensionInsights(body, teacherId);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    private static Double safeGet(java.util.List<Double> list, int i) {
        if (list == null || i < 0 || i >= list.size()) return null;
        return list.get(i);
    }

    private List<CourseStudentPerformanceItem> resolveExportStudents(String scope, Long teacherId, Long courseId, Long studentId) {
        CourseStudentPerformanceResponse perf = analyticsQueryService.getCourseStudentPerformance(
                teacherId, courseId, 1, 100000, null, "radar", null, null, null
        );
        List<CourseStudentPerformanceItem> all = perf == null || perf.getItems() == null ? List.of() : perf.getItems();
        boolean allScope = "all".equalsIgnoreCase(scope);
        if (allScope) return all;
        if (studentId == null) return List.of();
        List<CourseStudentPerformanceItem> single = new ArrayList<>();
        for (CourseStudentPerformanceItem it : all) {
            if (it != null && Objects.equals(it.getStudentId(), studentId)) {
                single.add(it);
                break;
            }
        }
        return single;
    }

    private static AbilityCompareQuery copyCompareBody(AbilityCompareQuery src) {
        AbilityCompareQuery dst = new AbilityCompareQuery();
        dst.setCourseId(src.getCourseId());
        dst.setClassId(src.getClassId());
        dst.setStudentId(src.getStudentId());
        dst.setStartDateA(src.getStartDateA());
        dst.setEndDateA(src.getEndDateA());
        dst.setAssignmentIdsA(src.getAssignmentIdsA());
        dst.setStartDateB(src.getStartDateB());
        dst.setEndDateB(src.getEndDateB());
        dst.setAssignmentIdsB(src.getAssignmentIdsB());
        dst.setIncludeClassAvg(src.getIncludeClassAvg());
        return dst;
    }

    private static Map<String, Double> toScoreMap(List<String> dims, List<Double> scores) {
        Map<String, Double> map = new LinkedHashMap<>();
        if (dims == null) return map;
        for (int i = 0; i < dims.size(); i++) {
            String code = dimToCode(dims.get(i));
            Double value = (scores != null && i < scores.size()) ? scores.get(i) : null;
            map.put(code, value);
        }
        return map;
    }

    private static String dimToCode(String nameZh) {
        if ("道德认知".equals(nameZh)) return "MORAL_COGNITION";
        if ("学习态度".equals(nameZh)) return "LEARNING_ATTITUDE";
        if ("学习能力".equals(nameZh)) return "LEARNING_ABILITY";
        if ("学习方法".equals(nameZh)) return "LEARNING_METHOD";
        if ("学习成绩".equals(nameZh)) return "ACADEMIC_GRADE";
        return nameZh;
    }

    private static String dimensionRating(Double score) {
        if (score == null) return "";
        double s = score;
        if (s >= 85.0) return "A";
        if (s >= 70.0) return "B";
        if (s >= 60.0) return "C";
        return "D";
    }

    private static double calculateRadarAreaPercent(List<Double> scores) {
        if (scores == null || scores.size() < 3) return 0.0;
        int n = scores.size();
        double angle = (2 * Math.PI) / n;
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            double r1 = safeScore(scores.get(i)) / 100.0;
            double r2 = safeScore(scores.get((i + 1) % n)) / 100.0;
            sum += r1 * r2 * Math.sin(angle);
        }
        double area = 0.5 * sum;
        double maxArea = 0.5 * n * Math.sin(angle);
        if (maxArea <= 0) return 0.0;
        double percent = (area / maxArea) * 100.0;
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;
        return round(percent);
    }

    private static String classifyRadar(List<Double> scores) {
        if (scores == null || scores.isEmpty()) return "D";
        int n = scores.size();
        double gradeScore = safeScore(scores.get(n - 1));
        double nonGradeSum = 0.0;
        for (int i = 0; i < n - 1; i++) nonGradeSum += safeScore(scores.get(i));
        double nonGradeAvg = (n - 1) <= 0 ? 0.0 : nonGradeSum / (n - 1);
        boolean abilityHigh = nonGradeAvg >= 75.0;
        boolean gradeHigh = gradeScore >= 75.0;
        if (abilityHigh && gradeHigh) return "A";
        if (abilityHigh) return "B";
        if (gradeHigh) return "C";
        return "D";
    }

    private static double safeScore(Double value) {
        return value == null ? 0.0 : value;
    }

    private static Double round(Double v) { return v == null ? null : Math.round(v * 10.0) / 10.0; }
}

