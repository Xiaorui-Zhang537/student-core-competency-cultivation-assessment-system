package com.noncore.assessment.controller;

import com.noncore.assessment.entity.*;
import com.noncore.assessment.service.AbilityService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 能力评估控制器
 * 提供能力评估相关的API接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@RestController
@RequestMapping("/api/ability")
@Tag(name = "能力评估", description = "能力评估相关操作")
public class AbilityController extends BaseController {

    private final AbilityService abilityService;

    public AbilityController(AbilityService abilityService, UserService userService) {
        super(userService);
        this.abilityService = abilityService;
    }

    /**
     * 获取所有能力维度
     */
    @GetMapping("/dimensions")
    @Operation(summary = "获取所有能力维度", description = "获取系统中定义的所有能力维度")
    public ResponseEntity<ApiResponse<List<AbilityDimension>>> getAbilityDimensions() {
        List<AbilityDimension> dimensions = abilityService.getAllAbilityDimensions();
        return ResponseEntity.ok(ApiResponse.success(dimensions));
    }

    /**
     * 获取学生能力仪表板
     */
    @GetMapping("/student/dashboard")
    @Operation(summary = "获取学生能力仪表板", description = "获取学生的能力发展概览数据")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStudentAbilityDashboard() {
        Map<String, Object> dashboard = abilityService.getStudentAbilityDashboard(getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    /**
     * 获取能力发展趋势
     */
    @GetMapping("/student/trends")
    @Operation(summary = "获取能力发展趋势", description = "获取学生能力发展的时间序列数据")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAbilityTrends(
            @Parameter(description = "能力维度ID（可选）") @RequestParam(required = false) Long dimensionId,
            @Parameter(description = "时间范围（月数）", example = "6") @RequestParam(defaultValue = "6") Integer timeRange) {
        List<Map<String, Object>> trends = abilityService.getStudentAbilityTrends(getCurrentUserId(), dimensionId, timeRange);
        return ResponseEntity.ok(ApiResponse.success(trends));
    }

    /**
     * 获取学习建议
     */
    @GetMapping("/student/recommendations")
    @Operation(summary = "获取学习建议", description = "获取基于能力评估结果的个性化学习建议")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<LearningRecommendation>>> getLearningRecommendations(
            @Parameter(description = "能力维度ID（可选）") @RequestParam(required = false) Long dimensionId,
            @Parameter(description = "返回数量限制", example = "10") @RequestParam(defaultValue = "10") Integer limit) {
        List<LearningRecommendation> recommendations = abilityService.getLearningRecommendations(getCurrentUserId(), dimensionId, limit);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }

    /**
     * 获取学生能力目标
     */
    @GetMapping("/student/goals")
    @Operation(summary = "获取学生能力目标", description = "获取学生设定的能力发展目标")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<AbilityGoal>>> getStudentGoals() {
        List<AbilityGoal> goals = abilityService.getStudentGoals(getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(goals));
    }

    /**
     * 创建能力目标
     */
    @PostMapping("/student/goals")
    @Operation(summary = "创建能力目标", description = "学生创建新的能力发展目标")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<AbilityGoal>> createAbilityGoal(@Valid @RequestBody AbilityGoal goal) {
        goal.setStudentId(getCurrentUserId());
        AbilityGoal createdGoal = abilityService.createAbilityGoal(goal);
        return ResponseEntity.ok(ApiResponse.success(createdGoal));
    }

    /**
     * 更新能力目标
     */
    @PutMapping("/student/goals/{goalId}")
    @Operation(summary = "更新能力目标", description = "更新已存在的能力发展目标")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<AbilityGoal>> updateAbilityGoal(
            @Parameter(description = "目标ID") @PathVariable Long goalId,
            @Valid @RequestBody AbilityGoal goal) {
        AbilityGoal updatedGoal = abilityService.updateAbilityGoal(goalId, goal);
        return ResponseEntity.ok(ApiResponse.success(updatedGoal));
    }

    /**
     * 删除能力目标
     */
    @DeleteMapping("/student/goals/{goalId}")
    @Operation(summary = "删除能力目标", description = "删除指定的能力发展目标")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> deleteAbilityGoal(@Parameter(description = "目标ID") @PathVariable Long goalId) {
        abilityService.deleteAbilityGoal(goalId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 提交自评
     */
    @PostMapping("/student/self-assessment")
    @Operation(summary = "提交自评", description = "学生对某个能力维度进行自我评估")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<AbilityAssessment>> submitSelfAssessment(
            @Parameter(description = "维度ID") @RequestParam Long dimensionId,
            @Parameter(description = "自评分数") @RequestParam BigDecimal score,
            @Parameter(description = "自评反馈") @RequestParam(required = false) String feedback) {
        AbilityAssessment assessment = abilityService.submitSelfAssessment(getCurrentUserId(), dimensionId, score, feedback);
        return ResponseEntity.ok(ApiResponse.success(assessment));
    }

    /**
     * 获取评估历史
     */
    @GetMapping("/student/assessments")
    @Operation(summary = "获取评估历史", description = "分页获取学生的能力评估历史记录")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<PageResult<AbilityAssessment>>> getStudentAssessments(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") Integer size) {
        PageResult<AbilityAssessment> assessments = abilityService.getStudentAssessments(getCurrentUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(assessments));
    }

    /**
     * 获取能力报告历史
     */
    @GetMapping("/student/reports")
    @Operation(summary = "获取能力报告历史", description = "分页获取学生的能力分析报告")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<PageResult<AbilityReport>>> getAbilityReportHistory(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size) {
        PageResult<AbilityReport> reports = abilityService.getAbilityReportHistory(getCurrentUserId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    /**
     * 获取最新能力报告
     */
    @GetMapping("/student/report/latest")
    @Operation(summary = "获取最新能力报告", description = "获取学生最新的能力分析报告")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<AbilityReport>> getLatestAbilityReport() {
        AbilityReport latestReport = abilityService.getLatestAbilityReport(getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(latestReport));
    }

    /**
     * 获取指定能力报告
     */
    @GetMapping("/student/report/{reportId}")
    @Operation(summary = "获取指定能力报告", description = "根据报告ID获取详细的能力分析报告")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<AbilityReport>> getAbilityReport(@Parameter(description = "报告ID") @PathVariable Long reportId) {
        AbilityReport report = abilityService.getAbilityReportById(reportId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    /**
     * 生成能力报告
     */
    @PostMapping("/student/report/generate")
    @Operation(summary = "生成能力报告", description = "手动生成学生能力分析报告")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<AbilityReport>> generateAbilityReport(
            @Parameter(description = "报告类型") @RequestParam(defaultValue = "monthly") String reportType,
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDate endDate) {
        Long studentId = getCurrentUserId();
        
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusMonths(1);
        }
        
        AbilityReport report = abilityService.generateStudentReport(studentId, reportType, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    /**
     * 标记学习建议为已读
     */
    @PostMapping("/student/recommendations/{recommendationId}/read")
    @Operation(summary = "标记学习建议为已读", description = "将指定的学习建议标记为已读状态")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> markRecommendationAsRead(@Parameter(description = "建议ID") @PathVariable Long recommendationId) {
        abilityService.markRecommendationAsRead(recommendationId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 采纳学习建议
     */
    @PostMapping("/student/recommendations/{recommendationId}/accept")
    @Operation(summary = "采纳学习建议", description = "采纳指定的学习建议")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> acceptRecommendation(@Parameter(description = "建议ID") @PathVariable Long recommendationId) {
        abilityService.acceptRecommendation(recommendationId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ==================== 教师接口 ====================

    /**
     * 获取班级能力统计
     */
    @GetMapping("/teacher/class/{courseId}/stats")
    @Operation(summary = "获取班级能力统计", description = "教师查看班级整体能力发展统计")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getClassAbilityStats(
            @Parameter(description = "课程ID") @PathVariable Long courseId,
            @Parameter(description = "能力维度ID（可选）") @RequestParam(required = false) Long dimensionId) {
        List<Map<String, Object>> stats = abilityService.getClassAbilityStats(courseId, dimensionId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 记录能力评估
     */
    @PostMapping("/teacher/assessment")
    @Operation(summary = "记录能力评估", description = "教师对学生进行能力评估")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<Void>> recordAbilityAssessment(
            @Parameter(description = "学生ID") @RequestParam Long studentId,
            @Parameter(description = "维度ID") @RequestParam Long dimensionId,
            @Parameter(description = "得分") @RequestParam BigDecimal score,
            @Parameter(description = "评估类型") @RequestParam String assessmentType,
            @Parameter(description = "关联对象ID") @RequestParam(required = false) Long relatedId,
            @Parameter(description = "评估依据") @RequestParam(required = false) String evidence) {
        abilityService.recordAbilityAssessment(studentId, dimensionId, score, assessmentType, relatedId, evidence);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 获取能力排名
     */
    @GetMapping("/teacher/ranking")
    @Operation(summary = "获取能力排名", description = "获取学生在某个能力维度上的排名")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getStudentAbilityRanking(
            @Parameter(description = "维度ID") @RequestParam Long dimensionId,
            @Parameter(description = "返回数量", example = "50") @RequestParam(defaultValue = "50") Integer limit) {
        List<Map<String, Object>> ranking = abilityService.getStudentAbilityRanking(dimensionId, limit);
        return ResponseEntity.ok(ApiResponse.success(ranking));
    }
} 