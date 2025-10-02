package com.noncore.assessment.controller;

import com.noncore.assessment.service.ProjectTreeService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/project")
@Tag(name = "项目", description = "公开项目信息接口")
public class ProjectController {

    private final ProjectTreeService projectTreeService;

    public ProjectController(ProjectTreeService projectTreeService) {
        this.projectTreeService = projectTreeService;
    }

    @GetMapping("/tree")
    @Operation(summary = "获取项目目录树（公开）", description = "返回 backend/frontend/docs 的目录结构，有限深度")
    public ResponseEntity<ApiResponse<List<ProjectTreeService.Node>>> getTree(
            @RequestParam(value = "depth", required = false, defaultValue = "0") Integer depth
    ) {
        // depth=0 => 无限制（谨慎使用）；其余 1..10
        int d = depth == null ? 5 : (depth == 0 ? 0 : Math.max(1, Math.min(depth, 10)));
        List<ProjectTreeService.Node> data = projectTreeService.readProjectTree(d);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}


