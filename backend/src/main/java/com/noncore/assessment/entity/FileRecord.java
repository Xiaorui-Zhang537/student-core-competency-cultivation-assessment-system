package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件记录实体类
 * 对应数据库file_records表
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Schema(description = "文件记录实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileRecord {

    @Schema(description = "文件ID", example = "1")
    private Long id;

    @Schema(description = "原始文件名", example = "homework.pdf")
    private String originalName;

    @Schema(description = "存储文件名", example = "20241228_103000_homework.pdf")
    private String storedName;

    @Schema(description = "文件路径", example = "/uploads/assignments/20241228_103000_homework.pdf")
    private String filePath;

    @Schema(description = "文件大小（字节）", example = "1024000")
    private Long fileSize;

    @Schema(description = "文件类型", example = "pdf")
    private String fileType;

    @Schema(description = "MIME类型", example = "application/pdf")
    private String mimeType;

    @Schema(description = "文件内容类型", example = "application/pdf")
    private String contentType;

    @Schema(description = "文件扩展名", example = "pdf")
    private String extension;

    @Schema(description = "上传目的", example = "assignment")
    private String uploadPurpose;

    @Schema(description = "文件MD5值", example = "d41d8cd98f00b204e9800998ecf8427e")
    private String md5Hash;

    @Schema(description = "上传者ID", example = "1")
    private Long uploaderId;

    @Schema(description = "关联类型", example = "assignment", allowableValues = {"assignment", "submission", "course", "profile"})
    private String relatedType;

    @Schema(description = "关联ID", example = "1")
    private Long relatedId;

    @Schema(description = "文件状态", example = "active", allowableValues = {"active", "deleted", "archived"})
    @Builder.Default
    private String status = "active";

    @Schema(description = "下载次数", example = "10")
    @Builder.Default
    private Integer downloadCount = 0;

    @Schema(description = "最后下载时间", example = "2024-12-28 15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastDownloadAt;

    @Schema(description = "创建时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Schema(description = "更新时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Schema(description = "是否删除", example = "false")
    @Builder.Default
    private Boolean deleted = false;
    
    /**
     * 是否为图片文件
     */
    public boolean isImage() {
        if (contentType == null) return false;
        return contentType.startsWith("image/");
    }

    /**
     * 是否为文档文件
     */
    public boolean isDocument() {
        if (extension == null) return false;
        return extension.matches("pdf|doc|docx|ppt|pptx|xls|xlsx|txt");
    }

    /**
     * 是否为视频文件
     */
    public boolean isVideo() {
        if (contentType == null) return false;
        return contentType.startsWith("video/");
    }

    /**
     * 获取友好的文件大小
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "未知";
        
        long size = fileSize;
        String[] units = {"B", "KB", "MB", "GB"};
        int unit = 0;
        
        while (size >= 1024 && unit < units.length - 1) {
            size /= 1024;
            unit++;
        }
        
        return size + " " + units[unit];
    }

    /**
     * 记录下载
     */
    public void recordDownload() {
        this.downloadCount = (this.downloadCount == null ? 0 : this.downloadCount) + 1;
        this.lastDownloadAt = LocalDateTime.now();
        updateTimestamp();
    }

    /**
     * 更新时间戳
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
} 