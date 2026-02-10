package com.noncore.assessment.controller;

import com.noncore.assessment.entity.Lesson;
import com.noncore.assessment.entity.LessonProgress;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.LessonService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.PageResult;
import com.noncore.assessment.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.noncore.assessment.dto.request.BatchLessonStatusRequest;


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
@RequestMapping("/lessons")
@Tag(name = "课程章节管理", description = "课程章节的增删改查及学习进度管理")
public class LessonController extends BaseController {

    private final LessonService lessonService;
    private final FileStorageService fileStorageService;

    public LessonController(LessonService lessonService, UserService userService, FileStorageService fileStorageService) {
        super(userService);
        this.lessonService = lessonService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * 创建新章节
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "创建章节", description = "教师创建新的课程章节")
    public ResponseEntity<ApiResponse<Lesson>> createLesson(@Valid @RequestBody Lesson lesson) {
        Lesson createdLesson = lessonService.createLesson(lesson);
        return ResponseEntity.ok(ApiResponse.success(createdLesson));
    }

    /**
     * 获取章节详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取章节详情", description = "根据章节ID获取详细信息")
    public ResponseEntity<ApiResponse<Lesson>> getLessonById(@PathVariable Long id) {
        Lesson lesson = lessonService.getLessonById(id);
        return ResponseEntity.ok(ApiResponse.success(lesson));
    }

    /**
     * 更新章节信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "更新章节", description = "更新章节信息")
    public ResponseEntity<ApiResponse<Lesson>> updateLesson(@PathVariable Long id, @Valid @RequestBody Lesson lesson) {
        Lesson updatedLesson = lessonService.updateLesson(id, lesson);
        return ResponseEntity.ok(ApiResponse.success(updatedLesson));
    }

    /**
     * 设置章节内容（视频URL与资料绑定）
     */
    @PutMapping("/{id}/content")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "设置章节内容", description = "更新视频URL并绑定资料文件（传 fileIds）")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateLessonContent(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String videoUrl = body.get("videoUrl") == null ? null : String.valueOf(body.get("videoUrl"));
        Boolean allowScrubbing = body.get("allowScrubbing") == null ? null : Boolean.valueOf(String.valueOf(body.get("allowScrubbing")));
        Boolean allowSpeedChange = body.get("allowSpeedChange") == null ? null : Boolean.valueOf(String.valueOf(body.get("allowSpeedChange")));
        java.util.List<Long> fileIds = null;
        Object files = body.get("materialFileIds");
        if (files instanceof java.util.List<?> list) {
            fileIds = new java.util.ArrayList<>();
            for (Object o : list) { if (o != null) fileIds.add(Long.valueOf(String.valueOf(o))); }
        }

        // 更新视频 URL/播放控制
        if (videoUrl != null || allowScrubbing != null || allowSpeedChange != null) {
            Lesson l = new Lesson();
            l.setVideoUrl(videoUrl);
            l.setAllowScrubbing(allowScrubbing);
            l.setAllowSpeedChange(allowSpeedChange);
            lessonService.updateLesson(id, l);
        }

        // 绑定资料文件（方案A：通过关联表 lesson_materials，不移动原文件记录）
        if (fileIds != null) {
            // 去重并保证为正整数
            java.util.LinkedHashSet<Long> uniq = new java.util.LinkedHashSet<>();
            for (Long fid : fileIds) { if (fid != null && fid > 0) uniq.add(fid); }
            lessonService.replaceLessonMaterials(id, new java.util.ArrayList<>(uniq), getCurrentUserId());
        }
        Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("lessonId", id);
        resp.put("videoUrl", videoUrl);
        resp.put("materialFileIds", fileIds == null ? java.util.Collections.emptyList() : fileIds);
        resp.put("allowScrubbing", allowScrubbing);
        resp.put("allowSpeedChange", allowSpeedChange);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    /**
     * 获取节次资料（通过关联表）
     */
    @GetMapping("/{id}/materials")
    @Operation(summary = "获取节次资料", description = "返回该节次关联的所有资料文件")
    public ResponseEntity<ApiResponse<List<com.noncore.assessment.entity.FileRecord>>> getLessonMaterials(@PathVariable Long id) {
        List<com.noncore.assessment.entity.FileRecord> files = lessonService.getLessonMaterials(id);
        return ResponseEntity.ok(ApiResponse.success(files));
    }

    /**
     * 删除章节
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "删除章节", description = "删除指定章节")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 获取课程的所有章节
     */
    @GetMapping("/course/{courseId}")
    @Operation(summary = "获取课程章节", description = "获取指定课程的所有章节")
    public ResponseEntity<ApiResponse<List<Lesson>>> getLessonsByCourse(@PathVariable Long courseId) {
        List<Lesson> lessons = lessonService.getLessonsByCourse(courseId);
        return ResponseEntity.ok(ApiResponse.success(lessons));
    }

    /**
     * 分页获取章节列表
     */
    @GetMapping("/course/{courseId}/page")
    @Operation(summary = "分页获取章节", description = "分页获取指定课程的章节列表")
    public ResponseEntity<ApiResponse<PageResult<Lesson>>> getLessonsWithPagination(
            @PathVariable Long courseId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size) {
        PageResult<Lesson> result = lessonService.getLessonsWithPagination(courseId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取学生章节进度
     */
    @GetMapping("/{lessonId}/progress")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    @Operation(summary = "获取学习进度", description = "获取学生在指定章节的学习进度")
    public ResponseEntity<ApiResponse<LessonProgress>> getStudentProgress(
            @PathVariable Long lessonId,
            @Parameter(description = "学生ID，教师或管理员可指定") @RequestParam(required = false) Long studentId) {
        if (studentId == null) {
            studentId = getCurrentUserId();
        } else if (!hasRole("TEACHER") && !hasRole("ADMIN")) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        LessonProgress progress = lessonService.getStudentProgress(studentId, lessonId);
        return ResponseEntity.ok(ApiResponse.success(progress));
    }

    /**
     * 更新学生章节进度
     */
    @PostMapping("/{lessonId}/progress")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "更新学习进度", description = "更新学生在指定章节的学习进度")
    public ResponseEntity<ApiResponse<Void>> updateStudentProgress(
            @PathVariable Long lessonId,
            @RequestBody Map<String, Object> progressData) {
        Long studentId = getCurrentUserId();
        BigDecimal progress = progressData.get("progress") != null ?
                new BigDecimal(progressData.get("progress").toString()) : null;
        Integer studyTime = progressData.get("studyTime") != null ?
                (Integer) progressData.get("studyTime") : null;
        Integer lastPosition = progressData.get("lastPosition") != null ?
                (Integer) progressData.get("lastPosition") : null;
        lessonService.updateStudentProgress(studentId, lessonId, progress, studyTime, lastPosition);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 标记章节完成
     */
    @PostMapping("/{lessonId}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "标记章节完成", description = "标记学生已完成指定章节")
    public ResponseEntity<ApiResponse<Void>> markLessonCompleted(@PathVariable Long lessonId) {
        lessonService.markLessonCompleted(getCurrentUserId(), lessonId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 获取学生课程进度
     */
    @GetMapping("/course/{courseId}/student-progress")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    @Operation(summary = "获取课程进度", description = "获取学生在指定课程的所有章节进度")
    public ResponseEntity<ApiResponse<List<LessonProgress>>> getStudentCourseProgress(
            @PathVariable Long courseId,
            @Parameter(description = "学生ID，教师或管理员可指定") @RequestParam(required = false) Long studentId) {
        if (studentId == null) {
            studentId = getCurrentUserId();
        } else if (!hasRole("TEACHER") && !hasRole("ADMIN")) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        List<LessonProgress> progressList = lessonService.getStudentCourseProgress(studentId, courseId);
        return ResponseEntity.ok(ApiResponse.success(progressList));
    }

    /**
     * 计算课程整体进度
     */
    @GetMapping("/course/{courseId}/progress-percentage")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    @Operation(summary = "计算课程进度", description = "计算学生在指定课程的整体进度百分比")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateCourseProgress(
            @PathVariable Long courseId,
            @Parameter(description = "学生ID，教师或管理员可指定") @RequestParam(required = false) Long studentId) {
        if (studentId == null) {
            studentId = getCurrentUserId();
        } else if (!hasRole("TEACHER") && !hasRole("ADMIN")) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        BigDecimal progress = lessonService.calculateCourseProgress(studentId, courseId);
        return ResponseEntity.ok(ApiResponse.success(progress));
    }

    /**
     * 获取热门章节
     */
    @GetMapping("/popular")
    @Operation(summary = "获取热门章节", description = "获取学习人数最多的章节")
    public ResponseEntity<ApiResponse<List<Lesson>>> getPopularLessons(
            @Parameter(description = "限制数量", example = "10") @RequestParam(defaultValue = "10") Integer limit) {
        List<Lesson> lessons = lessonService.getPopularLessons(limit);
        return ResponseEntity.ok(ApiResponse.success(lessons));
    }

    /**
     * 搜索章节
     */
    @GetMapping("/search")
    @Operation(summary = "搜索章节", description = "根据关键词搜索章节")
    public ResponseEntity<ApiResponse<List<Lesson>>> searchLessons(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword,
            @Parameter(description = "课程ID，可选") @RequestParam(required = false) Long courseId) {
        List<Lesson> lessons = lessonService.searchLessons(keyword, courseId);
        return ResponseEntity.ok(ApiResponse.success(lessons));
    }

    /**
     * 发布章节
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "发布章节", description = "将章节状态设置为已发布")
    public ResponseEntity<ApiResponse<Void>> publishLesson(@PathVariable Long id) {
        lessonService.publishLesson(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 取消发布章节
     */
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "取消发布章节", description = "将章节状态设置为草稿")
    public ResponseEntity<ApiResponse<Void>> unpublishLesson(@PathVariable Long id) {
        lessonService.unpublishLesson(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 调整章节顺序
     */
    @PutMapping("/{id}/order")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "调整章节顺序", description = "调整章节在课程中的排序")
    public ResponseEntity<ApiResponse<Void>> updateLessonOrder(@PathVariable Long id, @RequestBody Map<String, Integer> orderData) {
        Integer newOrder = orderData.get("order");
        if (newOrder == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "新顺序号不能为空");
        }
        lessonService.updateLessonOrder(id, newOrder);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 获取章节统计信息
     */
    @GetMapping("/course/{courseId}/statistics")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取章节统计", description = "获取指定课程的章节统计信息")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLessonStatistics(@PathVariable Long courseId) {
        Map<String, Object> statistics = lessonService.getLessonStatistics(courseId);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * 批量更新章节状态
     */
    @PutMapping("/batch-status")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "批量更新状态", description = "批量更新多个章节的状态")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchUpdateStatus(@RequestBody BatchLessonStatusRequest request) {
        Map<String, Object> result = lessonService.batchUpdateLessonStatus(request.getLessonIds(), request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 复制章节到其他课程
     */
    @PostMapping("/{id}/copy")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "复制章节", description = "复制章节到其他课程")
    public ResponseEntity<ApiResponse<Lesson>> copyLessonToCourse(
            @PathVariable Long id,
            @RequestBody Map<String, Long> targetData) {
        Long targetCourseId = targetData.get("targetCourseId");
        if (targetCourseId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "目标课程ID不能为空");
        }
        Lesson copiedLesson = lessonService.copyLessonToCourse(id, targetCourseId);
        return ResponseEntity.ok(ApiResponse.success(copiedLesson));
    }

    /**
     * 获取最近学习章节
     */
    @GetMapping("/recent-studied")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "获取最近学习", description = "获取学生最近学习的章节")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRecentStudiedLessons(
            @Parameter(description = "限制数量", example = "5") @RequestParam(defaultValue = "5") Integer limit) {
        List<Map<String, Object>> lessons = lessonService.getRecentStudiedLessons(getCurrentUserId(), limit);
        return ResponseEntity.ok(ApiResponse.success(lessons));
    }

    /**
     * 获取章节笔记
     */
    @GetMapping("/{lessonId}/notes")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "获取章节笔记", description = "获取当前学生在指定章节的学习笔记")
    public ResponseEntity<ApiResponse<Map<String, String>>> getLessonNotes(@PathVariable Long lessonId) {
        String notes = lessonService.getLessonNotes(getCurrentUserId(), lessonId);
        Map<String, String> data = new java.util.HashMap<>();
        data.put("notes", notes == null ? "" : notes);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 添加章节笔记
     */
    @PostMapping("/{lessonId}/notes")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "添加章节笔记", description = "为指定章节添加学习笔记")
    public ResponseEntity<ApiResponse<Void>> addLessonNotes(
            @PathVariable Long lessonId,
            @RequestBody Map<String, String> notesData) {
        String notes = notesData.get("notes");
        if (notes == null || notes.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "笔记内容不能为空");
        }
        lessonService.addLessonNotes(getCurrentUserId(), lessonId, notes);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 为章节评分
     */
    @PostMapping("/{lessonId}/rating")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "章节评分", description = "为指定章节进行评分")
    public ResponseEntity<ApiResponse<Void>> rateLesson(
            @PathVariable Long lessonId,
            @RequestBody Map<String, Integer> ratingData) {
        Integer rating = ratingData.get("rating");
        if (rating == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "评分不能为空");
        }
        lessonService.rateLessons(getCurrentUserId(), lessonId, rating);
        return ResponseEntity.ok(ApiResponse.success());
    }
} 