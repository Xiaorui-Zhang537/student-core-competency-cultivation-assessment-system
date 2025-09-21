package com.noncore.assessment.controller;

import com.noncore.assessment.entity.*;
import com.noncore.assessment.service.AbilityService;
import com.noncore.assessment.service.AbilityAnalyticsService;
import com.noncore.assessment.dto.request.AbilityRadarQuery;
import com.noncore.assessment.dto.request.AbilityCompareQuery;
import com.noncore.assessment.dto.response.AbilityRadarResponse;
import com.noncore.assessment.dto.response.AbilityCompareResponse;
import com.noncore.assessment.dto.response.AbilityDimensionInsightsResponse;
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
@RequestMapping("/ability")
@Tag(name = "能力评估", description = "能力评估相关操作")
public class AbilityController extends BaseController {

    private final AbilityService abilityService;
    private final AbilityAnalyticsService abilityAnalyticsService;

    public AbilityController(AbilityService abilityService, AbilityAnalyticsService abilityAnalyticsService, UserService userService) {
        super(userService);
        this.abilityService = abilityService;
        this.abilityAnalyticsService = abilityAnalyticsService;
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

    // ===== 学生端五维雷达（与教师端一致的5维，来源相同计算逻辑） =====
    @GetMapping("/student/radar")
    @Operation(summary = "学生五维能力雷达（学生 vs 班级/课程均值）", description = "courseId 必填；可选 classId、startDate、endDate；若缺失则按最近30天")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<AbilityRadarResponse>> getStudentRadar(
            @RequestParam Long courseId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) java.time.LocalDate startDate,
            @RequestParam(required = false) java.time.LocalDate endDate
    ) {
        Long studentId = getCurrentUserId();
        if (startDate == null || endDate == null) {
            java.time.LocalDate end = java.time.LocalDate.now();
            java.time.LocalDate start = end.minusDays(29);
            startDate = start;
            endDate = end;
        }
        AbilityRadarQuery q = new AbilityRadarQuery();
        q.setCourseId(courseId);
        q.setClassId(classId);
        q.setStartDate(startDate);
        q.setEndDate(endDate);
        AbilityRadarResponse resp = abilityAnalyticsService.getRadarForStudent(q, studentId);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @PostMapping("/student/radar/compare")
    @Operation(summary = "学生五维能力对比（A/B区间或作业集）", description = "必填：courseId；studentId 自动取当前用户；可选：A/B 日期区间、作业集合、是否包含班级均值")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<AbilityCompareResponse>> compareStudentRadar(@RequestBody AbilityCompareQuery body) {
        Long studentId = getCurrentUserId();
        body.setStudentId(studentId);
        AbilityCompareResponse resp = abilityAnalyticsService.getRadarCompareForStudent(body, studentId);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @PostMapping("/student/dimension-insights")
    @Operation(summary = "学生维度解析/文字分析", description = "在 A/B 条件下生成每个维度的对比分析与建议；未提供B则与自身A对比")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<AbilityDimensionInsightsResponse>> studentDimensionInsights(@RequestBody AbilityCompareQuery body) {
        Long studentId = getCurrentUserId();
        body.setStudentId(studentId);
        AbilityDimensionInsightsResponse resp = abilityAnalyticsService.getDimensionInsightsForStudent(body, studentId);
        return ResponseEntity.ok(ApiResponse.success(resp));
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
     * 学生：按上下文获取最新AI能力报告（与教师端策略一致，优先 submissionId -> assignmentId -> courseId）
     */
    @GetMapping("/student/report/latest-by-context")
    @Operation(summary = "学生按上下文获取最新AI能力报告", description = "用于学生端按当前作业/提交上下文查看完整AI报告")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<AbilityReport>> getStudentLatestReportByContext(
            @Parameter(description = "课程ID") @RequestParam(required = false) Long courseId,
            @Parameter(description = "作业ID") @RequestParam(required = false) Long assignmentId,
            @Parameter(description = "提交ID") @RequestParam(required = false) Long submissionId
    ) {
        Long studentId = getCurrentUserId();
        AbilityReport latestReport = abilityService.getLatestAbilityReportByContext(studentId, courseId, assignmentId, submissionId);
        return ResponseEntity.ok(ApiResponse.success(latestReport));
    }

    

    /**
     * 教师：获取指定学生的最新能力报告（用于评分页面持久化展示AI建议）
     */
    @GetMapping("/teacher/report/latest")
    @Operation(summary = "教师获取学生最新能力报告", description = "教师根据学生ID获取其最新的能力报告")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<AbilityReport>> getLatestAbilityReportForTeacher(
            @Parameter(description = "学生ID") @RequestParam Long studentId,
            @Parameter(description = "课程ID") @RequestParam(required = false) Long courseId,
            @Parameter(description = "作业ID") @RequestParam(required = false) Long assignmentId,
            @Parameter(description = "提交ID") @RequestParam(required = false) Long submissionId) {
        AbilityReport latestReport = abilityService.getLatestAbilityReportByContext(studentId, courseId, assignmentId, submissionId);
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
     * 教师：基于AI规范化结果创建能力报告
     */
    @PostMapping(value = "/teacher/report/from-ai", params = "studentId")
    @Operation(summary = "教师创建AI能力报告", description = "将AI批改的规范化JSON生成一条能力报告记录")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<AbilityReport>> createReportFromAi(
            @Parameter(description = "学生ID") @RequestParam Long studentId,
            @Parameter(description = "规范化JSON") @RequestParam String normalizedJson,
            @Parameter(description = "报告标题") @RequestParam(required = false) String title,
            @Parameter(description = "课程ID") @RequestParam(required = false) Long courseId,
            @Parameter(description = "作业ID") @RequestParam(required = false) Long assignmentId,
            @Parameter(description = "提交ID") @RequestParam(required = false) Long submissionId,
            @Parameter(description = "AI批改历史ID") @RequestParam(required = false) Long aiHistoryId
    ) {
        AbilityReport report = abilityService.createReportFromAi(studentId, normalizedJson, title, courseId, assignmentId, submissionId, aiHistoryId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    // JSON Body 版本，避免 URL 过长引发代理或CORS异常
    @PostMapping(value = "/teacher/report/from-ai", consumes = "application/json", params = "!studentId")
    @Operation(summary = "教师创建AI能力报告(JSON)", description = "以JSON body提交，字段：studentId、normalizedJson、title、courseId、assignmentId、submissionId")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<AbilityReport>> createReportFromAiJson(@RequestBody java.util.Map<String, Object> body) {
        Long studentId = body.get("studentId") == null ? null : Long.valueOf(String.valueOf(body.get("studentId")));
        String normalizedJson = String.valueOf(body.get("normalizedJson"));
        String title = body.get("title") == null ? null : String.valueOf(body.get("title"));
        Long courseId = body.get("courseId") == null ? null : Long.valueOf(String.valueOf(body.get("courseId")));
        Long assignmentId = body.get("assignmentId") == null ? null : Long.valueOf(String.valueOf(body.get("assignmentId")));
        Long submissionId = body.get("submissionId") == null ? null : Long.valueOf(String.valueOf(body.get("submissionId")));
        Long aiHistoryId = body.get("aiHistoryId") == null ? null : Long.valueOf(String.valueOf(body.get("aiHistoryId")));
        AbilityReport report = abilityService.createReportFromAi(studentId, normalizedJson, title, courseId, assignmentId, submissionId, aiHistoryId);
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