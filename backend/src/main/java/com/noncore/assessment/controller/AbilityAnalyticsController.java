package com.noncore.assessment.controller;

import com.noncore.assessment.dto.request.AbilityRadarQuery;
import com.noncore.assessment.dto.request.UpdateAbilityWeightsRequest;
import com.noncore.assessment.dto.response.AbilityRadarResponse;
import com.noncore.assessment.dto.response.AbilityWeightsResponse;
import com.noncore.assessment.service.AbilityAnalyticsService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/teachers/ability")
@Tag(name = "Teacher Ability Analytics")
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
}

