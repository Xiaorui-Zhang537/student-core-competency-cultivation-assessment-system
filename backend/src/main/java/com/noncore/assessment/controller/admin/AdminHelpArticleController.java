package com.noncore.assessment.controller.admin;

import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.dto.request.HelpArticleUpsertRequest;
import com.noncore.assessment.entity.HelpArticle;
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
@RequestMapping("/admin/help/articles")
@Tag(name = "管理员帮助文章", description = "管理员新增、编辑、删除帮助文章")
@PreAuthorize("hasRole('ADMIN')")
public class AdminHelpArticleController extends BaseController {

    private final HelpService helpService;

    public AdminHelpArticleController(HelpService helpService, UserService userService) {
        super(userService);
        this.helpService = helpService;
    }

    @GetMapping
    @Operation(summary = "查看帮助文章列表")
    public ResponseEntity<ApiResponse<List<HelpArticle>>> listArticles(
            @RequestParam(required = false, name = "q") String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean published
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                helpService.listArticlesForAdmin(keyword, categoryId, published)
        ));
    }

    @PostMapping
    @Operation(summary = "新增帮助文章")
    public ResponseEntity<ApiResponse<HelpArticle>> createArticle(@RequestBody HelpArticleUpsertRequest body) {
        return ResponseEntity.ok(ApiResponse.success(helpService.createArticleForAdmin(body)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新帮助文章")
    public ResponseEntity<ApiResponse<HelpArticle>> updateArticle(@PathVariable Long id,
                                                                  @RequestBody HelpArticleUpsertRequest body) {
        return ResponseEntity.ok(ApiResponse.success(helpService.updateArticleForAdmin(id, body)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除帮助文章")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable Long id) {
        helpService.deleteArticleForAdmin(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
