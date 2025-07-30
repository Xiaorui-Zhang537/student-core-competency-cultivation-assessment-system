package com.noncore.assessment.controller;

import com.noncore.assessment.dto.response.StudentDashboardResponse;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.entity.LessonProgress;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.StudentService;
import com.noncore.assessment.util.ApiResponse;
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
 * 学生控制器
 * 提供学生相关的API接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@RestController
@RequestMapping("/api/student")
@Tag(name = "学生管理", description = "学生仪表板、课程、成绩等相关接口")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * 获取学生仪表板数据
     */
    @GetMapping("/dashboard")
    @Operation(summary = "获取学生仪表板数据", description = "获取学生的综合仪表板信息，包括统计数据、最近课程、待完成作业等")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<StudentDashboardResponse> getDashboard() {
        try {
            Long studentId = getCurrentUserId();
            StudentDashboardResponse dashboard = studentService.getDashboardData(studentId);
            return ApiResponse.success("获取学生仪表板数据成功", dashboard);
        } catch (Exception e) {
            logger.error("获取学生仪表板数据失败", e);
            return ApiResponse.error("获取学生仪表板数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生个人资料
     */
    @GetMapping("/profile")
    @Operation(summary = "获取学生个人资料", description = "获取当前登录学生的个人资料信息")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<User> getProfile() {
        try {
            Long studentId = getCurrentUserId();
            User profile = studentService.getStudentProfile(studentId);
            return ApiResponse.success("获取学生个人资料成功", profile);
        } catch (Exception e) {
            logger.error("获取学生个人资料失败", e);
            return ApiResponse.error("获取学生个人资料失败: " + e.getMessage());
        }
    }

    /**
     * 更新学生个人资料
     */
    @PutMapping("/profile")
    @Operation(summary = "更新学生个人资料", description = "更新当前登录学生的个人资料信息")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<User> updateProfile(@Valid @RequestBody User profile) {
        try {
            Long studentId = getCurrentUserId();
            User updatedProfile = studentService.updateStudentProfile(studentId, profile);
            return ApiResponse.success("更新学生个人资料成功", updatedProfile);
        } catch (Exception e) {
            logger.error("更新学生个人资料失败", e);
            return ApiResponse.error("更新学生个人资料失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生课程列表
     */
    @GetMapping("/courses")
    @Operation(summary = "获取学生课程列表", description = "获取当前学生已注册的课程列表")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<List<Course>> getCourses() {
        try {
            Long studentId = getCurrentUserId();
            List<Course> courses = studentService.getStudentCourses(studentId);
            return ApiResponse.success("获取学生课程列表成功", courses);
        } catch (Exception e) {
            logger.error("获取学生课程列表失败", e);
            return ApiResponse.error("获取学生课程列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生作业列表
     */
    @GetMapping("/assignments")
    @Operation(summary = "获取学生作业列表", description = "获取学生的作业列表，支持按课程筛选")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<List<Assignment>> getAssignments(
            @Parameter(description = "课程ID", example = "1") @RequestParam(required = false) Long courseId) {
        try {
            Long studentId = getCurrentUserId();
            List<Assignment> assignments = studentService.getStudentAssignments(studentId, courseId);
            return ApiResponse.success("获取学生作业列表成功", assignments);
        } catch (Exception e) {
            logger.error("获取学生作业列表失败", e);
            return ApiResponse.error("获取学生作业列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取待完成作业列表
     */
    @GetMapping("/assignments/pending")
    @Operation(summary = "获取待完成作业列表", description = "获取学生待完成的作业列表")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<List<Assignment>> getPendingAssignments() {
        try {
            Long studentId = getCurrentUserId();
            List<Assignment> assignments = studentService.getPendingAssignments(studentId);
            return ApiResponse.success("获取待完成作业列表成功", assignments);
        } catch (Exception e) {
            logger.error("获取待完成作业列表失败", e);
            return ApiResponse.error("获取待完成作业列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取学习进度
     */
    @GetMapping("/courses/{courseId}/progress")
    @Operation(summary = "获取课程学习进度", description = "获取学生在指定课程中的学习进度")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<List<LessonProgress>> getProgress(
            @Parameter(description = "课程ID", example = "1") @PathVariable Long courseId) {
        try {
            Long studentId = getCurrentUserId();
            
            // 检查权限
            if (!studentService.hasAccessToCourse(studentId, courseId)) {
                return ApiResponse.error("您没有访问该课程的权限");
            }
            
            List<LessonProgress> progress = studentService.getStudentProgress(studentId, courseId);
            return ApiResponse.success("获取学习进度成功", progress);
        } catch (Exception e) {
            logger.error("获取学习进度失败", e);
            return ApiResponse.error("获取学习进度失败: " + e.getMessage());
        }
    }

    /**
     * 更新学习进度
     */
    @PutMapping("/courses/{courseId}/lessons/{lessonId}/progress")
    @Operation(summary = "更新学习进度", description = "更新学生在指定章节的学习进度")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<Void> updateProgress(
            @Parameter(description = "课程ID", example = "1") @PathVariable Long courseId,
            @Parameter(description = "章节ID", example = "1") @PathVariable Long lessonId,
            @RequestBody Map<String, Object> progressData) {
        try {
            Long studentId = getCurrentUserId();
            
            // 检查权限
            if (!studentService.hasAccessToCourse(studentId, courseId)) {
                return ApiResponse.error("您没有访问该课程的权限");
            }
            
            BigDecimal progress = new BigDecimal(progressData.get("progress").toString());
            Integer lastPosition = progressData.get("lastPosition") != null ? 
                (Integer) progressData.get("lastPosition") : 0;
            
            boolean success = studentService.updateLearningProgress(studentId, courseId, lessonId, progress, lastPosition);
            
            if (success) {
                return ApiResponse.success("更新学习进度成功");
            } else {
                return ApiResponse.error("更新学习进度失败");
            }
        } catch (Exception e) {
            logger.error("更新学习进度失败", e);
            return ApiResponse.error("更新学习进度失败: " + e.getMessage());
        }
    }

    /**
     * 标记章节完成
     */
    @PostMapping("/courses/{courseId}/lessons/{lessonId}/complete")
    @Operation(summary = "标记章节完成", description = "标记学生已完成指定章节的学习")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<Void> completeLesson(
            @Parameter(description = "课程ID", example = "1") @PathVariable Long courseId,
            @Parameter(description = "章节ID", example = "1") @PathVariable Long lessonId) {
        try {
            Long studentId = getCurrentUserId();
            
            // 检查权限
            if (!studentService.hasAccessToCourse(studentId, courseId)) {
                return ApiResponse.error("您没有访问该课程的权限");
            }
            
            boolean success = studentService.markLessonCompleted(studentId, courseId, lessonId);
            
            if (success) {
                return ApiResponse.success("标记章节完成成功");
            } else {
                return ApiResponse.error("标记章节完成失败");
            }
        } catch (Exception e) {
            logger.error("标记章节完成失败", e);
            return ApiResponse.error("标记章节完成失败: " + e.getMessage());
        }
    }

    /**
     * 获取学习统计
     */
    @GetMapping("/stats")
    @Operation(summary = "获取学习统计", description = "获取学生的学习统计数据")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<Map<String, Object>> getStats() {
        try {
            Long studentId = getCurrentUserId();
            Map<String, Object> stats = studentService.getStudentStats(studentId);
            return ApiResponse.success("获取学习统计成功", stats);
        } catch (Exception e) {
            logger.error("获取学习统计失败", e);
            return ApiResponse.error("获取学习统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取平均成绩
     */
    @GetMapping("/grade/average")
    @Operation(summary = "获取平均成绩", description = "获取学生的平均成绩")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<BigDecimal> getAverageGrade(
            @Parameter(description = "课程ID（可选）", example = "1") @RequestParam(required = false) Long courseId) {
        try {
            Long studentId = getCurrentUserId();
            
            BigDecimal averageGrade;
            if (courseId != null) {
                // 检查权限
                if (!studentService.hasAccessToCourse(studentId, courseId)) {
                    return ApiResponse.error("您没有访问该课程的权限");
                }
                averageGrade = studentService.calculateCourseAverageGrade(studentId, courseId);
            } else {
                averageGrade = studentService.calculateAverageGrade(studentId);
            }
            
            return ApiResponse.success("获取平均成绩成功", averageGrade);
        } catch (Exception e) {
            logger.error("获取平均成绩失败", e);
            return ApiResponse.error("获取平均成绩失败: " + e.getMessage());
        }
    }

    /**
     * 获取课程完成度
     */
    @GetMapping("/courses/{courseId}/completion")
    @Operation(summary = "获取课程完成度", description = "获取学生在指定课程的完成度")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<BigDecimal> getCourseCompletion(
            @Parameter(description = "课程ID", example = "1") @PathVariable Long courseId) {
        try {
            Long studentId = getCurrentUserId();
            
            // 检查权限
            if (!studentService.hasAccessToCourse(studentId, courseId)) {
                return ApiResponse.error("您没有访问该课程的权限");
            }
            
            BigDecimal completion = studentService.getCourseCompletionRate(studentId, courseId);
            return ApiResponse.success("获取课程完成度成功", completion);
        } catch (Exception e) {
            logger.error("获取课程完成度失败", e);
            return ApiResponse.error("获取课程完成度失败: " + e.getMessage());
        }
    }

    /**
     * 添加学习笔记
     */
    @PostMapping("/lessons/{lessonId}/notes")
    @Operation(summary = "添加学习笔记", description = "为指定章节添加学习笔记")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<Void> addNotes(
            @Parameter(description = "章节ID", example = "1") @PathVariable Long lessonId,
            @RequestBody Map<String, String> noteData) {
        try {
            Long studentId = getCurrentUserId();
            
            // 检查权限
            if (!studentService.hasAccessToLesson(studentId, lessonId)) {
                return ApiResponse.error("您没有访问该章节的权限");
            }
            
            String notes = noteData.get("notes");
            boolean success = studentService.addStudyNotes(studentId, lessonId, notes);
            
            if (success) {
                return ApiResponse.success("添加学习笔记成功");
            } else {
                return ApiResponse.error("添加学习笔记失败");
            }
        } catch (Exception e) {
            logger.error("添加学习笔记失败", e);
            return ApiResponse.error("添加学习笔记失败: " + e.getMessage());
        }
    }

    /**
     * 章节评分
     */
    @PostMapping("/lessons/{lessonId}/rating")
    @Operation(summary = "章节评分", description = "为指定章节进行评分")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<Void> rateLesson(
            @Parameter(description = "章节ID", example = "1") @PathVariable Long lessonId,
            @RequestBody Map<String, Integer> ratingData) {
        try {
            Long studentId = getCurrentUserId();
            
            // 检查权限
            if (!studentService.hasAccessToLesson(studentId, lessonId)) {
                return ApiResponse.error("您没有访问该章节的权限");
            }
            
            Integer rating = ratingData.get("rating");
            if (rating == null || rating < 1 || rating > 5) {
                return ApiResponse.error("评分必须在1-5之间");
            }
            
            boolean success = studentService.rateLesion(studentId, lessonId, rating);
            
            if (success) {
                return ApiResponse.success("章节评分成功");
            } else {
                return ApiResponse.error("章节评分失败");
            }
        } catch (Exception e) {
            logger.error("章节评分失败", e);
            return ApiResponse.error("章节评分失败: " + e.getMessage());
        }
    }

    /**
     * 获取学习排名
     */
    @GetMapping("/ranking")
    @Operation(summary = "获取学习排名", description = "获取学生的学习排名信息")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<Map<String, Object>> getRanking(
            @Parameter(description = "课程ID（可选）", example = "1") @RequestParam(required = false) Long courseId) {
        try {
            Long studentId = getCurrentUserId();
            
            if (courseId != null && !studentService.hasAccessToCourse(studentId, courseId)) {
                return ApiResponse.error("您没有访问该课程的权限");
            }
            
            Map<String, Object> ranking = studentService.getStudentRanking(studentId, courseId);
            return ApiResponse.success("获取学习排名成功", ranking);
        } catch (Exception e) {
            logger.error("获取学习排名失败", e);
            return ApiResponse.error("获取学习排名失败: " + e.getMessage());
        }
    }

    /**
     * 获取学习热力图数据
     */
    @GetMapping("/heatmap")
    @Operation(summary = "获取学习热力图数据", description = "获取学生的学习热力图数据")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<List<Map<String, Object>>> getHeatmapData(
            @Parameter(description = "查询天数", example = "30") @RequestParam(defaultValue = "30") Integer days) {
        try {
            Long studentId = getCurrentUserId();
            List<Map<String, Object>> heatmapData = studentService.getStudyHeatmapData(studentId, days);
            return ApiResponse.success("获取学习热力图数据成功", heatmapData);
        } catch (Exception e) {
            logger.error("获取学习热力图数据失败", e);
            return ApiResponse.error("获取学习热力图数据失败: " + e.getMessage());
        }
    }

    /**
     * 重置课程进度
     */
    @DeleteMapping("/courses/{courseId}/progress")
    @Operation(summary = "重置课程进度", description = "重置学生在指定课程的学习进度")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<Void> resetProgress(
            @Parameter(description = "课程ID", example = "1") @PathVariable Long courseId) {
        try {
            Long studentId = getCurrentUserId();
            
            // 检查权限
            if (!studentService.hasAccessToCourse(studentId, courseId)) {
                return ApiResponse.error("您没有访问该课程的权限");
            }
            
            boolean success = studentService.resetCourseProgress(studentId, courseId);
            
            if (success) {
                return ApiResponse.success("重置课程进度成功");
            } else {
                return ApiResponse.error("重置课程进度失败");
            }
        } catch (Exception e) {
            logger.error("重置课程进度失败", e);
            return ApiResponse.error("重置课程进度失败: " + e.getMessage());
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
        
        // 如果无法获取用户ID，抛出异常
                    throw new BusinessException(ErrorCode.USER_NOT_FOUND, "无法获取当前用户信息");
    }
} 