package com.noncore.assessment.controller.admin;

import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.dto.response.admin.AdminLessonNoteListItemResponse;
import com.noncore.assessment.mapper.LessonProgressMapper;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理员-课程课堂笔记（只读）。
 *
 * <p>说明：用于审计与导出，避免前端 N×M 拉取。</p>
 *
 * @author System
 * @since 2026-02-27
 */
@RestController
@RequestMapping("/admin/courses")
@Tag(name = "管理员-课程课堂笔记", description = "管理员分页查看课程课堂笔记（只读）")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourseNotesController extends BaseController {

    private final LessonProgressMapper lessonProgressMapper;

    public AdminCourseNotesController(LessonProgressMapper lessonProgressMapper, UserService userService) {
        super(userService);
        this.lessonProgressMapper = lessonProgressMapper;
    }

    @GetMapping("/{courseId}/lesson-notes")
    @Operation(summary = "分页查看课程课堂笔记", description = "仅返回 notes 非空的记录；支持按学生/节次/关键词筛选")
    public ResponseEntity<ApiResponse<PageResult<AdminLessonNoteListItemResponse>>> pageLessonNotes(
            @PathVariable Long courseId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long lessonId,
            @RequestParam(required = false, name = "q") String q
    ) {
        int p = page == null ? 1 : Math.max(1, page);
        int s = size == null ? 20 : Math.min(Math.max(1, size), 200);
        int offset = (p - 1) * s;
        String qq = (q == null || q.isBlank()) ? null : q.trim();

        List<AdminLessonNoteListItemResponse> items = lessonProgressMapper.selectAdminCourseLessonNotes(courseId, studentId, lessonId, qq, offset, s);
        Long total = lessonProgressMapper.countAdminCourseLessonNotes(courseId, studentId, lessonId, qq);
        long tt = total == null ? 0L : total;
        int pages = (int) Math.max(1, (tt + s - 1) / s);
        return ResponseEntity.ok(ApiResponse.success(PageResult.of(items, p, s, tt, pages)));
    }
}

