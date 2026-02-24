package com.noncore.assessment.controller.admin;

import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.dto.response.admin.AdminCourseDetailResponse;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.service.admin.AdminCourseService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员-课程总览控制器。
 *
 * @author System
 * @since 2026-02-14
 */
@RestController
@RequestMapping("/admin/courses")
@Tag(name = "管理员-课程管理", description = "管理员课程总览与详情")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCoursesController extends BaseController {

    private final AdminCourseService adminCourseService;

    public AdminCoursesController(AdminCourseService adminCourseService, UserService userService) {
        super(userService);
        this.adminCourseService = adminCourseService;
    }

    @GetMapping
    @Operation(summary = "分页查询课程（全量）")
    public ResponseEntity<ApiResponse<PageResult<Course>>> pageCourses(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false, name = "query") String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long teacherId
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminCourseService.pageCourses(page, size, query, category, difficulty, status, teacherId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取课程详情（含在读人数）")
    public ResponseEntity<ApiResponse<AdminCourseDetailResponse>> getCourse(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminCourseService.getCourseDetail(id)));
    }

    /**
     * 管理员获取课程学生列表（分页）。
     *
     * <p>说明：管理员可全局查看任意课程学生名单，用于数据中心与审计场景。</p>
     */
    @GetMapping("/{id}/students")
    @Operation(summary = "管理员获取课程学生列表", description = "管理员获取任意课程的学生列表（分页），支持搜索/筛选/排序")
    public ResponseEntity<ApiResponse<PageResult<User>>> pageCourseStudents(
            @PathVariable("id") Long courseId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "搜索关键词（昵称/用户名/学号/工号）") @RequestParam(required = false) String search,
            @Parameter(description = "排序字段(name|progress|grade|lastActive|joinDate)") @RequestParam(required = false, defaultValue = "joinDate") String sortBy,
            @Parameter(description = "活跃度筛选(high|medium|low|inactive)") @RequestParam(required = false) String activity,
            @Parameter(description = "成绩筛选(excellent|good|average|below)") @RequestParam(required = false) String grade,
            @Parameter(description = "进度筛选(not-started|in-progress|completed)") @RequestParam(required = false) String progress
    ) {
        PageResult<User> result = adminCourseService.pageCourseStudents(courseId, page, size, search, sortBy, activity, grade, progress);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}

