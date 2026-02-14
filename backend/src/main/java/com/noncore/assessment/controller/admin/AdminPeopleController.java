package com.noncore.assessment.controller.admin;

import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.dto.response.admin.AdminStudentDetailResponse;
import com.noncore.assessment.dto.response.admin.AdminTeacherDetailResponse;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.service.admin.AdminPeopleService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员-学生/教师数据中心控制器（详情）。
 *
 * <p>列表分页复用 `/admin/users`（按 role=student/teacher 过滤），详情在此提供聚合字段。</p>
 *
 * @author System
 * @since 2026-02-14
 */
@RestController
@RequestMapping("/admin/people")
@Tag(name = "管理员-人员数据中心", description = "管理员访问学生/教师聚合数据")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPeopleController extends BaseController {

    private final AdminPeopleService adminPeopleService;

    public AdminPeopleController(AdminPeopleService adminPeopleService, UserService userService) {
        super(userService);
        this.adminPeopleService = adminPeopleService;
    }

    @GetMapping("/students/{id}")
    @Operation(summary = "学生详情（聚合）")
    public ResponseEntity<ApiResponse<AdminStudentDetailResponse>> getStudent(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminPeopleService.getStudentDetail(id)));
    }

    @GetMapping("/teachers/{id}")
    @Operation(summary = "教师详情（聚合）")
    public ResponseEntity<ApiResponse<AdminTeacherDetailResponse>> getTeacher(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminPeopleService.getTeacherDetail(id)));
    }
}

