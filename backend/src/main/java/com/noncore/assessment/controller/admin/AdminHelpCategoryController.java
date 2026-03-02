package com.noncore.assessment.controller.admin;

import com.noncore.assessment.controller.BaseController;
import com.noncore.assessment.dto.request.HelpCategoryCreateRequest;
import com.noncore.assessment.entity.HelpCategory;
import com.noncore.assessment.service.HelpService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/help/categories")
@Tag(name = "管理员帮助分类", description = "管理员新增帮助分类")
@PreAuthorize("hasRole('ADMIN')")
public class AdminHelpCategoryController extends BaseController {

    private final HelpService helpService;

    public AdminHelpCategoryController(HelpService helpService, UserService userService) {
        super(userService);
        this.helpService = helpService;
    }

    @PostMapping
    @Operation(summary = "新增帮助分类")
    public ResponseEntity<ApiResponse<HelpCategory>> createCategory(@RequestBody HelpCategoryCreateRequest body) {
        return ResponseEntity.ok(ApiResponse.success(helpService.createCategoryForAdmin(body)));
    }
}
