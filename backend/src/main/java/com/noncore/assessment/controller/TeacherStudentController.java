package com.noncore.assessment.controller;

import com.noncore.assessment.dto.response.TeacherStudentProfileResponse;
import com.noncore.assessment.dto.response.CourseStudentBasicResponse;
import com.noncore.assessment.dto.response.TeacherStudentActivityResponse;
import com.noncore.assessment.dto.response.TeacherStudentAlertsResponse;
import com.noncore.assessment.entity.LearningRecommendation;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.service.TeacherStudentService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teachers/students")
@Tag(name = "教师-学生详情", description = "教师视角查询学生概况与课程")
public class TeacherStudentController extends BaseController {

    private final TeacherStudentService teacherStudentService;

    public TeacherStudentController(TeacherStudentService teacherStudentService, UserService userService) {
        super(userService);
        this.teacherStudentService = teacherStudentService;
    }

    @GetMapping("/{studentId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取学生概况")
    public ResponseEntity<ApiResponse<TeacherStudentProfileResponse>> getStudentProfile(@PathVariable Long studentId) {
        TeacherStudentProfileResponse profile = teacherStudentService.getStudentProfile(getCurrentUserId(), studentId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping("/{studentId}/courses")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取学生参与课程列表（可按教师过滤）")
    public ResponseEntity<ApiResponse<List<Course>>> getStudentCourses(@PathVariable Long studentId) {
        List<Course> courses = teacherStudentService.getStudentCourses(getCurrentUserId(), studentId);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/basic")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取课程学生基础列表（用于老师端雷达图下拉）")
    public ResponseEntity<ApiResponse<CourseStudentBasicResponse>> getCourseStudentsBasic(
            @RequestParam Long courseId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10000") Integer size,
            @RequestParam(required = false) String keyword
    ) {
        CourseStudentBasicResponse resp = teacherStudentService.getCourseStudentsBasic(
                getCurrentUserId(), courseId, page, size, keyword
        );
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @GetMapping("/{studentId}/activity")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取学生近期学习活跃度与最近学习记录")
    public ResponseEntity<ApiResponse<TeacherStudentActivityResponse>> getStudentActivity(@PathVariable Long studentId,
                                                                                          @RequestParam(required = false, defaultValue = "7") Integer days,
                                                                                          @RequestParam(required = false, defaultValue = "5") Integer limit) {
        TeacherStudentActivityResponse resp = teacherStudentService.getStudentActivity(getCurrentUserId(), studentId, days, limit);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @GetMapping("/{studentId}/alerts")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取学生风险预警")
    public ResponseEntity<ApiResponse<TeacherStudentAlertsResponse>> getStudentAlerts(@PathVariable Long studentId) {
        TeacherStudentAlertsResponse resp = teacherStudentService.getStudentAlerts(getCurrentUserId(), studentId);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @GetMapping("/{studentId}/recommendations")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取学生个性化建议（按优先级）")
    public ResponseEntity<ApiResponse<java.util.List<LearningRecommendation>>> getStudentRecommendations(@PathVariable Long studentId,
                                                                                                        @RequestParam(required = false, defaultValue = "6") Integer limit) {
        java.util.List<LearningRecommendation> list = teacherStudentService.getStudentRecommendations(getCurrentUserId(), studentId, limit);
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}


