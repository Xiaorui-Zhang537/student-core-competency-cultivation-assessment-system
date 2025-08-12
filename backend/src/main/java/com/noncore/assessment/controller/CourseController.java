package com.noncore.assessment.controller;

import com.noncore.assessment.dto.response.CourseStatisticsResponse;
import com.noncore.assessment.dto.request.InviteStudentsRequest;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.service.CourseDiscoveryService;
import com.noncore.assessment.service.CourseService;
import com.noncore.assessment.service.EnrollmentService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.noncore.assessment.dto.request.BatchCourseStatusRequest;

import java.util.List;
import com.noncore.assessment.entity.User;

@RestController
@RequestMapping("/courses")
@Tag(name = "课程管理", description = "课程创建、查询、更新、删除等相关接口")
public class CourseController extends BaseController {

    private final CourseService courseService;
    private final CourseDiscoveryService courseDiscoveryService;
    private final EnrollmentService enrollmentService;
    public CourseController(CourseService courseService, CourseDiscoveryService courseDiscoveryService, EnrollmentService enrollmentService, UserService userService) {
        super(userService);
        this.courseService = courseService;
        this.courseDiscoveryService = courseDiscoveryService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    @Operation(summary = "分页查询课程", description = "支持关键词搜索、分类筛选、难度筛选等")
    public ResponseEntity<ApiResponse<PageResult<Course>>> getCourses(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false, name = "query") String query,
            @Parameter(description = "课程分类") @RequestParam(required = false) String category,
            @Parameter(description = "难度级别") @RequestParam(required = false) String difficulty,
            @Parameter(description = "课程状态") @RequestParam(required = false) String status,
            @Parameter(description = "教师ID") @RequestParam(required = false) Long teacherId) {
        PageResult<Course> result = courseDiscoveryService.getCourses(page, size, query, category, difficulty, status, teacherId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取课程详情", description = "根据课程ID获取详细信息")
    public ResponseEntity<ApiResponse<Course>> getCourseById(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success(course));
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "创建课程", description = "教师创建新课程")
    public ResponseEntity<ApiResponse<Course>> createCourse(@Valid @RequestBody Course course) {
        course.setTeacherId(getCurrentUserId());
        Course createdCourse = courseService.createCourse(course);
        return ResponseEntity.ok(ApiResponse.success(createdCourse));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "更新课程", description = "更新课程信息")
    public ResponseEntity<ApiResponse<Course>> updateCourse(@PathVariable Long id, @Valid @RequestBody Course course) {
        Course updatedCourse = courseService.updateCourse(id, course);
        return ResponseEntity.ok(ApiResponse.success(updatedCourse));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "删除课程", description = "删除指定课程")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/{id}/enroll")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(summary = "学生选课", description = "学生报名参加课程")
    public ResponseEntity<ApiResponse<Void>> enrollCourse(@PathVariable Long id) {
        enrollmentService.enrollCourse(id, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{id}/enroll")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(summary = "学生退课", description = "学生退出已选课程")
    public ResponseEntity<ApiResponse<Void>> unenrollCourse(@PathVariable Long id) {
        enrollmentService.unenrollCourse(id, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/{id}/enrollment-status")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(summary = "检查选课状态", description = "检查学生是否已选择该课程")
    public ResponseEntity<ApiResponse<Boolean>> checkEnrollmentStatus(@PathVariable Long id) {
        boolean enrolled = enrollmentService.isStudentEnrolled(id, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(enrolled));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "发布课程", description = "将课程状态设置为已发布")
    public ResponseEntity<ApiResponse<Void>> publishCourse(@PathVariable Long id) {
        courseService.publishCourse(id, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "下架课程", description = "将课程状态设置为草稿")
    public ResponseEntity<ApiResponse<Void>> unpublishCourse(@PathVariable Long id) {
        courseService.unpublishCourse(id, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/popular")
    @Operation(summary = "获取热门课程", description = "获取报名人数最多的课程")
    public ResponseEntity<ApiResponse<List<Course>>> getPopularCourses(
            @Parameter(description = "限制数量", example = "10") @RequestParam(defaultValue = "10") Integer limit) {
        List<Course> courses = courseDiscoveryService.getPopularCourses(limit);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/recommended")
    @Operation(summary = "获取推荐课程", description = "获取评分最高的课程")
    public ResponseEntity<ApiResponse<List<Course>>> getRecommendedCourses(
            @Parameter(description = "限制数量", example = "10") @RequestParam(defaultValue = "10") Integer limit) {
        List<Course> courses = courseDiscoveryService.getRecommendedCourses(limit);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "分类课程", description = "根据分类获取课程列表")
    public ResponseEntity<ApiResponse<List<Course>>> getCoursesByCategory(@PathVariable String category) {
        List<Course> courses = courseDiscoveryService.getCoursesByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索课程", description = "根据关键词搜索课程")
    public ResponseEntity<ApiResponse<List<Course>>> searchCourses(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword) {
        List<Course> courses = courseDiscoveryService.searchCourses(keyword);
        return ResponseEntity.ok(ApiResponse.success(courses));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取课程统计", description = "获取课程相关的统计信息")
    public ResponseEntity<ApiResponse<CourseStatisticsResponse>> getCourseStatistics(
            @Parameter(description = "教师ID，不传则统计所有") @RequestParam(required = false) Long teacherId) {
        if (teacherId == null && hasRole("TEACHER")) {
            teacherId = getCurrentUserId();
        }
        CourseStatisticsResponse statistics = courseService.getCourseStatistics(teacherId);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @PutMapping("/batch-status")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "批量更新状态", description = "批量更新多个课程的状态")
    public ResponseEntity<ApiResponse<Void>> batchUpdateStatus(@RequestBody BatchCourseStatusRequest request) {
        courseService.batchUpdateStatus(request.getCourseIds(), request.getStatus());
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 教师获取课程学生列表（分页）
     */
    @GetMapping("/{id}/students")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取课程学生列表", description = "教师获取自己课程的学生列表（分页）")
    public ResponseEntity<ApiResponse<PageResult<User>>> getCourseStudents(
            @PathVariable("id") Long courseId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "20") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "搜索关键词（昵称/用户名/学号/工号）") @RequestParam(required = false) String search,
            @Parameter(description = "排序字段(name|progress|grade|lastActive|joinDate)") @RequestParam(required = false, defaultValue = "joinDate") String sortBy,
            @Parameter(description = "活跃度筛选(high|medium|low|inactive)") @RequestParam(required = false) String activity,
            @Parameter(description = "成绩筛选(excellent|good|average|below)") @RequestParam(required = false) String grade,
            @Parameter(description = "进度筛选(not-started|in-progress|completed)") @RequestParam(required = false) String progress
    ) {
        PageResult<User> result = enrollmentService.getCourseStudents(getCurrentUserId(), courseId, page, size, search, sortBy, activity, grade, progress);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 教师批量邀请/添加学生到课程
     */
    @PostMapping("/{id}/students/invite")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "批量添加学生到课程", description = "教师批量添加学生到自己的课程（传入学生ID列表）")
    public ResponseEntity<ApiResponse<Void>> inviteStudents(
            @PathVariable("id") Long courseId,
            @Valid @RequestBody InviteStudentsRequest request) {
        enrollmentService.addStudentsToCourse(getCurrentUserId(), courseId, request.getStudentIds());
        return ResponseEntity.ok(ApiResponse.success());
    }
} 