package com.noncore.assessment.controller.admin;

import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.dto.request.HelpTicketReplyRequest;
import com.noncore.assessment.dto.request.HelpTicketStatusUpdateRequest;
import com.noncore.assessment.dto.response.HelpTicketDetailResponse;
import com.noncore.assessment.entity.HelpTicket;
import com.noncore.assessment.service.HelpService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import com.noncore.assessment.util.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/help/tickets")
@Tag(name = "管理员帮助工单", description = "管理员查看、回复和处理支持单")
@PreAuthorize("hasRole('ADMIN')")
public class AdminHelpController extends BaseController {

    private final HelpService helpService;

    public AdminHelpController(HelpService helpService, UserService userService) {
        super(userService);
        this.helpService = helpService;
    }

    @GetMapping
    @Operation(summary = "分页查看支持单")
    public ResponseEntity<ApiResponse<PageResult<HelpTicket>>> pageTickets(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String sourceRole,
            @RequestParam(required = false, name = "q") String keyword
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                helpService.pageTicketsForAdmin(page, size, status, channel, priority, sourceRole, keyword)
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "支持单详情")
    public ResponseEntity<ApiResponse<HelpTicketDetailResponse>> ticketDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(helpService.getAdminTicketDetail(id)));
    }

    @PostMapping("/{id}/reply")
    @Operation(summary = "回复支持单")
    public ResponseEntity<ApiResponse<HelpTicketDetailResponse>> replyTicket(@PathVariable Long id,
                                                                             @RequestBody HelpTicketReplyRequest body) {
        return ResponseEntity.ok(ApiResponse.success(
                helpService.adminReplyTicket(getCurrentUserId(), getCurrentUser().getRole(), id, body.getContent())
        ));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新支持单状态")
    public ResponseEntity<ApiResponse<HelpTicket>> updateStatus(@PathVariable Long id,
                                                                @RequestBody HelpTicketStatusUpdateRequest body) {
        return ResponseEntity.ok(ApiResponse.success(helpService.updateTicketStatus(getCurrentUserId(), id, body.getStatus())));
    }
}
