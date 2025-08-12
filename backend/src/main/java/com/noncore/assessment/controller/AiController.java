package com.noncore.assessment.controller;

import com.noncore.assessment.dto.request.AiChatRequest;
import com.noncore.assessment.dto.response.AiChatResponse;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import com.noncore.assessment.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AiController extends BaseController {

    private final AiService aiService;

    public AiController(AiService aiService, UserService userService) {
        super(userService);
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "AI聊天", description = "基于课程与学生上下文的AI聊天（非流式）")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(@Valid @RequestBody AiChatRequest request) {
        if (request.getStudentIds() != null && request.getStudentIds().size() > 5) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "最多选择 5 名学生");
        }
        String answer = aiService.generateAnswer(request, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(new AiChatResponse(answer)));
    }
}

