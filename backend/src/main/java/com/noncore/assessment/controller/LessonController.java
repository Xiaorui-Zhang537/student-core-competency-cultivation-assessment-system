package com.noncore.assessment.controller;

import com.noncore.assessment.entity.Lesson;
import com.noncore.assessment.entity.LessonProgress;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.LessonService;
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
 * 课程章节控制器
 * 提供课程章节相关的API接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@RestController
@RequestMapping("/api/lessons")
@Tag(name = "课程章节管理", description = "课程章节的增删改查及学习进度管理")
public class LessonController {

    private static final Logger logger = LoggerFactory.getLogger(LessonController.class);

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    /**
     * 创建新章节
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "创建章节", description = "教师创建新的课程章节")
    public ApiResponse<Lesson> createLesson(@Valid @RequestBody Lesson lesson) {
        try {
            Lesson createdLesson = lessonService.createLesson(lesson);
            return ApiResponse.success("章节创建成功", createdLesson);
        } catch (Exception e) {
            logger.error("创建章节失败", e);
            return ApiResponse.error("创建章节失败: " + e.getMessage());
        }
    }

    /**
     * 获取章节详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取章节详情", description = "根据章节ID获取详细信息")
    public ApiResponse<Lesson> getLessonById(@PathVariable Long id) {
        try {
            Lesson lesson = lessonService.getLessonById(id);
            return ApiResponse.success("获取章节详情成功", lesson);
        } catch (Exception e) {
            logger.error("获取章节详情失败: lessonId={}", id, e);
            return ApiResponse.error("获取章节详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新章节信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "更新章节", description = "更新章节信息")
    public ApiResponse<Lesson> updateLesson(@PathVariable Long id, @Valid @RequestBody Lesson lesson) {
        try {
            Lesson updatedLesson = lessonService.updateLesson(id, lesson);
            return ApiResponse.success("章节更新成功", updatedLesson);
        } catch (Exception e) {
            logger.error("更新章节失败: lessonId={}", id, e);
            return ApiResponse.error("更新章节失败: " + e.getMessage());
        }
    }

    /**
     * 删除章节
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "删除章节", description = "删除指定章节")
    public ApiResponse<Void> deleteLesson(@PathVariable Long id) {
        try {
            lessonService.deleteLesson(id);
            return ApiResponse.success("章节删除成功");
        } catch (Exception e) {
            logger.error("删除章节失败: lessonId={}", id, e);
            return ApiResponse.error("删除章节失败: " + e.getMessage());
        }
    }

    /**
     * 获取课程的所有章节
     */
    @GetMapping("/course/{courseId}")
    @Operation(summary = "获取课程章节", description = "获取指定课程的所有章节")
    public ApiResponse<List<Lesson>> getLessonsByCourse(@PathVariable Long courseId) {
        try {
            List<Lesson> lessons = lessonService.getLessonsByCourse(courseId);
            return ApiResponse.success("获取课程章节成功", lessons);
        } catch (Exception e) {
            logger.error("获取课程章节失败: courseId={}", courseId, e);
            return ApiResponse.error("获取课程章节失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取章节列表
     */
    @GetMapping("/course/{courseId}/page")
    @Operation(summary = "分页获取章节", description = "分页获取指定课程的章节列表")
    public ApiResponse<PageResult<Lesson>> getLessonsWithPagination(
            @PathVariable Long courseId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size) {
        try {
            PageResult<Lesson> result = lessonService.getLessonsWithPagination(courseId, page, size);
            return ApiResponse.success("获取章节列表成功", result);
        } catch (Exception e) {
            logger.error("分页获取章节失败: courseId={}, page={}, size={}", courseId, page, size, e);
            return ApiResponse.error("获取章节列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生章节进度
     */
    @GetMapping("/{lessonId}/progress")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "获取学习进度", description = "获取学生在指定章节的学习进度")
    public ApiResponse<LessonProgress> getStudentProgress(
            @PathVariable Long lessonId,
            @Parameter(description = "学生ID，不传则使用当前用户") @RequestParam(required = false) Long studentId) {
        try {
            // 如果没有传studentId且当前用户是学生，则使用当前用户ID
            if (studentId == null) {
                studentId = getCurrentUserId();
            }
            
            LessonProgress progress = lessonService.getStudentProgress(studentId, lessonId);
            return ApiResponse.success("获取学习进度成功", progress);
        } catch (Exception e) {
            logger.error("获取学习进度失败: lessonId={}, studentId={}", lessonId, studentId, e);
            return ApiResponse.error("获取学习进度失败: " + e.getMessage());
        }
    }

    /**
     * 更新学生章节进度
     */
    @PostMapping("/{lessonId}/progress")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "更新学习进度", description = "更新学生在指定章节的学习进度")
    public ApiResponse<Void> updateStudentProgress(
            @PathVariable Long lessonId,
            @RequestBody Map<String, Object> progressData) {
        try {
            Long studentId = getCurrentUserId();
            
            BigDecimal progress = progressData.get("progress") != null ? 
                new BigDecimal(progressData.get("progress").toString()) : null;
            Integer studyTime = progressData.get("studyTime") != null ? 
                (Integer) progressData.get("studyTime") : null;
            Integer lastPosition = progressData.get("lastPosition") != null ? 
                (Integer) progressData.get("lastPosition") : null;
            
            boolean success = lessonService.updateStudentProgress(studentId, lessonId, progress, studyTime, lastPosition);
            
            if (success) {
                return ApiResponse.success("学习进度更新成功");
            } else {
                return ApiResponse.error("学习进度更新失败");
            }
        } catch (Exception e) {
            logger.error("更新学习进度失败: lessonId={}", lessonId, e);
            return ApiResponse.error("更新学习进度失败: " + e.getMessage());
        }
    }

    /**
     * 标记章节完成
     */
    @PostMapping("/{lessonId}/complete")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "标记章节完成", description = "标记学生已完成指定章节")
    public ApiResponse<Void> markLessonCompleted(@PathVariable Long lessonId) {
        try {
            Long studentId = getCurrentUserId();
            boolean success = lessonService.markLessonCompleted(studentId, lessonId);
            
            if (success) {
                return ApiResponse.success("章节标记完成成功");
            } else {
                return ApiResponse.error("章节标记完成失败");
            }
        } catch (Exception e) {
            logger.error("标记章节完成失败: lessonId={}", lessonId, e);
            return ApiResponse.error("标记章节完成失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生课程进度
     */
    @GetMapping("/course/{courseId}/student-progress")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "获取课程进度", description = "获取学生在指定课程的所有章节进度")
    public ApiResponse<List<LessonProgress>> getStudentCourseProgress(
            @PathVariable Long courseId,
            @Parameter(description = "学生ID，不传则使用当前用户") @RequestParam(required = false) Long studentId) {
        try {
            if (studentId == null) {
                studentId = getCurrentUserId();
            }
            
            List<LessonProgress> progressList = lessonService.getStudentCourseProgress(studentId, courseId);
            return ApiResponse.success("获取课程进度成功", progressList);
        } catch (Exception e) {
            logger.error("获取课程进度失败: courseId={}, studentId={}", courseId, studentId, e);
            return ApiResponse.error("获取课程进度失败: " + e.getMessage());
        }
    }

    /**
     * 计算课程整体进度
     */
    @GetMapping("/course/{courseId}/progress-percentage")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "计算课程进度", description = "计算学生在指定课程的整体进度百分比")
    public ApiResponse<BigDecimal> calculateCourseProgress(
            @PathVariable Long courseId,
            @Parameter(description = "学生ID，不传则使用当前用户") @RequestParam(required = false) Long studentId) {
        try {
            if (studentId == null) {
                studentId = getCurrentUserId();
            }
            
            BigDecimal progress = lessonService.calculateCourseProgress(studentId, courseId);
            return ApiResponse.success("计算课程进度成功", progress);
        } catch (Exception e) {
            logger.error("计算课程进度失败: courseId={}, studentId={}", courseId, studentId, e);
            return ApiResponse.error("计算课程进度失败: " + e.getMessage());
        }
    }

    /**
     * 获取热门章节
     */
    @GetMapping("/popular")
    @Operation(summary = "获取热门章节", description = "获取学习人数最多的章节")
    public ApiResponse<List<Lesson>> getPopularLessons(
            @Parameter(description = "限制数量", example = "10") @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Lesson> lessons = lessonService.getPopularLessons(limit);
            return ApiResponse.success("获取热门章节成功", lessons);
        } catch (Exception e) {
            logger.error("获取热门章节失败", e);
            return ApiResponse.error("获取热门章节失败: " + e.getMessage());
        }
    }

    /**
     * 搜索章节
     */
    @GetMapping("/search")
    @Operation(summary = "搜索章节", description = "根据关键词搜索章节")
    public ApiResponse<List<Lesson>> searchLessons(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword,
            @Parameter(description = "课程ID，可选") @RequestParam(required = false) Long courseId) {
        try {
            List<Lesson> lessons = lessonService.searchLessons(keyword, courseId);
            return ApiResponse.success("搜索章节成功", lessons);
        } catch (Exception e) {
            logger.error("搜索章节失败: keyword={}, courseId={}", keyword, courseId, e);
            return ApiResponse.error("搜索章节失败: " + e.getMessage());
        }
    }

    /**
     * 发布章节
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "发布章节", description = "将章节状态设置为已发布")
    public ApiResponse<Void> publishLesson(@PathVariable Long id) {
        try {
            boolean success = lessonService.publishLesson(id);
            if (success) {
                return ApiResponse.success("章节发布成功");
            } else {
                return ApiResponse.error("章节发布失败");
            }
        } catch (Exception e) {
            logger.error("发布章节失败: lessonId={}", id, e);
            return ApiResponse.error("发布章节失败: " + e.getMessage());
        }
    }

    /**
     * 取消发布章节
     */
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "取消发布章节", description = "将章节状态设置为草稿")
    public ApiResponse<Void> unpublishLesson(@PathVariable Long id) {
        try {
            boolean success = lessonService.unpublishLesson(id);
            if (success) {
                return ApiResponse.success("章节取消发布成功");
            } else {
                return ApiResponse.error("章节取消发布失败");
            }
        } catch (Exception e) {
            logger.error("取消发布章节失败: lessonId={}", id, e);
            return ApiResponse.error("取消发布章节失败: " + e.getMessage());
        }
    }

    /**
     * 调整章节顺序
     */
    @PutMapping("/{id}/order")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "调整章节顺序", description = "调整章节在课程中的排序")
    public ApiResponse<Void> updateLessonOrder(@PathVariable Long id, @RequestBody Map<String, Integer> orderData) {
        try {
            Integer newOrder = orderData.get("order");
            if (newOrder == null) {
                return ApiResponse.error("新顺序号不能为空");
            }
            
            boolean success = lessonService.updateLessonOrder(id, newOrder);
            if (success) {
                return ApiResponse.success("章节顺序调整成功");
            } else {
                return ApiResponse.error("章节顺序调整失败");
            }
        } catch (Exception e) {
            logger.error("调整章节顺序失败: lessonId={}", id, e);
            return ApiResponse.error("调整章节顺序失败: " + e.getMessage());
        }
    }

    /**
     * 获取章节统计信息
     */
    @GetMapping("/course/{courseId}/statistics")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "获取章节统计", description = "获取指定课程的章节统计信息")
    public ApiResponse<Map<String, Object>> getLessonStatistics(@PathVariable Long courseId) {
        try {
            Map<String, Object> statistics = lessonService.getLessonStatistics(courseId);
            return ApiResponse.success("获取章节统计成功", statistics);
        } catch (Exception e) {
            logger.error("获取章节统计失败: courseId={}", courseId, e);
            return ApiResponse.error("获取章节统计失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新章节状态
     */
    @PutMapping("/batch-status")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "批量更新状态", description = "批量更新多个章节的状态")
    public ApiResponse<Map<String, Object>> batchUpdateStatus(@RequestBody BatchUpdateRequest request) {
        try {
            Map<String, Object> result = lessonService.batchUpdateLessonStatus(request.getLessonIds(), request.getStatus());
            return ApiResponse.success("批量更新章节状态完成", result);
        } catch (Exception e) {
            logger.error("批量更新章节状态失败", e);
            return ApiResponse.error("批量更新章节状态失败: " + e.getMessage());
        }
    }

    /**
     * 复制章节到其他课程
     */
    @PostMapping("/{id}/copy")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "复制章节", description = "复制章节到其他课程")
    public ApiResponse<Lesson> copyLessonToCourse(
            @PathVariable Long id, 
            @RequestBody Map<String, Long> targetData) {
        try {
            Long targetCourseId = targetData.get("targetCourseId");
            if (targetCourseId == null) {
                return ApiResponse.error("目标课程ID不能为空");
            }
            
            Lesson copiedLesson = lessonService.copyLessonToCourse(id, targetCourseId);
            return ApiResponse.success("章节复制成功", copiedLesson);
        } catch (Exception e) {
            logger.error("复制章节失败: lessonId={}", id, e);
            return ApiResponse.error("复制章节失败: " + e.getMessage());
        }
    }

    /**
     * 获取最近学习章节
     */
    @GetMapping("/recent-studied")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "获取最近学习", description = "获取学生最近学习的章节")
    public ApiResponse<List<Map<String, Object>>> getRecentStudiedLessons(
            @Parameter(description = "限制数量", example = "5") @RequestParam(defaultValue = "5") Integer limit) {
        try {
            Long studentId = getCurrentUserId();
            List<Map<String, Object>> lessons = lessonService.getRecentStudiedLessons(studentId, limit);
            return ApiResponse.success("获取最近学习章节成功", lessons);
        } catch (Exception e) {
            logger.error("获取最近学习章节失败", e);
            return ApiResponse.error("获取最近学习章节失败: " + e.getMessage());
        }
    }

    /**
     * 添加章节笔记
     */
    @PostMapping("/{lessonId}/notes")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "添加章节笔记", description = "为指定章节添加学习笔记")
    public ApiResponse<Void> addLessonNotes(
            @PathVariable Long lessonId,
            @RequestBody Map<String, String> notesData) {
        try {
            Long studentId = getCurrentUserId();
            String notes = notesData.get("notes");
            
            if (notes == null || notes.trim().isEmpty()) {
                return ApiResponse.error("笔记内容不能为空");
            }
            
            boolean success = lessonService.addLessonNotes(studentId, lessonId, notes);
            if (success) {
                return ApiResponse.success("添加章节笔记成功");
            } else {
                return ApiResponse.error("添加章节笔记失败");
            }
        } catch (Exception e) {
            logger.error("添加章节笔记失败: lessonId={}", lessonId, e);
            return ApiResponse.error("添加章节笔记失败: " + e.getMessage());
        }
    }

    /**
     * 为章节评分
     */
    @PostMapping("/{lessonId}/rating")
    @PreAuthorize("hasRole('ROLE_STUDENT') or hasRole('ROLE_ADMIN')")
    @Operation(summary = "章节评分", description = "为指定章节进行评分")
    public ApiResponse<Void> rateLesson(
            @PathVariable Long lessonId,
            @RequestBody Map<String, Integer> ratingData) {
        try {
            Long studentId = getCurrentUserId();
            Integer rating = ratingData.get("rating");
            
            if (rating == null) {
                return ApiResponse.error("评分不能为空");
            }
            
            boolean success = lessonService.rateLessons(studentId, lessonId, rating);
            if (success) {
                return ApiResponse.success("章节评分成功");
            } else {
                return ApiResponse.error("章节评分失败");
            }
        } catch (Exception e) {
            logger.error("章节评分失败: lessonId={}", lessonId, e);
            return ApiResponse.error("章节评分失败: " + e.getMessage());
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
     * 批量更新请求DTO
     */
    public static class BatchUpdateRequest {
        private List<Long> lessonIds;
        private String status;

        public List<Long> getLessonIds() {
            return lessonIds;
        }

        public void setLessonIds(List<Long> lessonIds) {
            this.lessonIds = lessonIds;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
} 