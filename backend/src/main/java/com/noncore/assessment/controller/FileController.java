package com.noncore.assessment.controller;

import com.noncore.assessment.entity.FileRecord;
import com.noncore.assessment.service.FileStorageService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 文件管理控制器
 * 处理文件上传、下载、删除等操作
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@RestController
@RequestMapping("/api/files")
@Tag(name = "文件管理", description = "文件上传、下载、删除等操作")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传文件到服务器")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    public ApiResponse<FileRecord> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "purpose", defaultValue = "general") String purpose,
            @RequestParam(value = "relatedId", required = false) Long relatedId) {
        try {
            Long userId = getCurrentUserId();
            FileRecord fileRecord = fileStorageService.uploadFile(file, userId, purpose, relatedId);
            return ApiResponse.success("文件上传成功", fileRecord);
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     */
    @GetMapping("/{fileId}/download")
    @Operation(summary = "下载文件", description = "根据文件ID下载文件")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) {
        try {
            Long userId = getCurrentUserId();
            
            // 获取文件信息
            FileRecord fileRecord = fileStorageService.getFileInfo(fileId);
            if (fileRecord == null) {
                return ResponseEntity.notFound().build();
            }

            // 下载文件
            byte[] fileBytes = fileStorageService.downloadFile(fileId, userId);

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(fileRecord.getMimeType()));
            headers.setContentLength(fileBytes.length);
            
            // 处理文件名编码
            String encodedFilename = URLEncoder.encode(fileRecord.getOriginalName(), StandardCharsets.UTF_8);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileBytes);

        } catch (Exception e) {
            logger.error("文件下载失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{fileId}")
    @Operation(summary = "删除文件", description = "根据文件ID删除文件")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    public ApiResponse<Void> deleteFile(@PathVariable Long fileId) {
        try {
            Long userId = getCurrentUserId();
            boolean success = fileStorageService.deleteFile(fileId, userId);
            
            if (success) {
                return ApiResponse.success("文件删除成功");
            } else {
                return ApiResponse.error("文件删除失败");
            }
        } catch (Exception e) {
            logger.error("文件删除失败", e);
            return ApiResponse.error("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/{fileId}/info")
    @Operation(summary = "获取文件信息", description = "根据文件ID获取文件详细信息")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    public ApiResponse<FileRecord> getFileInfo(@PathVariable Long fileId) {
        try {
            FileRecord fileRecord = fileStorageService.getFileInfo(fileId);
            if (fileRecord != null) {
                return ApiResponse.success("获取文件信息成功", fileRecord);
            } else {
                return ApiResponse.error("文件不存在");
            }
        } catch (Exception e) {
            logger.error("获取文件信息失败", e);
            return ApiResponse.error("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户文件列表
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的文件列表", description = "获取当前用户上传的文件列表")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    public ApiResponse<List<FileRecord>> getMyFiles(
            @RequestParam(value = "purpose", required = false) String purpose) {
        try {
            Long userId = getCurrentUserId();
            List<FileRecord> files = fileStorageService.getUserFiles(userId, purpose);
            return ApiResponse.success("获取文件列表成功", files);
        } catch (Exception e) {
            logger.error("获取文件列表失败", e);
            return ApiResponse.error("获取文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取关联对象的文件列表
     */
    @GetMapping("/related")
    @Operation(summary = "获取关联文件列表", description = "获取指定用途和关联ID的文件列表")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    public ApiResponse<List<FileRecord>> getRelatedFiles(
            @RequestParam("purpose") String purpose,
            @RequestParam("relatedId") Long relatedId) {
        try {
            List<FileRecord> files = fileStorageService.getRelatedFiles(purpose, relatedId);
            return ApiResponse.success("获取关联文件列表成功", files);
        } catch (Exception e) {
            logger.error("获取关联文件列表失败", e);
            return ApiResponse.error("获取关联文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取存储使用量
     */
    @GetMapping("/storage/usage")
    @Operation(summary = "获取存储使用量", description = "获取当前用户的存储空间使用量")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    public ApiResponse<Map<String, Object>> getStorageUsage() {
        try {
            Long userId = getCurrentUserId();
            Long usage = fileStorageService.getUserStorageUsage(userId);
            
            Map<String, Object> result = Map.of(
                "usage", usage,
                "usageFormatted", formatFileSize(usage),
                "limit", 100 * 1024 * 1024L, // 100MB限制
                "limitFormatted", "100MB"
            );
            
            return ApiResponse.success("获取存储使用量成功", result);
        } catch (Exception e) {
            logger.error("获取存储使用量失败", e);
            return ApiResponse.error("获取存储使用量失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除文件
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除文件", description = "批量删除多个文件")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    public ApiResponse<Map<String, Object>> batchDeleteFiles(@RequestBody List<Long> fileIds) {
        try {
            Long userId = getCurrentUserId();
            Map<String, Object> result = fileStorageService.batchDeleteFiles(fileIds, userId);
            return ApiResponse.success("批量删除文件完成", result);
        } catch (Exception e) {
            logger.error("批量删除文件失败", e);
            return ApiResponse.error("批量删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 图片预览
     */
    @GetMapping("/{fileId}/preview")
    @Operation(summary = "图片预览", description = "预览图片文件")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<byte[]> previewImage(@PathVariable Long fileId) {
        try {
            Long userId = getCurrentUserId();
            
            // 获取文件信息
            FileRecord fileRecord = fileStorageService.getFileInfo(fileId);
            if (fileRecord == null) {
                return ResponseEntity.notFound().build();
            }

            // 检查是否为图片文件
            String mimeType = fileRecord.getMimeType();
            if (mimeType == null || !mimeType.startsWith("image/")) {
                return ResponseEntity.badRequest().build();
            }

            // 下载文件
            byte[] fileBytes = fileStorageService.downloadFile(fileId, userId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mimeType));
            headers.setContentLength(fileBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileBytes);

        } catch (Exception e) {
            logger.error("图片预览失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // 私有辅助方法

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.valueOf(authentication.getName());
    }

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
        }
    }
} 