package com.noncore.assessment.controller;

import com.noncore.assessment.dto.response.BehaviorSummaryResponse;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.BehaviorAggregationService;
import com.noncore.assessment.service.TeacherStudentService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 行为证据评价（阶段一）接口：仅返回纯代码聚合的行为摘要，不调用 AI。
 */
@RestController
@RequestMapping("/behavior")
public class BehaviorEvidenceController extends BaseController {

    private final BehaviorAggregationService aggregationService;
    private final TeacherStudentService teacherStudentService;

    public BehaviorEvidenceController(BehaviorAggregationService aggregationService,
                                     TeacherStudentService teacherStudentService,
                                     UserService userService) {
        super(userService);
        this.aggregationService = aggregationService;
        this.teacherStudentService = teacherStudentService;
    }

    /**
     * 获取阶段一行为摘要（纯代码聚合）。
     *
     * <p>强约束：本接口不得触发任何 AI 调用。</p>
     */
    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "行为证据-阶段一摘要（不调用AI）")
    public ResponseEntity<ApiResponse<BehaviorSummaryResponse>> getSummary(@RequestParam(required = false) Long studentId,
                                                                           @RequestParam(required = false) Long courseId,
                                                                           @RequestParam(required = false, defaultValue = "7d") @NotBlank String range) {
        Long current = getCurrentUserId();

        // 学生仅允许查看自己
        if (hasRole("STUDENT")) {
            Long targetStudentId = (studentId == null) ? current : studentId;
            if (!current.equals(targetStudentId)) {
                throw new BusinessException(ErrorCode.PERMISSION_DENIED, "学生仅允许查看自己的行为摘要");
            }
            BehaviorSummaryResponse resp = aggregationService.getOrBuildSummary(targetStudentId, courseId, range);
            return ResponseEntity.ok(ApiResponse.success(resp));
        }

        // 教师/管理员查看：studentId 必填，且必须具备查看权限（复用现有教师-学生权限校验）
        if (studentId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "studentId 必填");
        }
        if (hasRole("TEACHER")) {
            // 若无课程交集会抛出 PERMISSION_DENIED
            teacherStudentService.getStudentProfile(current, studentId);
        }

        BehaviorSummaryResponse resp = aggregationService.getOrBuildSummary(studentId, courseId, range);
        return ResponseEntity.ok(ApiResponse.success(resp));
    }
}

