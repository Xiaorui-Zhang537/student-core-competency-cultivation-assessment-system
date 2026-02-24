package com.noncore.assessment.controller.admin;

import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.dto.request.AbilityCompareQuery;
import com.noncore.assessment.dto.request.AbilityRadarQuery;
import com.noncore.assessment.dto.response.AbilityCompareResponse;
import com.noncore.assessment.dto.response.AbilityRadarResponse;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.AbilityAnalyticsService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 管理员-能力雷达与对比（审计/数据中心）。
 *
 * <p>说明：管理员可全局查看任意学生在指定课程下的五维能力雷达与区间对比，复用学生端计算逻辑。</p>
 *
 * @author System
 * @since 2026-02-14
 */
@RestController
@RequestMapping("/admin/ability")
@Tag(name = "管理员-能力分析", description = "管理员能力雷达与对比（全局）")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAbilityAnalyticsController extends BaseController {

    private final AbilityAnalyticsService abilityAnalyticsService;

    public AdminAbilityAnalyticsController(AbilityAnalyticsService abilityAnalyticsService, UserService userService) {
        super(userService);
        this.abilityAnalyticsService = abilityAnalyticsService;
    }

    @GetMapping("/radar")
    @Operation(summary = "管理员获取学生五维能力雷达", description = "参数：studentId, courseId 必填；可选 classId、startDate、endDate；若缺失则按最近30天")
    public ResponseEntity<ApiResponse<AbilityRadarResponse>> getRadar(
            @Parameter(description = "学生ID") @RequestParam Long studentId,
            @Parameter(description = "课程ID") @RequestParam Long courseId,
            @Parameter(description = "班级ID（可选）") @RequestParam(required = false) Long classId,
            @Parameter(description = "开始日期（可选）") @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "结束日期（可选）") @RequestParam(required = false) LocalDate endDate
    ) {
        AbilityRadarQuery q = new AbilityRadarQuery();
        q.setCourseId(courseId);
        q.setClassId(classId);
        q.setStartDate(startDate);
        q.setEndDate(endDate);
        AbilityRadarResponse resp = abilityAnalyticsService.getRadarForStudent(q, studentId);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @PostMapping("/radar/compare")
    @Operation(summary = "管理员获取学生五维能力对比（A/B）", description = "Body 复用 AbilityCompareQuery，必填 courseId；studentId 可选，若未提供则必须通过 queryParam 传入")
    public ResponseEntity<ApiResponse<AbilityCompareResponse>> compareRadar(
            @Parameter(description = "学生ID（可选；若 body 未提供则使用此参数）") @RequestParam(required = false) Long studentId,
            @RequestBody AbilityCompareQuery body
    ) {
        Long sid = body.getStudentId() != null ? body.getStudentId() : studentId;
        if (sid == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "studentId 不能为空");
        }
        body.setStudentId(sid);
        AbilityCompareResponse resp = abilityAnalyticsService.getRadarCompareForStudent(body, sid);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }
}

