package com.noncore.assessment.controller;

import com.noncore.assessment.dto.response.TeacherDashboardResponse;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.Grade;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.TeacherService;
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

import java.util.List;
import java.util.Map;

/**
 * 教师控制器
 * 提供教师相关的API接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@RestController
@RequestMapping("/api/teacher")
@Tag(name = "教师管理", description = "教师仪表板、课程管理、学生管理等相关接口")
public class TeacherController {

    private static final Logger logger = LoggerFactory.getLogger(TeacherController.class);

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    /**
     * 获取教师仪表板数据
     */
    @GetMapping("/dashboard")
    @Operation(summary = "获取教师仪表板数据", description = "获取教师的综合仪表板信息，包括统计数据、活跃课程、待评分作业等")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<TeacherDashboardResponse> getDashboard() {
        try {
            Long teacherId = getCurrentUserId();
            TeacherDashboardResponse dashboard = teacherService.getDashboardData(teacherId);
            return ApiResponse.success("获取教师仪表板数据成功", dashboard);
        } catch (Exception e) {
            logger.error("获取教师仪表板数据失败", e);
            return ApiResponse.error("获取教师仪表板数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取教师统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取教师统计数据", description = "获取教师的统计数据，包括课程数、学生数、作业数等")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Map<String, Object>> getStats() {
        try {
            Long teacherId = getCurrentUserId();
            Map<String, Object> stats = teacherService.getTeacherStats(teacherId);
            return ApiResponse.success("获取教师统计数据成功", stats);
        } catch (Exception e) {
            logger.error("获取教师统计数据失败", e);
            return ApiResponse.error("获取教师统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取教师个人资料
     */
    @GetMapping("/profile")
    @Operation(summary = "获取教师个人资料", description = "获取当前登录教师的个人资料信息")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<User> getProfile() {
        try {
            Long teacherId = getCurrentUserId();
            User profile = teacherService.getTeacherProfile(teacherId);
            return ApiResponse.success("获取教师个人资料成功", profile);
        } catch (Exception e) {
            logger.error("获取教师个人资料失败", e);
            return ApiResponse.error("获取教师个人资料失败: " + e.getMessage());
        }
    }

    /**
     * 更新教师个人资料
     */
    @PutMapping("/profile")
    @Operation(summary = "更新教师个人资料", description = "更新当前登录教师的个人资料信息")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<User> updateProfile(@Valid @RequestBody User profile) {
        try {
            Long teacherId = getCurrentUserId();
            User updatedProfile = teacherService.updateTeacherProfile(teacherId, profile);
            return ApiResponse.success("更新教师个人资料成功", updatedProfile);
        } catch (Exception e) {
            logger.error("更新教师个人资料失败", e);
            return ApiResponse.error("更新教师个人资料失败: " + e.getMessage());
        }
    }

    /**
     * 获取教师课程列表
     */
    @GetMapping("/courses")
    @Operation(summary = "获取教师课程列表", description = "获取当前教师的所有课程列表")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<List<Course>> getCourses() {
        try {
            Long teacherId = getCurrentUserId();
            List<Course> courses = teacherService.getTeacherCourses(teacherId);
            return ApiResponse.success("获取教师课程列表成功", courses);
        } catch (Exception e) {
            logger.error("获取教师课程列表失败", e);
            return ApiResponse.error("获取教师课程列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生概览列表
     */
    @GetMapping("/students")
    @Operation(summary = "获取学生概览列表", description = "获取教师管理的学生概览信息，支持按课程和状态筛选")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<PageResult<User>> getStudents(
            @Parameter(description = "课程ID", example = "1") @RequestParam(required = false) Long courseId,
            @Parameter(description = "学生状态", example = "active") @RequestParam(required = false) String status,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size) {
        try {
            Long teacherId = getCurrentUserId();
            PageResult<User> students = teacherService.getStudentOverview(teacherId, courseId, status, page, size);
            return ApiResponse.success("获取学生概览列表成功", students);
        } catch (Exception e) {
            logger.error("获取学生概览列表失败", e);
            return ApiResponse.error("获取学生概览列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生进度报告
     */
    @GetMapping("/students/{studentId}/progress")
    @Operation(summary = "获取学生进度报告", description = "获取指定学生的详细学习进度报告")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Map<String, Object>> getStudentProgress(
            @Parameter(description = "学生ID", example = "1") @PathVariable Long studentId,
            @Parameter(description = "课程ID", example = "1") @RequestParam(required = false) Long courseId) {
        try {
            Long teacherId = getCurrentUserId();
            Map<String, Object> progressReport = teacherService.getStudentProgressReport(teacherId, studentId, courseId);
            return ApiResponse.success("获取学生进度报告成功", progressReport);
        } catch (Exception e) {
            logger.error("获取学生进度报告失败", e);
            return ApiResponse.error("获取学生进度报告失败: " + e.getMessage());
        }
    }

    /**
     * 获取课程分析数据
     */
    @GetMapping("/courses/{courseId}/analytics")
    @Operation(summary = "获取课程分析数据", description = "获取指定课程的详细分析数据")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Map<String, Object>> getCourseAnalytics(
            @Parameter(description = "课程ID", example = "1") @PathVariable Long courseId,
            @Parameter(description = "时间范围", example = "1month") @RequestParam(defaultValue = "1month") String timeRange) {
        try {
            Long teacherId = getCurrentUserId();
            Map<String, Object> analytics = teacherService.getCourseAnalytics(teacherId, courseId, timeRange);
            return ApiResponse.success("获取课程分析数据成功", analytics);
        } catch (Exception e) {
            logger.error("获取课程分析数据失败", e);
            return ApiResponse.error("获取课程分析数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取班级表现数据
     */
    @GetMapping("/courses/{courseId}/performance")
    @Operation(summary = "获取班级表现数据", description = "获取指定课程的班级整体表现数据")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Map<String, Object>> getClassPerformance(
            @Parameter(description = "课程ID", example = "1") @PathVariable Long courseId,
            @Parameter(description = "时间范围", example = "1month") @RequestParam(defaultValue = "1month") String timeRange) {
        try {
            Long teacherId = getCurrentUserId();
            Map<String, Object> performance = teacherService.getClassPerformance(teacherId, courseId, timeRange);
            return ApiResponse.success("获取班级表现数据成功", performance);
        } catch (Exception e) {
            logger.error("获取班级表现数据失败", e);
            return ApiResponse.error("获取班级表现数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取课程管理数据
     */
    @GetMapping("/courses/management")
    @Operation(summary = "获取课程管理数据", description = "获取教师所有课程的管理数据")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<List<Map<String, Object>>> getCourseManagement() {
        try {
            Long teacherId = getCurrentUserId();
            List<Map<String, Object>> managementData = teacherService.getCourseManagementData(teacherId);
            return ApiResponse.success("获取课程管理数据成功", managementData);
        } catch (Exception e) {
            logger.error("获取课程管理数据失败", e);
            return ApiResponse.error("获取课程管理数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取作业分析数据
     */
    @GetMapping("/assignments/{assignmentId}/analytics")
    @Operation(summary = "获取作业分析数据", description = "获取指定作业的详细分析数据")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Map<String, Object>> getAssignmentAnalytics(
            @Parameter(description = "作业ID", example = "1") @PathVariable Long assignmentId) {
        try {
            Long teacherId = getCurrentUserId();
            Map<String, Object> analytics = teacherService.getAssignmentAnalytics(teacherId, assignmentId);
            return ApiResponse.success("获取作业分析数据成功", analytics);
        } catch (Exception e) {
            logger.error("获取作业分析数据失败", e);
            return ApiResponse.error("获取作业分析数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取待评分作业列表
     */
    @GetMapping("/gradings/pending")
    @Operation(summary = "获取待评分作业列表", description = "获取教师待评分的作业列表")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<List<Map<String, Object>>> getPendingGradings(
            @Parameter(description = "课程ID", example = "1") @RequestParam(required = false) Long courseId) {
        try {
            Long teacherId = getCurrentUserId();
            List<Map<String, Object>> pendingGradings = teacherService.getPendingGradings(teacherId, courseId);
            return ApiResponse.success("获取待评分作业列表成功", pendingGradings);
        } catch (Exception e) {
            logger.error("获取待评分作业列表失败", e);
            return ApiResponse.error("获取待评分作业列表失败: " + e.getMessage());
        }
    }

    /**
     * 批量评分作业
     */
    @PostMapping("/gradings/batch")
    @Operation(summary = "批量评分作业", description = "批量评分多个作业")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Map<String, Object>> batchGradeAssignments(@Valid @RequestBody List<Grade> grades) {
        try {
            Long teacherId = getCurrentUserId();
            Map<String, Object> result = teacherService.batchGradeAssignments(teacherId, grades);
            return ApiResponse.success("批量评分作业完成", result);
        } catch (Exception e) {
            logger.error("批量评分作业失败", e);
            return ApiResponse.error("批量评分作业失败: " + e.getMessage());
        }
    }

    /**
     * 发布课程
     */
    @PostMapping("/courses/{courseId}/publish")
    @Operation(summary = "发布课程", description = "发布指定课程")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Void> publishCourse(
            @Parameter(description = "课程ID", example = "1") @PathVariable Long courseId) {
        try {
            Long teacherId = getCurrentUserId();
            boolean success = teacherService.publishCourse(teacherId, courseId);
            
            if (success) {
                return ApiResponse.success("发布课程成功");
            } else {
                return ApiResponse.error("发布课程失败");
            }
        } catch (Exception e) {
            logger.error("发布课程失败", e);
            return ApiResponse.error("发布课程失败: " + e.getMessage());
        }
    }

    /**
     * 取消发布课程
     */
    @PostMapping("/courses/{courseId}/unpublish")
    @Operation(summary = "取消发布课程", description = "取消发布指定课程")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Void> unpublishCourse(
            @Parameter(description = "课程ID", example = "1") @PathVariable Long courseId) {
        try {
            Long teacherId = getCurrentUserId();
            boolean success = teacherService.unpublishCourse(teacherId, courseId);
            
            if (success) {
                return ApiResponse.success("取消发布课程成功");
            } else {
                return ApiResponse.error("取消发布课程失败");
            }
        } catch (Exception e) {
            logger.error("取消发布课程失败", e);
            return ApiResponse.error("取消发布课程失败: " + e.getMessage());
        }
    }

    /**
     * 归档课程
     */
    @PostMapping("/courses/{courseId}/archive")
    @Operation(summary = "归档课程", description = "归档指定课程")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Void> archiveCourse(
            @Parameter(description = "课程ID", example = "1") @PathVariable Long courseId) {
        try {
            Long teacherId = getCurrentUserId();
            boolean success = teacherService.archiveCourse(teacherId, courseId);
            
            if (success) {
                return ApiResponse.success("归档课程成功");
            } else {
                return ApiResponse.error("归档课程失败");
            }
        } catch (Exception e) {
            logger.error("归档课程失败", e);
            return ApiResponse.error("归档课程失败: " + e.getMessage());
        }
    }

    /**
     * 获取课程学生列表
     */
    @GetMapping("/courses/{courseId}/students")
    @Operation(summary = "获取课程学生列表", description = "获取指定课程的学生列表")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<PageResult<User>> getCourseStudents(
            @Parameter(description = "课程ID", example = "1") @PathVariable Long courseId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size) {
        try {
            Long teacherId = getCurrentUserId();
            PageResult<User> students = teacherService.getCourseStudents(teacherId, courseId, page, size);
            return ApiResponse.success("获取课程学生列表成功", students);
        } catch (Exception e) {
            logger.error("获取课程学生列表失败", e);
            return ApiResponse.error("获取课程学生列表失败: " + e.getMessage());
        }
    }

    /**
     * 移除课程学生
     */
    @DeleteMapping("/courses/{courseId}/students/{studentId}")
    @Operation(summary = "移除课程学生", description = "从指定课程中移除学生")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Void> removeStudentFromCourse(
            @Parameter(description = "课程ID", example = "1") @PathVariable Long courseId,
            @Parameter(description = "学生ID", example = "1") @PathVariable Long studentId) {
        try {
            Long teacherId = getCurrentUserId();
            boolean success = teacherService.removeStudentFromCourse(teacherId, courseId, studentId);
            
            if (success) {
                return ApiResponse.success("移除学生成功");
            } else {
                return ApiResponse.error("移除学生失败");
            }
        } catch (Exception e) {
            logger.error("移除学生失败", e);
            return ApiResponse.error("移除学生失败: " + e.getMessage());
        }
    }

    /**
     * 批量添加学生到课程
     */
    @PostMapping("/courses/{courseId}/students")
    @Operation(summary = "批量添加学生到课程", description = "批量添加学生到指定课程")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Map<String, Object>> addStudentsToCourse(
            @Parameter(description = "课程ID", example = "1") @PathVariable Long courseId,
            @RequestBody Map<String, List<Long>> request) {
        try {
            Long teacherId = getCurrentUserId();
            List<Long> studentIds = request.get("studentIds");
            
            if (studentIds == null || studentIds.isEmpty()) {
                return ApiResponse.error("学生ID列表不能为空");
            }
            
            Map<String, Object> result = teacherService.addStudentsToCourse(teacherId, courseId, studentIds);
            return ApiResponse.success("批量添加学生完成", result);
        } catch (Exception e) {
            logger.error("批量添加学生失败", e);
            return ApiResponse.error("批量添加学生失败: " + e.getMessage());
        }
    }

    /**
     * 获取课程统计数据
     */
    @GetMapping("/courses/{courseId}/statistics")
    @Operation(summary = "获取课程统计数据", description = "获取指定课程的统计数据")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Map<String, Object>> getCourseStatistics(
            @Parameter(description = "课程ID", example = "1") @PathVariable Long courseId) {
        try {
            Long teacherId = getCurrentUserId();
            Map<String, Object> statistics = teacherService.getCourseStatistics(teacherId, courseId);
            return ApiResponse.success("获取课程统计数据成功", statistics);
        } catch (Exception e) {
            logger.error("获取课程统计数据失败", e);
            return ApiResponse.error("获取课程统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取作业提交统计
     */
    @GetMapping("/assignments/{assignmentId}/submission-stats")
    @Operation(summary = "获取作业提交统计", description = "获取指定作业的提交统计数据")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Map<String, Object>> getAssignmentSubmissionStats(
            @Parameter(description = "作业ID", example = "1") @PathVariable Long assignmentId) {
        try {
            Long teacherId = getCurrentUserId();
            Map<String, Object> stats = teacherService.getAssignmentSubmissionStats(teacherId, assignmentId);
            return ApiResponse.success("获取作业提交统计成功", stats);
        } catch (Exception e) {
            logger.error("获取作业提交统计失败", e);
            return ApiResponse.error("获取作业提交统计失败: " + e.getMessage());
        }
    }

    /**
     * 发送通知给学生
     */
    @PostMapping("/notifications/send")
    @Operation(summary = "发送通知给学生", description = "向指定课程或学生发送通知")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Map<String, Object>> sendNotification(@RequestBody Map<String, Object> request) {
        try {
            Long teacherId = getCurrentUserId();
            
            Long courseId = request.get("courseId") != null ? 
                Long.valueOf(request.get("courseId").toString()) : null;
            
            @SuppressWarnings("unchecked")
            List<Long> studentIds = (List<Long>) request.get("studentIds");
            
            String title = (String) request.get("title");
            String content = (String) request.get("content");
            String type = (String) request.get("type");
            
            if (title == null || content == null) {
                return ApiResponse.error("通知标题和内容不能为空");
            }
            
            Map<String, Object> result = teacherService.sendNotificationToStudents(
                teacherId, courseId, studentIds, title, content, type);
            
            return ApiResponse.success("发送通知成功", result);
        } catch (Exception e) {
            logger.error("发送通知失败", e);
            return ApiResponse.error("发送通知失败: " + e.getMessage());
        }
    }

    /**
     * 获取教学分析数据
     */
    @GetMapping("/analytics")
    @Operation(summary = "获取教学分析数据", description = "获取教师的教学分析数据")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Map<String, Object>> getTeachingAnalytics(
            @Parameter(description = "时间范围", example = "1month") @RequestParam(defaultValue = "1month") String timeRange) {
        try {
            Long teacherId = getCurrentUserId();
            Map<String, Object> analytics = teacherService.getTeachingAnalytics(teacherId, timeRange);
            return ApiResponse.success("获取教学分析数据成功", analytics);
        } catch (Exception e) {
            logger.error("获取教学分析数据失败", e);
            return ApiResponse.error("获取教学分析数据失败: " + e.getMessage());
        }
    }

    /**
     * 生成教学报告
     */
    @PostMapping("/reports/generate")
    @Operation(summary = "生成教学报告", description = "生成指定类型的教学报告")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Map<String, Object>> generateReport(@RequestBody Map<String, Object> request) {
        try {
            Long teacherId = getCurrentUserId();
            
            Long courseId = request.get("courseId") != null ? 
                Long.valueOf(request.get("courseId").toString()) : null;
            
            String reportType = (String) request.get("reportType");
            String timeRange = (String) request.get("timeRange");
            
            if (reportType == null) {
                return ApiResponse.error("报告类型不能为空");
            }
            
            Map<String, Object> report = teacherService.generateTeachingReport(
                teacherId, courseId, reportType, timeRange);
            
            return ApiResponse.success("生成教学报告成功", report);
        } catch (Exception e) {
            logger.error("生成教学报告失败", e);
            return ApiResponse.error("生成教学报告失败: " + e.getMessage());
        }
    }

    /**
     * 导出学生成绩
     */
    @PostMapping("/courses/{courseId}/grades/export")
    @Operation(summary = "导出学生成绩", description = "导出指定课程的学生成绩")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Map<String, Object>> exportGrades(
            @Parameter(description = "课程ID", example = "1") @PathVariable Long courseId,
            @RequestBody Map<String, String> request) {
        try {
            Long teacherId = getCurrentUserId();
            String format = request.get("format");
            
            if (format == null) {
                format = "excel"; // 默认格式
            }
            
            Map<String, Object> exportInfo = teacherService.exportStudentGrades(teacherId, courseId, format);
            return ApiResponse.success("导出学生成绩成功", exportInfo);
        } catch (Exception e) {
            logger.error("导出学生成绩失败", e);
            return ApiResponse.error("导出学生成绩失败: " + e.getMessage());
        }
    }

    /**
     * 获取教师评价统计
     */
    @GetMapping("/ratings")
    @Operation(summary = "获取教师评价统计", description = "获取教师的评价统计数据")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<Map<String, Object>> getRatingStats() {
        try {
            Long teacherId = getCurrentUserId();
            Map<String, Object> ratingStats = teacherService.getTeacherRatingStats(teacherId);
            return ApiResponse.success("获取教师评价统计成功", ratingStats);
        } catch (Exception e) {
            logger.error("获取教师评价统计失败", e);
            return ApiResponse.error("获取教师评价统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生活跃度数据
     */
    @GetMapping("/courses/{courseId}/activity")
    @Operation(summary = "获取学生活跃度数据", description = "获取指定课程的学生活跃度数据")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<List<Map<String, Object>>> getStudentActivity(
            @Parameter(description = "课程ID", example = "1") @PathVariable Long courseId,
            @Parameter(description = "时间范围", example = "1week") @RequestParam(defaultValue = "1week") String timeRange) {
        try {
            Long teacherId = getCurrentUserId();
            List<Map<String, Object>> activityData = teacherService.getStudentActivityData(teacherId, courseId, timeRange);
            return ApiResponse.success("获取学生活跃度数据成功", activityData);
        } catch (Exception e) {
            logger.error("获取学生活跃度数据失败", e);
            return ApiResponse.error("获取学生活跃度数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取教师日程安排
     */
    @GetMapping("/schedule")
    @Operation(summary = "获取教师日程安排", description = "获取教师的日程安排")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<List<Map<String, Object>>> getSchedule(
            @Parameter(description = "开始日期", example = "2024-12-01") @RequestParam String startDate,
            @Parameter(description = "结束日期", example = "2024-12-31") @RequestParam String endDate) {
        try {
            Long teacherId = getCurrentUserId();
            List<Map<String, Object>> schedule = teacherService.getTeacherSchedule(teacherId, startDate, endDate);
            return ApiResponse.success("获取教师日程安排成功", schedule);
        } catch (Exception e) {
            logger.error("获取教师日程安排失败", e);
            return ApiResponse.error("获取教师日程安排失败: " + e.getMessage());
        }
    }

    /**
     * 获取教师通知列表
     */
    @GetMapping("/notifications")
    @Operation(summary = "获取教师通知列表", description = "获取教师的通知列表")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ApiResponse<PageResult<Map<String, Object>>> getNotifications(
            @Parameter(description = "通知类型", example = "system") @RequestParam(required = false) String type,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size) {
        try {
            Long teacherId = getCurrentUserId();
            PageResult<Map<String, Object>> notifications = teacherService.getTeacherNotifications(teacherId, type, page, size);
            return ApiResponse.success("获取教师通知列表成功", notifications);
        } catch (Exception e) {
            logger.error("获取教师通知列表失败", e);
            return ApiResponse.error("获取教师通知列表失败: " + e.getMessage());
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