package com.noncore.assessment.controller;

import com.noncore.assessment.dto.response.BehaviorInsightResponse;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.BehaviorInsightGenerationService;
import com.noncore.assessment.service.TeacherStudentService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 行为证据评价（阶段二）接口：阶段性调用 AI 做解释与总结（不算分）。
 */
@RestController
@RequestMapping("/behavior/insights")
public class BehaviorInsightController extends BaseController {

    private final BehaviorInsightGenerationService insightGenerationService;
    private final TeacherStudentService teacherStudentService;

    public BehaviorInsightController(BehaviorInsightGenerationService insightGenerationService,
                                     TeacherStudentService teacherStudentService,
                                     UserService userService) {
        super(userService);
        this.insightGenerationService = insightGenerationService;
        this.teacherStudentService = teacherStudentService;
    }

    @PostMapping("/generate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "生成行为洞察（阶段二，AI解读）", description = "按时间窗触发 AI 对阶段一摘要进行解释与总结（不计算分数；测试：学生每日最多7次）")
    public ResponseEntity<ApiResponse<BehaviorInsightResponse>> generate(@RequestParam Long studentId,
                                                                         @RequestParam(required = false) Long courseId,
                                                                         @RequestParam(required = false, defaultValue = "7d") String range,
                                                                         @RequestParam(required = false) String model,
                                                                         @RequestParam(required = false, defaultValue = "false") Boolean force) {
        Long operatorId = getCurrentUserId();
        boolean isStudent = hasRole("STUDENT");
        boolean f = Boolean.TRUE.equals(force);

        if (isStudent) {
            // 学生仅允许触发生成自己的洞察，且不允许 force
            if (!operatorId.equals(studentId)) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED, "学生仅允许生成/查看自己的行为洞察");
            }
            if (f) {
                throw new BusinessException(ErrorCode.INVALID_PARAMETER, "学生不允许 force 重跑");
            }
        } else if (hasRole("TEACHER")) {
            // 教师权限校验：必须与学生有课程交集（复用既有校验）
            teacherStudentService.getStudentProfile(operatorId, studentId);
        }

        BehaviorInsightResponse resp = insightGenerationService.generate(operatorId, studentId, courseId, range, model, f, isStudent);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取最新行为洞察（阶段二）")
    public ResponseEntity<ApiResponse<BehaviorInsightResponse>> getLatest(@RequestParam Long studentId,
                                                                          @RequestParam(required = false) Long courseId,
                                                                          @RequestParam(required = false, defaultValue = "7d") String range) {
        Long operatorId = getCurrentUserId();
        if (hasRole("STUDENT") && !operatorId.equals(studentId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "学生仅允许查看自己的行为洞察");
        }
        if (hasRole("TEACHER")) {
            teacherStudentService.getStudentProfile(operatorId, studentId);
        }
        BehaviorInsightResponse resp = insightGenerationService.getLatest(operatorId, studentId, courseId, range);
        // 学生端冷却/额度提示由 generationService.getLatest 注入（测试：每日最多7次）
        return ResponseEntity.ok(ApiResponse.success(resp));
    }
}

