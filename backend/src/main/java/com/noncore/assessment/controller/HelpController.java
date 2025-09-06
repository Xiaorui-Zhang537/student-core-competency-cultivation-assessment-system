package com.noncore.assessment.controller;

import com.noncore.assessment.entity.HelpArticle;
import com.noncore.assessment.entity.HelpArticleFeedback;
import com.noncore.assessment.entity.HelpCategory;
import com.noncore.assessment.entity.HelpTicket;
import com.noncore.assessment.dto.request.HelpTicketCreateRequest;
import com.noncore.assessment.service.HelpService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/help")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "http://0.0.0.0:5173"}, allowCredentials = "true")
@Tag(name = "帮助中心", description = "帮助中心分类、文章、反馈与工单")
public class HelpController extends BaseController {

    private final HelpService helpService;

    public HelpController(HelpService helpService, UserService userService) {
        super(userService);
        this.helpService = helpService;
    }

    @GetMapping("/categories")
    @Operation(summary = "分类列表")
    public ResponseEntity<ApiResponse<List<HelpCategory>>> categories() {
        return ResponseEntity.ok(ApiResponse.success(helpService.listCategories()));
    }

    @GetMapping("/articles")
    @Operation(summary = "文章列表")
    public ResponseEntity<ApiResponse<List<HelpArticle>>> articles(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        return ResponseEntity.ok(ApiResponse.success(helpService.listArticles(q, categoryId, tag, sort)));
    }

    @GetMapping("/articles/{slug}")
    @Operation(summary = "文章详情")
    public ResponseEntity<ApiResponse<HelpArticle>> article(@PathVariable String slug,
                                                            @RequestParam(defaultValue = "true") boolean inc) {
        return ResponseEntity.ok(ApiResponse.success(helpService.getArticleBySlug(slug, inc)));
    }

    @PostMapping("/articles/{id}/feedback")
    @Operation(summary = "提交文章反馈")
    public ResponseEntity<ApiResponse<Void>> articleFeedback(@PathVariable Long id,
                                                             @RequestParam(required = false) Boolean helpful,
                                                             @RequestParam(required = false) String content) {
        HelpArticleFeedback f = new HelpArticleFeedback();
        f.setArticleId(id);
        Long uid = getOptionalUserId();
        f.setUserId(uid);
        f.setHelpful(helpful);
        f.setContent(content);
        helpService.submitArticleFeedback(f);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/tickets")
    @Operation(summary = "创建工单")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<HelpTicket>> createTicket(@RequestBody HelpTicketCreateRequest body) {
        HelpTicket t = helpService.createTicket(getCurrentUserId(), body.getTitle(), body.getDescription());
        return ResponseEntity.ok(ApiResponse.success(t));
    }

    @GetMapping("/tickets")
    @Operation(summary = "我的工单列表")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<HelpTicket>>> myTickets() {
        return ResponseEntity.ok(ApiResponse.success(helpService.listMyTickets(getCurrentUserId())));
    }

    @PutMapping("/tickets/{id}")
    @Operation(summary = "编辑我的工单")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<HelpTicket>> updateTicket(@PathVariable Long id,
                                                                @RequestBody HelpTicketCreateRequest body) {
        HelpTicket updated = helpService.updateMyTicket(getCurrentUserId(), id, body.getTitle(), body.getDescription());
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/tickets/{id}")
    @Operation(summary = "删除/撤回我的工单")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteTicket(@PathVariable Long id) {
        helpService.deleteMyTicket(getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}


