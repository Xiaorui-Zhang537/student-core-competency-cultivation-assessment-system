package com.noncore.assessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

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
    private String status;

    @Schema(description = "下载次数", example = "10")
    private Integer downloadCount;

    @Schema(description = "最后下载时间", example = "2024-12-28 15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastDownloadAt;

    @Schema(description = "创建时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2024-12-28 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "是否删除", example = "false")
    private Boolean deleted;

    /**
     * 默认构造方法
     */
    public FileRecord() {
        this.status = "active";
        this.downloadCount = 0;
        this.deleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 构造方法
     */
    public FileRecord(String originalName, String storedName, String filePath, Long fileSize, String contentType, Long uploaderId) {
        this();
        this.originalName = originalName;
        this.storedName = storedName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.uploaderId = uploaderId;
        
        // 提取文件扩展名
        if (originalName != null && originalName.contains(".")) {
            this.extension = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        }
    }

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

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getStoredName() {
        return storedName;
    }

    public void setStoredName(String storedName) {
        this.storedName = storedName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getUploadPurpose() {
        return uploadPurpose;
    }

    public void setUploadPurpose(String uploadPurpose) {
        this.uploadPurpose = uploadPurpose;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }

    public Long getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(Long uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getRelatedType() {
        return relatedType;
    }

    public void setRelatedType(String relatedType) {
        this.relatedType = relatedType;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public LocalDateTime getLastDownloadAt() {
        return lastDownloadAt;
    }

    public void setLastDownloadAt(LocalDateTime lastDownloadAt) {
        this.lastDownloadAt = lastDownloadAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "FileRecord{" +
                "id=" + id +
                ", originalName='" + originalName + '\'' +
                ", storedName='" + storedName + '\'' +
                ", fileSize=" + fileSize +
                ", contentType='" + contentType + '\'' +
                ", uploaderId=" + uploaderId +
                ", relatedType='" + relatedType + '\'' +
                ", relatedId=" + relatedId +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
} 