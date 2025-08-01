package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.FileRecord;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.FileRecordMapper;
import com.noncore.assessment.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 文件存储服务实现类
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Service
@Transactional
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    private final FileRecordMapper fileRecordMapper;

    public FileStorageServiceImpl(FileRecordMapper fileRecordMapper) {
        this.fileRecordMapper = fileRecordMapper;
    }

    @Value("${file.upload.path:/tmp/uploads}")
    private String uploadPath;

    @Value("${file.upload.maxSize:10485760}") // 10MB
    private long maxFileSize;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "zip", "rar"
    );

    @Override
    public FileRecord uploadFile(MultipartFile file, Long uploaderId, String purpose, Long relatedId) {
        try {
            logger.info("上传文件，用户ID: {}, 文件名: {}, 用途: {}", uploaderId, file.getOriginalFilename(), purpose);

            // 验证文件
            validateFile(file);

            // 创建存储目录
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String purposePath = purpose != null ? purpose : "general";
            String fullPath = uploadPath + "/" + purposePath + "/" + datePath;
            Files.createDirectories(Paths.get(fullPath));

            // 生成唯一文件名
            String originalName = file.getOriginalFilename();
            String extension = getFileExtension(originalName);
            String storedName = generateUniqueFileName(extension);
            String filePath = fullPath + "/" + storedName;

            // 保存文件
            file.transferTo(new File(filePath));

            // 创建文件记录
            FileRecord fileRecord = new FileRecord();
            fileRecord.setOriginalName(originalName);
            fileRecord.setStoredName(storedName);
            fileRecord.setFilePath(filePath);
            fileRecord.setFileSize(file.getSize());
            fileRecord.setFileType(extension);
            fileRecord.setMimeType(file.getContentType());
            fileRecord.setUploaderId(uploaderId);
            fileRecord.setUploadPurpose(purpose);
            fileRecord.setRelatedId(relatedId);
            fileRecord.setDownloadCount(0);
            fileRecord.setStatus("active");

            // 保存文件记录到数据库
            if (fileRecordMapper.insertFileRecord(fileRecord) <= 0) {
                throw new BusinessException(ErrorCode.OPERATION_FAILED, "保存文件记录失败");
            }

            logger.info("文件上传成功，文件ID: {}", fileRecord.getId());
            return fileRecord;

        } catch (IOException e) {
            logger.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] downloadFile(Long fileId, Long userId) {
        try {
            logger.info("下载文件，文件ID: {}, 用户ID: {}", fileId, userId);

            // 检查权限
            if (!hasFilePermission(fileId, userId)) {
                throw new BusinessException(ErrorCode.FILE_PERMISSION_DENIED, "没有权限下载该文件");
            }

            // 获取文件记录
            FileRecord fileRecord = fileRecordMapper.selectFileRecordById(fileId);
            if (fileRecord == null) {
                throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "文件不存在");
            }

            // 读取文件
            Path filePath = Paths.get(fileRecord.getFilePath());
            if (!Files.exists(filePath)) {
                throw new BusinessException(ErrorCode.FILE_LOST, "文件已丢失");
            }

            // 增加下载次数
            fileRecordMapper.incrementDownloadCount(fileId);

            byte[] fileBytes = Files.readAllBytes(filePath);
            logger.info("文件下载成功，文件ID: {}, 大小: {} bytes", fileId, fileBytes.length);
            return fileBytes;

        } catch (IOException e) {
            logger.error("文件下载失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(Long fileId, Long userId) {
        logger.info("删除文件，文件ID: {}, 用户ID: {}", fileId, userId);

        // 检查权限
        if (!hasFilePermission(fileId, userId)) {
            throw new BusinessException(ErrorCode.FILE_PERMISSION_DENIED, "没有权限删除该文件");
        }

        // 获取文件记录
        FileRecord fileRecord = fileRecordMapper.selectFileRecordById(fileId);
        if (fileRecord == null) {
            return true; // 文件不存在，视为删除成功
        }

        // 删除物理文件
        try {
            Path filePath = Paths.get(fileRecord.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            logger.error("删除物理文件失败, path: {}", fileRecord.getFilePath(), e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "物理文件删除失败");
        }


        // 软删除数据库记录
        int result = fileRecordMapper.deleteFileRecord(fileId);
        
        logger.info("文件删除成功，文件ID: {}", fileId);
        return result > 0;
    }

    @Override
    public FileRecord getFileInfo(Long fileId) {
        logger.info("获取文件信息，文件ID: {}", fileId);
        return fileRecordMapper.selectFileRecordById(fileId);
    }

    @Override
    public List<FileRecord> getUserFiles(Long uploaderId, String purpose) {
        logger.info("获取用户文件列表，用户ID: {}, 用途: {}", uploaderId, purpose);
        
        if (purpose != null) {
            return fileRecordMapper.selectByPurposeAndRelatedId(purpose, uploaderId);
        } else {
            return fileRecordMapper.selectByUploaderId(uploaderId);
        }
    }

    @Override
    public List<FileRecord> getRelatedFiles(String purpose, Long relatedId) {
        logger.info("获取关联文件列表，用途: {}, 关联ID: {}", purpose, relatedId);
        return fileRecordMapper.selectByPurposeAndRelatedId(purpose, relatedId);
    }

    @Override
    public boolean hasFilePermission(Long fileId, Long userId) {
        FileRecord fileRecord = fileRecordMapper.selectFileRecordById(fileId);
        if (fileRecord == null) {
            return false;
        }

        // 文件上传者有权限
        if (userId.equals(fileRecord.getUploaderId())) {
            return true;
        }

        // 根据文件用途检查权限
        String purpose = fileRecord.getUploadPurpose();
        return switch (purpose) {
            case "avatar", "assignment" ->
                // 只有上传者可以访问
                    false;
            case "course", "lesson" ->
                // 课程相关文件，需要检查课程权限（简化实现，返回true）
                    true;
            default -> false;
        };
    }

    @Override
    public Long getUserStorageUsage(Long userId) {
        logger.info("获取用户存储使用量，用户ID: {}", userId);
        return fileRecordMapper.getUserStorageUsage(userId);
    }

    @Override
    public Map<String, Object> batchDeleteFiles(List<Long> fileIds, Long userId) {
        logger.info("批量删除文件，文件数量: {}, 用户ID: {}", fileIds.size(), userId);

        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (Long fileId : fileIds) {
            try {
                if (deleteFile(fileId, userId)) {
                    successCount++;
                } else {
                    failCount++;
                    errors.add("删除文件失败，文件ID: " + fileId);
                }
            } catch (Exception e) {
                failCount++;
                errors.add("删除文件失败，文件ID: " + fileId + ", 错误: " + e.getMessage());
                logger.error("批量删除文件失败", e);
            }
        }

        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);

        return result;
    }

    @Override
    public Map<String, Object> cleanupExpiredFiles(int days) {
        logger.info("清理过期文件，过期天数: {}", days);

        Map<String, Object> result = new HashMap<>();
        // 简化实现，返回模拟数据
        result.put("cleanedFiles", 0);
        result.put("freedSpace", 0L);
        
        return result;
    }

    // 私有辅助方法

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY, "文件不能为空");
        }

        if (file.getSize() > maxFileSize) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEED, "文件大小超过限制: " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new BusinessException(ErrorCode.FILE_TYPE_UNSUPPORTED, "不支持的文件类型: " + extension);
            }
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private String generateUniqueFileName(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }
} 