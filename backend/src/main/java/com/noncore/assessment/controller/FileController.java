package com.noncore.assessment.controller;

import com.noncore.assessment.entity.FileRecord;
import com.noncore.assessment.service.FileStorageService;
import com.noncore.assessment.service.UserService;
import com.noncore.assessment.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/files")
@Tag(name = "文件管理", description = "文件上传、下载、删除等操作")
public class FileController extends BaseController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService, UserService userService) {
        super(userService);
        this.fileStorageService = fileStorageService;
    }

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传文件到服务器")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FileRecord>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "purpose", defaultValue = "general") String purpose,
            @RequestParam(value = "relatedId", required = false) Long relatedId) {
        FileRecord fileRecord = fileStorageService.uploadFile(file, getCurrentUserId(), purpose, relatedId);
        return ResponseEntity.ok(ApiResponse.success(fileRecord));
    }

    /**
     * 下载文件
     */
    @GetMapping("/{fileId}/download")
    @Operation(summary = "下载文件", description = "根据文件ID下载文件")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) {
        Long userId = getCurrentUserId();

        FileRecord fileRecord = fileStorageService.getFileInfo(fileId);
        if (fileRecord == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] fileBytes = fileStorageService.downloadFile(fileId, userId);

        HttpHeaders headers = new HttpHeaders();
        String mime = safeMimeType(fileRecord);
        headers.setContentType(MediaType.parseMediaType(mime));
        headers.setContentLength(fileBytes.length);

        String encodedFilename = URLEncoder.encode(fileRecord.getOriginalName(), StandardCharsets.UTF_8);
        // 浏览器PDF工具栏显示文件名依赖 Content-Disposition: inline; filename
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileRecord.getOriginalName() + "\"; filename*=UTF-8''" + encodedFilename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileBytes);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{fileId}")
    @Operation(summary = "删除文件", description = "根据文件ID删除文件")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Long fileId) {
        fileStorageService.deleteFile(fileId, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/{fileId}/info")
    @Operation(summary = "获取文件信息", description = "根据文件ID获取文件详细信息")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FileRecord>> getFileInfo(@PathVariable Long fileId) {
        FileRecord fileRecord = fileStorageService.getFileInfo(fileId);
        return ResponseEntity.ok(ApiResponse.success(fileRecord));
    }

    /**
     * 获取用户文件列表
     */
    @GetMapping("/my")
    @Operation(summary = "获取我的文件列表", description = "获取当前用户上传的文件列表")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<FileRecord>>> getMyFiles(
            @RequestParam(value = "purpose", required = false) String purpose) {
        List<FileRecord> files = fileStorageService.getUserFiles(getCurrentUserId(), purpose);
        return ResponseEntity.ok(ApiResponse.success(files));
    }

    /**
     * 获取关联对象的文件列表
     */
    @GetMapping("/related")
    @Operation(summary = "获取关联文件列表", description = "获取指定用途和关联ID的文件列表")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<FileRecord>>> getRelatedFiles(
            @RequestParam("purpose") String purpose,
            @RequestParam("relatedId") Long relatedId) {
        List<FileRecord> files = fileStorageService.getRelatedFiles(purpose, relatedId);
        return ResponseEntity.ok(ApiResponse.success(files));
    }

    /**
     * 获取存储使用量
     */
    @GetMapping("/storage/usage")
    @Operation(summary = "获取存储使用量", description = "获取当前用户的存储空间使用量")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStorageUsage() {
        Long usage = fileStorageService.getUserStorageUsage(getCurrentUserId());
        Map<String, Object> result = Map.of(
            "usage", usage,
            "usageFormatted", formatFileSize(usage),
            "limit", 100 * 1024 * 1024L, // 100MB限制
            "limitFormatted", "100MB"
        );
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 批量删除文件
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除文件", description = "批量删除多个文件")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchDeleteFiles(@RequestBody List<Long> fileIds) {
        Map<String, Object> result = fileStorageService.batchDeleteFiles(fileIds, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 预览文件（支持图片与 PDF 内嵌预览）
     */
    @GetMapping("/{fileId}/preview")
    @Operation(summary = "文件预览", description = "预览图片与 PDF 文件（inline）")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> previewImage(@PathVariable Long fileId) {
        Long userId = getCurrentUserId();

        FileRecord fileRecord = fileStorageService.getFileInfo(fileId);
        if (fileRecord == null) {
            return ResponseEntity.notFound().build();
        }

        String mimeType = safeMimeType(fileRecord);
        String mt = mimeType == null ? "" : mimeType.toLowerCase();
        boolean previewable = mt.startsWith("image/") || mt.startsWith("application/pdf");
        if (!previewable) return ResponseEntity.badRequest().build();

        byte[] fileBytes = fileStorageService.downloadFile(fileId, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mimeType));
        headers.setContentLength(fileBytes.length);
        // 对 PDF 设置 inline filename，确保浏览器 PDF 查看器显示原始文件名
        if (mt.startsWith("application/pdf")) {
            String original = fileRecord.getOriginalName();
            String encodedFilename = URLEncoder.encode(original, StandardCharsets.UTF_8);
            headers.set(HttpHeaders.CONTENT_DISPOSITION,
                    "inline; filename=\"" + original + "\"; filename*=UTF-8''" + encodedFilename);
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileBytes);
    }

    private String safeMimeType(FileRecord f) {
        String mt = f.getMimeType();
        if (mt != null && !mt.isBlank()) return mt;
        String name = f.getOriginalName();
        String ext = null;
        if (name != null && name.contains(".")) {
            ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
        }
        if (ext == null || ext.isBlank()) return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        return switch (ext) {
            case "png" -> MediaType.IMAGE_PNG_VALUE;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG_VALUE;
            case "gif" -> MediaType.IMAGE_GIF_VALUE;
            case "pdf" -> MediaType.APPLICATION_PDF_VALUE;
            case "mp4" -> "video/mp4";
            case "mov" -> "video/quicktime";
            case "avi" -> "video/x-msvideo";
            case "mkv" -> "video/x-matroska";
            case "webm" -> "video/webm";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE;
        };
    }

    // 私有辅助方法

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