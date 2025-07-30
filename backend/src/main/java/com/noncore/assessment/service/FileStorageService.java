package com.noncore.assessment.service;

import com.noncore.assessment.entity.FileRecord;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文件存储服务接口
 * 处理文件上传、下载、删除等操作
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
public interface FileStorageService {

    /**
     * 上传文件
     *
     * @param file 上传的文件
     * @param uploaderId 上传者ID
     * @param purpose 上传目的（avatar, assignment, course, etc.）
     * @param relatedId 关联ID
     * @return 文件记录
     */
    FileRecord uploadFile(MultipartFile file, Long uploaderId, String purpose, Long relatedId);

    /**
     * 下载文件
     *
     * @param fileId 文件ID
     * @param userId 用户ID（用于权限检查）
     * @return 文件字节数组
     */
    byte[] downloadFile(Long fileId, Long userId);

    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @param userId 用户ID（用于权限检查）
     * @return 删除结果
     */
    boolean deleteFile(Long fileId, Long userId);

    /**
     * 获取文件信息
     *
     * @param fileId 文件ID
     * @return 文件记录
     */
    FileRecord getFileInfo(Long fileId);

    /**
     * 获取用户上传的文件列表
     *
     * @param uploaderId 上传者ID
     * @param purpose 文件用途（可选）
     * @return 文件列表
     */
    List<FileRecord> getUserFiles(Long uploaderId, String purpose);

    /**
     * 获取关联对象的文件列表
     *
     * @param purpose 文件用途
     * @param relatedId 关联ID
     * @return 文件列表
     */
    List<FileRecord> getRelatedFiles(String purpose, Long relatedId);

    /**
     * 检查文件权限
     *
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasFilePermission(Long fileId, Long userId);

    /**
     * 获取用户存储使用量
     *
     * @param userId 用户ID
     * @return 使用量（字节）
     */
    Long getUserStorageUsage(Long userId);

    /**
     * 批量删除文件
     *
     * @param fileIds 文件ID列表
     * @param userId 用户ID
     * @return 删除结果统计
     */
    Map<String, Object> batchDeleteFiles(List<Long> fileIds, Long userId);

    /**
     * 清理过期文件
     *
     * @param days 过期天数
     * @return 清理统计
     */
    Map<String, Object> cleanupExpiredFiles(int days);
} 