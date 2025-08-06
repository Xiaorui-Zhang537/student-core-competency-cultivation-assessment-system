package com.noncore.assessment.controller;

import com.noncore.assessment.dto.response.StudentDashboardResponse;
import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.service.*;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
@Tag(name = "学生模块", description = "学生相关接口")
public class StudentController extends BaseController {

    private final StudentService studentService;
    private final DashboardService dashboardService;
    private final EnrollmentService enrollmentService;
    private final AssignmentService assignmentService;

    public StudentController(StudentService studentService, DashboardService dashboardService, EnrollmentService enrollmentService, AssignmentService assignmentService, UserService userService) {
        super(userService);
        this.studentService = studentService;
        this.dashboardService = dashboardService;
        this.enrollmentService = enrollmentService;
        this.assignmentService = assignmentService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "获取学生仪表盘数据")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<StudentDashboardResponse>> getStudentDashboard() {
        StudentDashboardResponse dashboardData = dashboardService.getStudentDashboardData(getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(dashboardData));
    }

    @GetMapping("/my/courses")
    @Operation(summary = "获取我参加的课程")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<Course>>> getMyEnrolledCourses() {
        List<Course> courses = enrollmentService.getEnrolledCourses(getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/my/assignments")
    @Operation(summary = "获取我的所有作业")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<Assignment>>> getMyAssignments() {
        List<Assignment> assignments = assignmentService.getAssignmentsForStudent(getCurrentUserId(), null);
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }

    @GetMapping("/my/assignments/pending")
    @Operation(summary = "获取我待完成的作业")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<Assignment>>> getMyPendingAssignments() {
        List<Assignment> assignments = studentService.getPendingAssignments(getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }
} 