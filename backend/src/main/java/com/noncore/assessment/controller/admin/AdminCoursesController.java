package com.noncore.assessment.controller.admin;

import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.dto.response.admin.AdminCourseDetailResponse;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.service.admin.AdminCourseService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
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
}

