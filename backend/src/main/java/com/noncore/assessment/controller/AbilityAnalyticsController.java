package com.noncore.assessment.controller;

import com.noncore.assessment.dto.request.AbilityRadarQuery;
import com.noncore.assessment.dto.request.UpdateAbilityWeightsRequest;
import com.noncore.assessment.dto.request.AbilityCompareQuery;
import com.noncore.assessment.dto.response.AbilityRadarResponse;
import com.noncore.assessment.dto.response.AbilityWeightsResponse;
import com.noncore.assessment.dto.response.AbilityCompareResponse;
import com.noncore.assessment.dto.response.AbilityDimensionInsightsResponse;
import com.noncore.assessment.service.AbilityAnalyticsService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/teachers/ability")
@Tag(name = "Teacher Ability Analytics")
@PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
public class AbilityAnalyticsController extends BaseController {

    private final AbilityAnalyticsService abilityAnalyticsService;

    public AbilityAnalyticsController(AbilityAnalyticsService abilityAnalyticsService, UserService userService) {
        super(userService);
        this.abilityAnalyticsService = abilityAnalyticsService;
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
    public ResponseEntity<byte[]> exportRadarCsv(AbilityRadarQuery query) {
        Long teacherId = getCurrentUserId();
        AbilityRadarResponse resp = abilityAnalyticsService.getRadar(query, teacherId);
        StringBuilder sb = new StringBuilder();
        sb.append("dimension,student,class,delta\n");
        for (int i = 0; i < resp.getDimensions().size(); i++) {
            sb.append(resp.getDimensions().get(i)).append(',')
                    .append(n(resp.getStudentScores().get(i))).append(',')
                    .append(n(resp.getClassAvgScores().get(i))).append(',')
                    .append(n(resp.getDeltas().get(i))).append('\n');
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv;charset=UTF-8")).body(bytes);
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
    public ResponseEntity<byte[]> exportCompareCsv(@Valid @RequestBody AbilityCompareQuery body) {
        Long teacherId = getCurrentUserId();
        AbilityCompareResponse resp = abilityAnalyticsService.getRadarCompare(body, teacherId);
        StringBuilder sb = new StringBuilder();
        sb.append("dimension,A_student,A_class,B_student,B_class,deltaAB,weight,weightedA,weightedB,weightedDelta\n");
        for (int i = 0; i < resp.getDimensions().size(); i++) {
            String dim = resp.getDimensions().get(i);
            Double aS = safeGet(resp.getSeriesA().getStudentScores(), i);
            Double aC = safeGet(resp.getSeriesA().getClassAvgScores(), i);
            Double bS = safeGet(resp.getSeriesB().getStudentScores(), i);
            Double bC = safeGet(resp.getSeriesB().getClassAvgScores(), i);
            Double d  = safeGet(resp.getDeltasAB(), i);
            Double w  = resp.getWeights() == null ? null : resp.getWeights().get(dimToCode(dim));
            Double wA = w == null || aS == null ? null : round(aS * w);
            Double wB = w == null || bS == null ? null : round(bS * w);
            Double wD = (wA == null || wB == null) ? null : round(wA - wB);
            sb.append(dim).append(',')
              .append(n(aS)).append(',')
              .append(n(aC)).append(',')
              .append(n(bS)).append(',')
              .append(n(bC)).append(',')
              .append(n(d)).append(',')
              .append(n(w)).append(',')
              .append(n(wA)).append(',')
              .append(n(wB)).append(',')
              .append(n(wD)).append('\n');
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv;charset=UTF-8")).body(bytes);
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

    private static String dimToCode(String nameZh) {
        if ("道德认知".equals(nameZh)) return "MORAL_COGNITION";
        if ("学习态度".equals(nameZh)) return "LEARNING_ATTITUDE";
        if ("学习能力".equals(nameZh)) return "LEARNING_ABILITY";
        if ("学习方法".equals(nameZh)) return "LEARNING_METHOD";
        if ("学习成绩".equals(nameZh)) return "ACADEMIC_GRADE";
        return nameZh;
    }

    private static Double round(Double v) { return v == null ? null : Math.round(v * 10.0) / 10.0; }
}

