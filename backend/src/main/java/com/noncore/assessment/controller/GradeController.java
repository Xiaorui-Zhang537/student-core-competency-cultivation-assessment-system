package com.noncore.assessment.controller;

import com.noncore.assessment.entity.Grade;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.GradeService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 成绩管理控制器
 * 提供成绩相关的API接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@RestController
@RequestMapping("/api/grades")
@Tag(name = "成绩管理", description = "成绩录入、查询、统计等相关接口")
public class GradeController {

    private static final Logger logger = LoggerFactory.getLogger(GradeController.class);

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    /**
     * 创建成绩记录
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "创建成绩", description = "教师为学生作业创建成绩记录")
    public ApiResponse<Grade> createGrade(@Valid @RequestBody Grade grade) {
        try {
            Grade createdGrade = gradeService.createGrade(grade);
            return ApiResponse.success("成绩创建成功", createdGrade);
        } catch (Exception e) {
            logger.error("创建成绩失败", e);
            return ApiResponse.error("创建成绩失败: " + e.getMessage());
        }
    }

    /**
     * 获取成绩详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "获取成绩详情", description = "根据成绩ID获取详细信息")
    public ApiResponse<Grade> getGradeById(@PathVariable Long id) {
        try {
            Grade grade = gradeService.getGradeById(id);
            return ApiResponse.success("获取成绩详情成功", grade);
        } catch (Exception e) {
            logger.error("获取成绩详情失败: gradeId={}", id, e);
            return ApiResponse.error("获取成绩详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新成绩信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "更新成绩", description = "更新成绩信息")
    public ApiResponse<Grade> updateGrade(@PathVariable Long id, @Valid @RequestBody Grade grade) {
        try {
            // 检查权限
            Long currentUserId = getCurrentUserId();
            if (!gradeService.canModifyGrade(id, currentUserId)) {
                return ApiResponse.error("没有权限修改该成绩");
            }
            
            Grade updatedGrade = gradeService.updateGrade(id, grade);
            return ApiResponse.success("成绩更新成功", updatedGrade);
        } catch (Exception e) {
            logger.error("更新成绩失败: gradeId={}", id, e);
            return ApiResponse.error("更新成绩失败: " + e.getMessage());
        }
    }

    /**
     * 删除成绩记录
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "删除成绩", description = "删除指定成绩记录")
    public ApiResponse<Void> deleteGrade(@PathVariable Long id) {
        try {
            // 检查权限
            Long currentUserId = getCurrentUserId();
            if (!gradeService.canModifyGrade(id, currentUserId)) {
                return ApiResponse.error("没有权限删除该成绩");
            }
            
            gradeService.deleteGrade(id);
            return ApiResponse.success("成绩删除成功");
        } catch (Exception e) {
            logger.error("删除成绩失败: gradeId={}", id, e);
            return ApiResponse.error("删除成绩失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生成绩列表
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "获取学生成绩", description = "获取指定学生的所有成绩")
    public ApiResponse<List<Grade>> getGradesByStudent(@PathVariable Long studentId) {
        try {
            // 如果是学生，只能查看自己的成绩
            Long currentUserId = getCurrentUserId();
            if (hasRole("STUDENT") && !currentUserId.equals(studentId)) {
                return ApiResponse.error("只能查看自己的成绩");
            }
            
            List<Grade> grades = gradeService.getGradesByStudent(studentId);
            return ApiResponse.success("获取学生成绩成功", grades);
        } catch (Exception e) {
            logger.error("获取学生成绩失败: studentId={}", studentId, e);
            return ApiResponse.error("获取学生成绩失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取学生成绩
     */
    @GetMapping("/student/{studentId}/page")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "分页获取学生成绩", description = "分页获取指定学生的成绩列表")
    public ApiResponse<PageResult<Grade>> getStudentGradesWithPagination(
            @PathVariable Long studentId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size) {
        try {
            // 权限检查
            Long currentUserId = getCurrentUserId();
            if (hasRole("STUDENT") && !currentUserId.equals(studentId)) {
                return ApiResponse.error("只能查看自己的成绩");
            }
            
            PageResult<Grade> result = gradeService.getStudentGradesWithPagination(studentId, page, size);
            return ApiResponse.success("获取学生成绩列表成功", result);
        } catch (Exception e) {
            logger.error("分页获取学生成绩失败: studentId={}, page={}, size={}", studentId, page, size, e);
            return ApiResponse.error("获取学生成绩列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取作业成绩列表
     */
    @GetMapping("/assignment/{assignmentId}")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "获取作业成绩", description = "获取指定作业的所有成绩")
    public ApiResponse<List<Grade>> getGradesByAssignment(@PathVariable Long assignmentId) {
        try {
            List<Grade> grades = gradeService.getGradesByAssignment(assignmentId);
            return ApiResponse.success("获取作业成绩成功", grades);
        } catch (Exception e) {
            logger.error("获取作业成绩失败: assignmentId={}", assignmentId, e);
            return ApiResponse.error("获取作业成绩失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取作业成绩
     */
    @GetMapping("/assignment/{assignmentId}/page")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "分页获取作业成绩", description = "分页获取指定作业的成绩列表")
    public ApiResponse<PageResult<Grade>> getAssignmentGradesWithPagination(
            @PathVariable Long assignmentId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size) {
        try {
            PageResult<Grade> result = gradeService.getAssignmentGradesWithPagination(assignmentId, page, size);
            return ApiResponse.success("获取作业成绩列表成功", result);
        } catch (Exception e) {
            logger.error("分页获取作业成绩失败: assignmentId={}, page={}, size={}", assignmentId, page, size, e);
            return ApiResponse.error("获取作业成绩列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生作业成绩
     */
    @GetMapping("/student/{studentId}/assignment/{assignmentId}")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "获取学生作业成绩", description = "获取指定学生在指定作业的成绩")
    public ApiResponse<Grade> getStudentAssignmentGrade(
            @PathVariable Long studentId,
            @PathVariable Long assignmentId) {
        try {
            // 权限检查
            Long currentUserId = getCurrentUserId();
            if (hasRole("STUDENT") && !currentUserId.equals(studentId)) {
                return ApiResponse.error("只能查看自己的成绩");
            }
            
            Grade grade = gradeService.getStudentAssignmentGrade(studentId, assignmentId);
            return ApiResponse.success("获取学生作业成绩成功", grade);
        } catch (Exception e) {
            logger.error("获取学生作业成绩失败: studentId={}, assignmentId={}", studentId, assignmentId, e);
            return ApiResponse.error("获取学生作业成绩失败: " + e.getMessage());
        }
    }

    /**
     * 批量评分
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "批量评分", description = "批量为学生作业评分")
    public ApiResponse<Map<String, Object>> batchGrading(@RequestBody List<Grade> grades) {
        try {
            Map<String, Object> result = gradeService.batchGrading(grades);
            return ApiResponse.success("批量评分完成", result);
        } catch (Exception e) {
            logger.error("批量评分失败", e);
            return ApiResponse.error("批量评分失败: " + e.getMessage());
        }
    }

    /**
     * 计算学生平均分
     */
    @GetMapping("/student/{studentId}/average")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "计算学生平均分", description = "计算指定学生的平均分")
    public ApiResponse<BigDecimal> calculateStudentAverageScore(@PathVariable Long studentId) {
        try {
            // 权限检查
            Long currentUserId = getCurrentUserId();
            if (hasRole("STUDENT") && !currentUserId.equals(studentId)) {
                return ApiResponse.error("只能查看自己的成绩");
            }
            
            BigDecimal average = gradeService.calculateStudentAverageScore(studentId);
            return ApiResponse.success("计算学生平均分成功", average);
        } catch (Exception e) {
            logger.error("计算学生平均分失败: studentId={}", studentId, e);
            return ApiResponse.error("计算学生平均分失败: " + e.getMessage());
        }
    }

    /**
     * 计算学生课程平均分
     */
    @GetMapping("/student/{studentId}/course/{courseId}/average")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "计算课程平均分", description = "计算学生在指定课程的平均分")
    public ApiResponse<BigDecimal> calculateStudentCourseAverageScore(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        try {
            // 权限检查
            Long currentUserId = getCurrentUserId();
            if (hasRole("STUDENT") && !currentUserId.equals(studentId)) {
                return ApiResponse.error("只能查看自己的成绩");
            }
            
            BigDecimal average = gradeService.calculateStudentCourseAverageScore(studentId, courseId);
            return ApiResponse.success("计算课程平均分成功", average);
        } catch (Exception e) {
            logger.error("计算课程平均分失败: studentId={}, courseId={}", studentId, courseId, e);
            return ApiResponse.error("计算课程平均分失败: " + e.getMessage());
        }
    }

    /**
     * 计算作业平均分
     */
    @GetMapping("/assignment/{assignmentId}/average")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "计算作业平均分", description = "计算指定作业的平均分")
    public ApiResponse<BigDecimal> calculateAssignmentAverageScore(@PathVariable Long assignmentId) {
        try {
            BigDecimal average = gradeService.calculateAssignmentAverageScore(assignmentId);
            return ApiResponse.success("计算作业平均分成功", average);
        } catch (Exception e) {
            logger.error("计算作业平均分失败: assignmentId={}", assignmentId, e);
            return ApiResponse.error("计算作业平均分失败: " + e.getMessage());
        }
    }

    /**
     * 获取作业成绩分布
     */
    @GetMapping("/assignment/{assignmentId}/distribution")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "获取成绩分布", description = "获取指定作业的成绩分布统计")
    public ApiResponse<List<Map<String, Object>>> getGradeDistribution(@PathVariable Long assignmentId) {
        try {
            List<Map<String, Object>> distribution = gradeService.getGradeDistribution(assignmentId);
            return ApiResponse.success("获取成绩分布成功", distribution);
        } catch (Exception e) {
            logger.error("获取成绩分布失败: assignmentId={}", assignmentId, e);
            return ApiResponse.error("获取成绩分布失败: " + e.getMessage());
        }
    }

    /**
     * 获取课程成绩统计
     */
    @GetMapping("/course/{courseId}/statistics")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "获取课程成绩统计", description = "获取指定课程的成绩统计信息")
    public ApiResponse<Map<String, Object>> getCourseGradeStatistics(@PathVariable Long courseId) {
        try {
            Map<String, Object> statistics = gradeService.getCourseGradeStatistics(courseId);
            return ApiResponse.success("获取课程成绩统计成功", statistics);
        } catch (Exception e) {
            logger.error("获取课程成绩统计失败: courseId={}", courseId, e);
            return ApiResponse.error("获取课程成绩统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生成绩统计
     */
    @GetMapping("/student/{studentId}/statistics")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "获取学生成绩统计", description = "获取指定学生的成绩统计信息")
    public ApiResponse<Map<String, Object>> getStudentGradeStatistics(@PathVariable Long studentId) {
        try {
            // 权限检查
            Long currentUserId = getCurrentUserId();
            if (hasRole("STUDENT") && !currentUserId.equals(studentId)) {
                return ApiResponse.error("只能查看自己的成绩");
            }
            
            Map<String, Object> statistics = gradeService.getStudentGradeStatistics(studentId);
            return ApiResponse.success("获取学生成绩统计成功", statistics);
        } catch (Exception e) {
            logger.error("获取学生成绩统计失败: studentId={}", studentId, e);
            return ApiResponse.error("获取学生成绩统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取待评分作业
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "获取待评分作业", description = "获取教师待评分的作业列表")
    public ApiResponse<PageResult<Map<String, Object>>> getPendingGrades(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size) {
        try {
            Long teacherId = getCurrentUserId();
            PageResult<Map<String, Object>> result = gradeService.getPendingGrades(teacherId, page, size);
            return ApiResponse.success("获取待评分作业成功", result);
        } catch (Exception e) {
            logger.error("获取待评分作业失败", e);
            return ApiResponse.error("获取待评分作业失败: " + e.getMessage());
        }
    }

    /**
     * 发布成绩
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "发布成绩", description = "发布指定成绩")
    public ApiResponse<Void> publishGrade(@PathVariable Long id) {
        try {
            // 检查权限
            Long currentUserId = getCurrentUserId();
            if (!gradeService.canModifyGrade(id, currentUserId)) {
                return ApiResponse.error("没有权限发布该成绩");
            }
            
            boolean success = gradeService.publishGrade(id);
            if (success) {
                return ApiResponse.success("成绩发布成功");
            } else {
                return ApiResponse.error("成绩发布失败");
            }
        } catch (Exception e) {
            logger.error("发布成绩失败: gradeId={}", id, e);
            return ApiResponse.error("发布成绩失败: " + e.getMessage());
        }
    }

    /**
     * 批量发布成绩
     */
    @PostMapping("/batch-publish")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "批量发布成绩", description = "批量发布多个成绩")
    public ApiResponse<Map<String, Object>> batchPublishGrades(@RequestBody List<Long> gradeIds) {
        try {
            Map<String, Object> result = gradeService.batchPublishGrades(gradeIds);
            return ApiResponse.success("批量发布成绩完成", result);
        } catch (Exception e) {
            logger.error("批量发布成绩失败", e);
            return ApiResponse.error("批量发布成绩失败: " + e.getMessage());
        }
    }

    /**
     * 获取成绩排名
     */
    @GetMapping("/ranking")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "获取成绩排名", description = "获取成绩排名列表")
    public ApiResponse<List<Map<String, Object>>> getGradeRanking(
            @Parameter(description = "课程ID", required = true) @RequestParam Long courseId,
            @Parameter(description = "作业ID") @RequestParam(required = false) Long assignmentId) {
        try {
            List<Map<String, Object>> ranking = gradeService.getGradeRanking(courseId, assignmentId);
            return ApiResponse.success("获取成绩排名成功", ranking);
        } catch (Exception e) {
            logger.error("获取成绩排名失败: courseId={}, assignmentId={}", courseId, assignmentId, e);
            return ApiResponse.error("获取成绩排名失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生排名
     */
    @GetMapping("/student/{studentId}/ranking")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "获取学生排名", description = "获取指定学生的排名信息")
    public ApiResponse<Map<String, Object>> getStudentRanking(
            @PathVariable Long studentId,
            @Parameter(description = "课程ID", required = true) @RequestParam Long courseId) {
        try {
            // 权限检查
            Long currentUserId = getCurrentUserId();
            if (hasRole("STUDENT") && !currentUserId.equals(studentId)) {
                return ApiResponse.error("只能查看自己的排名");
            }
            
            Map<String, Object> ranking = gradeService.getStudentRanking(studentId, courseId);
            return ApiResponse.success("获取学生排名成功", ranking);
        } catch (Exception e) {
            logger.error("获取学生排名失败: studentId={}, courseId={}", studentId, courseId, e);
            return ApiResponse.error("获取学生排名失败: " + e.getMessage());
        }
    }

    /**
     * 导出成绩数据
     */
    @PostMapping("/export")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "导出成绩", description = "导出成绩数据")
    public ApiResponse<Map<String, Object>> exportGrades(@RequestBody Map<String, Object> exportRequest) {
        try {
            Long courseId = exportRequest.get("courseId") != null ? 
                Long.valueOf(exportRequest.get("courseId").toString()) : null;
            Long assignmentId = exportRequest.get("assignmentId") != null ? 
                Long.valueOf(exportRequest.get("assignmentId").toString()) : null;
            String format = (String) exportRequest.getOrDefault("format", "excel");
            
            Map<String, Object> result = gradeService.exportGrades(courseId, assignmentId, format);
            return ApiResponse.success("成绩导出成功", result);
        } catch (Exception e) {
            logger.error("导出成绩失败", e);
            return ApiResponse.error("导出成绩失败: " + e.getMessage());
        }
    }

    /**
     * 获取成绩趋势
     */
    @GetMapping("/student/{studentId}/trend")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "获取成绩趋势", description = "获取学生成绩趋势数据")
    public ApiResponse<List<Map<String, Object>>> getGradeTrend(
            @PathVariable Long studentId,
            @Parameter(description = "课程ID") @RequestParam(required = false) Long courseId,
            @Parameter(description = "统计天数", example = "30") @RequestParam(defaultValue = "30") Integer days) {
        try {
            // 权限检查
            Long currentUserId = getCurrentUserId();
            if (hasRole("STUDENT") && !currentUserId.equals(studentId)) {
                return ApiResponse.error("只能查看自己的成绩趋势");
            }
            
            List<Map<String, Object>> trend = gradeService.getGradeTrend(studentId, courseId, days);
            return ApiResponse.success("获取成绩趋势成功", trend);
        } catch (Exception e) {
            logger.error("获取成绩趋势失败: studentId={}, courseId={}, days={}", studentId, courseId, days, e);
            return ApiResponse.error("获取成绩趋势失败: " + e.getMessage());
        }
    }

    /**
     * 添加成绩评语
     */
    @PostMapping("/{id}/feedback")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "添加成绩评语", description = "为指定成绩添加评语")
    public ApiResponse<Void> addGradeFeedback(
            @PathVariable Long id,
            @RequestBody Map<String, String> feedbackData) {
        try {
            // 检查权限
            Long currentUserId = getCurrentUserId();
            if (!gradeService.canModifyGrade(id, currentUserId)) {
                return ApiResponse.error("没有权限修改该成绩");
            }
            
            String feedback = feedbackData.get("feedback");
            if (feedback == null || feedback.trim().isEmpty()) {
                return ApiResponse.error("评语内容不能为空");
            }
            
            boolean success = gradeService.addGradeFeedback(id, feedback);
            if (success) {
                return ApiResponse.success("添加成绩评语成功");
            } else {
                return ApiResponse.error("添加成绩评语失败");
            }
        } catch (Exception e) {
            logger.error("添加成绩评语失败: gradeId={}", id, e);
            return ApiResponse.error("添加成绩评语失败: " + e.getMessage());
        }
    }

    /**
     * 重新评分
     */
    @PostMapping("/{id}/regrade")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "重新评分", description = "重新为指定成绩评分")
    public ApiResponse<Void> regrade(
            @PathVariable Long id,
            @RequestBody Map<String, Object> regradeData) {
        try {
            // 检查权限
            Long currentUserId = getCurrentUserId();
            if (!gradeService.canModifyGrade(id, currentUserId)) {
                return ApiResponse.error("没有权限修改该成绩");
            }
            
            BigDecimal newScore = regradeData.get("newScore") != null ? 
                new BigDecimal(regradeData.get("newScore").toString()) : null;
            String reason = (String) regradeData.get("reason");
            
            if (newScore == null) {
                return ApiResponse.error("新分数不能为空");
            }
            
            boolean success = gradeService.regrade(id, newScore, reason);
            if (success) {
                return ApiResponse.success("重新评分成功");
            } else {
                return ApiResponse.error("重新评分失败");
            }
        } catch (Exception e) {
            logger.error("重新评分失败: gradeId={}", id, e);
            return ApiResponse.error("重新评分失败: " + e.getMessage());
        }
    }

    /**
     * 获取成绩历史记录
     */
    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "获取成绩历史", description = "获取指定成绩的历史记录")
    public ApiResponse<List<Map<String, Object>>> getGradeHistory(@PathVariable Long id) {
        try {
            List<Map<String, Object>> history = gradeService.getGradeHistory(id);
            return ApiResponse.success("获取成绩历史记录成功", history);
        } catch (Exception e) {
            logger.error("获取成绩历史记录失败: gradeId={}", id, e);
            return ApiResponse.error("获取成绩历史记录失败: " + e.getMessage());
        }
    }

    /**
     * 计算GPA
     */
    @GetMapping("/student/{studentId}/gpa")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "计算GPA", description = "计算学生的GPA")
    public ApiResponse<BigDecimal> calculateGPA(
            @PathVariable Long studentId,
            @Parameter(description = "课程ID") @RequestParam(required = false) Long courseId) {
        try {
            // 权限检查
            Long currentUserId = getCurrentUserId();
            if (hasRole("STUDENT") && !currentUserId.equals(studentId)) {
                return ApiResponse.error("只能查看自己的GPA");
            }
            
            BigDecimal gpa = gradeService.calculateGPA(studentId, courseId);
            return ApiResponse.success("计算GPA成功", gpa);
        } catch (Exception e) {
            logger.error("计算GPA失败: studentId={}, courseId={}", studentId, courseId, e);
            return ApiResponse.error("计算GPA失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户ID的辅助方法
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
                    throw new BusinessException(ErrorCode.USER_NOT_FOUND, "无法获取当前用户信息");
    }

    /**
     * 检查用户角色的辅助方法
     */
    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
        }
        return false;
    }
} 