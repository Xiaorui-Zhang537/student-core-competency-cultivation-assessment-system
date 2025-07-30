package com.noncore.assessment.controller;

import com.noncore.assessment.entity.Course;
import com.noncore.assessment.service.CourseService;
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
import com.noncore.assessment.entity.User;
import com.noncore.assessment.mapper.UserMapper;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;

/**
 * 课程控制器
 * 提供课程相关的API接口
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@RestController
@RequestMapping("/api/courses")
@Tag(name = "课程管理", description = "课程创建、查询、更新、删除等相关接口")
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    private final CourseService courseService;
    private final UserMapper userMapper;

    public CourseController(CourseService courseService, UserMapper userMapper) {
        this.courseService = courseService;
        this.userMapper = userMapper;
    }

    /**
     * 分页查询课程列表
     */
    @GetMapping
    @Operation(summary = "分页查询课程", description = "支持关键词搜索、分类筛选、难度筛选等")
    public ApiResponse<PageResult<Course>> getCourses(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "课程分类") @RequestParam(required = false) String category,
            @Parameter(description = "难度级别") @RequestParam(required = false) String difficulty,
            @Parameter(description = "课程状态") @RequestParam(required = false) String status) {
        try {
            PageResult<Course> result = courseService.getCourses(page, size, keyword, category, difficulty, status);
            return ApiResponse.success("查询成功", result);
        } catch (Exception e) {
            logger.error("查询课程列表失败: {}", e.getMessage(), e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 根据ID获取课程详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取课程详情", description = "根据课程ID获取详细信息")
    public ApiResponse<Course> getCourseById(@PathVariable Long id) {
        try {
            Course course = courseService.getCourseById(id);
            return ApiResponse.success("获取成功", course);
        } catch (Exception e) {
            logger.error("获取课程详情失败: courseId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(404, e.getMessage());
        }
    }

    /**
     * 创建新课程
     */
    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "创建课程", description = "教师创建新课程")
    public ApiResponse<Course> createCourse(@Valid @RequestBody Course course) {
        try {
            // 设置当前用户为课程教师
            Long currentUserId = getCurrentUserId();
            course.setTeacherId(currentUserId);
            
            Course createdCourse = courseService.createCourse(course);
            return ApiResponse.success("课程创建成功", createdCourse);
        } catch (Exception e) {
            logger.error("创建课程失败: {}", e.getMessage(), e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 更新课程信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "更新课程", description = "更新课程信息")
    public ApiResponse<Course> updateCourse(@PathVariable Long id, @Valid @RequestBody Course course) {
        try {
            Course updatedCourse = courseService.updateCourse(id, course);
            return ApiResponse.success("课程更新成功", updatedCourse);
        } catch (Exception e) {
            logger.error("更新课程失败: courseId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "删除课程", description = "删除指定课程")
    public ApiResponse<String> deleteCourse(@PathVariable Long id) {
        try {
            courseService.deleteCourse(id);
            return ApiResponse.success("课程删除成功", "deleted");
        } catch (Exception e) {
            logger.error("删除课程失败: courseId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 学生选课
     */
    @PostMapping("/{id}/enroll")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(summary = "学生选课", description = "学生报名参加课程")
    public ApiResponse<String> enrollCourse(@PathVariable Long id) {
        try {
            Long studentId = getCurrentUserId();
            courseService.enrollCourse(id, studentId);
            return ApiResponse.success("选课成功", "enrolled");
        } catch (Exception e) {
            logger.error("选课失败: courseId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 学生退课
     */
    @DeleteMapping("/{id}/enroll")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(summary = "学生退课", description = "学生退出已选课程")
    public ApiResponse<String> unenrollCourse(@PathVariable Long id) {
        try {
            Long studentId = getCurrentUserId();
            courseService.unenrollCourse(id, studentId);
            return ApiResponse.success("退课成功", "unenrolled");
        } catch (Exception e) {
            logger.error("退课失败: courseId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 检查选课状态
     */
    @GetMapping("/{id}/enrollment-status")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(summary = "检查选课状态", description = "检查学生是否已选择该课程")
    public ApiResponse<Boolean> checkEnrollmentStatus(@PathVariable Long id) {
        try {
            Long studentId = getCurrentUserId();
            boolean enrolled = courseService.isStudentEnrolled(id, studentId);
            return ApiResponse.success("查询成功", enrolled);
        } catch (Exception e) {
            logger.error("检查选课状态失败: courseId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 发布课程
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "发布课程", description = "将课程状态设置为已发布")
    public ApiResponse<String> publishCourse(@PathVariable Long id) {
        try {
            courseService.publishCourse(id);
            return ApiResponse.success("课程发布成功", "published");
        } catch (Exception e) {
            logger.error("发布课程失败: courseId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 下架课程
     */
    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "下架课程", description = "将课程状态设置为草稿")
    public ApiResponse<String> unpublishCourse(@PathVariable Long id) {
        try {
            courseService.unpublishCourse(id);
            return ApiResponse.success("课程下架成功", "unpublished");
        } catch (Exception e) {
            logger.error("下架课程失败: courseId={}, error={}", id, e.getMessage(), e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 获取我的课程（教师）
     */
    @GetMapping("/my-courses")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取我的课程", description = "教师查看自己创建的课程")
    public ApiResponse<List<Course>> getMyCourses() {
        try {
            Long teacherId = getCurrentUserId();
            List<Course> courses = courseService.getCoursesByTeacher(teacherId);
            return ApiResponse.success("查询成功", courses);
        } catch (Exception e) {
            logger.error("获取我的课程失败: {}", e.getMessage(), e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 获取我的选课（学生）
     */
    @GetMapping("/my-enrollments")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(summary = "获取我的选课", description = "学生查看已选择的课程")
    public ApiResponse<List<Course>> getMyEnrollments() {
        try {
            Long studentId = getCurrentUserId();
            List<Course> courses = courseService.getEnrolledCourses(studentId);
            return ApiResponse.success("查询成功", courses);
        } catch (Exception e) {
            logger.error("获取我的选课失败: {}", e.getMessage(), e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 获取热门课程
     */
    @GetMapping("/popular")
    @Operation(summary = "获取热门课程", description = "获取报名人数最多的课程")
    public ApiResponse<List<Course>> getPopularCourses(
            @Parameter(description = "限制数量", example = "10") @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Course> courses = courseService.getPopularCourses(limit);
            return ApiResponse.success("查询成功", courses);
        } catch (Exception e) {
            logger.error("获取热门课程失败: {}", e.getMessage(), e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 获取推荐课程
     */
    @GetMapping("/recommended")
    @Operation(summary = "获取推荐课程", description = "获取评分最高的课程")
    public ApiResponse<List<Course>> getRecommendedCourses(
            @Parameter(description = "限制数量", example = "10") @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Course> courses = courseService.getRecommendedCourses(limit);
            return ApiResponse.success("查询成功", courses);
        } catch (Exception e) {
            logger.error("获取推荐课程失败: {}", e.getMessage(), e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 根据分类获取课程
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "分类课程", description = "根据分类获取课程列表")
    public ApiResponse<List<Course>> getCoursesByCategory(@PathVariable String category) {
        try {
            List<Course> courses = courseService.getCoursesByCategory(category);
            return ApiResponse.success("查询成功", courses);
        } catch (Exception e) {
            logger.error("按分类获取课程失败: category={}, error={}", category, e.getMessage(), e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 搜索课程
     */
    @GetMapping("/search")
    @Operation(summary = "搜索课程", description = "根据关键词搜索课程")
    public ApiResponse<List<Course>> searchCourses(
            @Parameter(description = "搜索关键词", required = true) @RequestParam String keyword) {
        try {
            List<Course> courses = courseService.searchCourses(keyword);
            return ApiResponse.success("搜索成功", courses);
        } catch (Exception e) {
            logger.error("搜索课程失败: keyword={}, error={}", keyword, e.getMessage(), e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 获取课程统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "获取课程统计", description = "获取课程相关的统计信息")
    public ApiResponse<Map<String, Object>> getCourseStatistics(
            @Parameter(description = "教师ID，不传则统计所有") @RequestParam(required = false) Long teacherId) {
        try {
            // 如果没有传teacherId且当前用户是教师，则统计当前教师的课程
            if (teacherId == null && hasRole("TEACHER")) {
                teacherId = getCurrentUserId();
            }
            Map<String, Object> statistics = courseService.getCourseStatistics(teacherId);
            return ApiResponse.success("统计成功", statistics);
        } catch (Exception e) {
            logger.error("获取课程统计失败: {}", e.getMessage(), e);
            return ApiResponse.error(500, e.getMessage());
        }
    }

    /**
     * 批量更新课程状态
     */
    @PutMapping("/batch-status")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "批量更新状态", description = "批量更新多个课程的状态")
    public ApiResponse<String> batchUpdateStatus(@RequestBody BatchUpdateRequest request) {
        try {
            courseService.batchUpdateStatus(request.getCourseIds(), request.getStatus());
            return ApiResponse.success("批量更新成功", "updated");
        } catch (Exception e) {
            logger.error("批量更新课程状态失败: {}", e.getMessage(), e);
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof String) {
                String username = (String) authentication.getPrincipal();
                User user = userMapper.selectByUsername(username);
                return user != null ? user.getId() : null;
            }
            return null;
        } catch (Exception e) {
            logger.error("获取当前用户信息失败", e);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "无法获取当前用户信息");
        }
    }

    /**
     * 检查当前用户是否有指定角色
     */
    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
        }
        return false;
    }



    /**
     * 批量更新请求DTO
     */
    public static class BatchUpdateRequest {
        private List<Long> courseIds;
        private String status;

        public List<Long> getCourseIds() {
            return courseIds;
        }

        public void setCourseIds(List<Long> courseIds) {
            this.courseIds = courseIds;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
} 