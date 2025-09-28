package com.noncore.assessment.service.impl;

import com.noncore.assessment.entity.FileRecord;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.FileRecordMapper;
import com.noncore.assessment.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
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

    private final com.noncore.assessment.mapper.ChatAttachmentMapper chatAttachmentMapper;

    public FileStorageServiceImpl(FileRecordMapper fileRecordMapper,
                                  com.noncore.assessment.mapper.ChatAttachmentMapper chatAttachmentMapper) {
        this.fileRecordMapper = fileRecordMapper;
        this.chatAttachmentMapper = chatAttachmentMapper;
    }

    // Unified configuration keys aligned with application.yml
    @Value("${file.upload-dir:/tmp/uploads}")
    private String uploadPath;

    // Support human-readable size such as 50MB, 10MB, or plain number in bytes
    @Value("${file.max-size:10485760}")
    private String maxFileSizeStr;

    @Value("${file.allowed-extensions:jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx,ppt,pptx,txt,zip,rar}")
    private String allowedExtensionsCsv;

    // Purpose-specific max sizes, e.g. "course_material=1GB,course_video=1GB"
    @Value("${file.purpose-max-sizes:}")
    private String purposeMaxSizesCsv;

    private long maxFileSize;
    private Set<String> allowedExtensions;
    private Path baseUploadDir;
    private Map<String, Long> purposeMaxSizeBytes = new HashMap<>();

    @PostConstruct
    public void initUploadConfig() {
        this.maxFileSize = parseSizeToBytes(maxFileSizeStr);
        this.allowedExtensions = parseExtensions(allowedExtensionsCsv);
        this.purposeMaxSizeBytes = parsePurposeMaxSizes(purposeMaxSizesCsv);
        // Resolve upload base directory to absolute path to avoid servlet temp dir resolution
        Path configured = Paths.get(uploadPath);
        if (configured.isAbsolute()) {
            this.baseUploadDir = configured.normalize();
        } else {
            this.baseUploadDir = Paths.get(System.getProperty("user.dir")).resolve(configured).normalize();
        }
        try {
            Files.createDirectories(this.baseUploadDir);
            logger.info("文件上传根目录: {}", this.baseUploadDir);
        } catch (IOException e) {
            logger.warn("创建上传根目录失败: {}", this.baseUploadDir, e);
        }
    }

    @Override
    public FileRecord uploadFile(MultipartFile file, Long uploaderId, String purpose, Long relatedId) {
        try {
            logger.info("上传文件，用户ID: {}, 文件名: {}, 用途: {}", uploaderId, file.getOriginalFilename(), purpose);

            // 验证文件（按用途应用特定大小限制）
            validateFile(file, purpose);

            // 创建存储目录（使用绝对路径）
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String purposePath = purpose != null ? purpose : "general";
            Path storageDir = baseUploadDir.resolve(purposePath).resolve(datePath).normalize();
            Files.createDirectories(storageDir);

            // 生成唯一文件名
            String originalName = file.getOriginalFilename();
            String extension = getFileExtension(originalName);
            String storedName = generateUniqueFileName(extension);
            Path targetPath = storageDir.resolve(storedName).normalize();

            // 保存文件（确保父目录存在，且使用绝对路径）
            File targetFile = targetPath.toFile();
            File parent = targetFile.getParentFile();
            if (parent != null && !parent.exists()) {
                // 双保险
                parent.mkdirs();
            }
            file.transferTo(targetFile);

            // 创建文件记录
            FileRecord fileRecord = new FileRecord();
            fileRecord.setOriginalName(originalName);
            fileRecord.setStoredName(storedName);
            fileRecord.setFilePath(targetPath.toString());
            fileRecord.setFileSize(file.getSize());
            fileRecord.setFileType(extension);
            fileRecord.setMimeType(file.getContentType());
            fileRecord.setUploaderId(uploaderId);
            fileRecord.setUploadPurpose(purpose);
            fileRecord.setRelatedType(purpose);
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

        // 先清理 lesson_materials 关联，避免删除记录时触发约束或脏引用
        try {
            fileRecordMapper.deleteLessonMaterialsByFileId(fileId);
        } catch (Exception e) {
            logger.warn("清理 lesson_materials 关联失败(fileId={})，继续删除文件记录", fileId, e);
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
            // Filter by uploader and related type when a purpose is provided
            return fileRecordMapper.selectByUploaderIdAndRelatedType(uploaderId, purpose);
        }
        return fileRecordMapper.selectByUploaderId(uploaderId);
    }

    @Override
    public List<FileRecord> getRelatedFiles(String purpose, Long relatedId) {
        logger.info("获取关联文件列表，用途: {}, 关联ID: {}", purpose, relatedId);
        return fileRecordMapper.selectByPurposeAndRelatedId(purpose, relatedId);
    }

    @Override
    public void cleanupRelatedFiles(String purpose, Long relatedId) {
        logger.info("清理关联文件，用途: {}, 关联ID: {}", purpose, relatedId);
        List<FileRecord> files = fileRecordMapper.selectByPurposeAndRelatedId(purpose, relatedId);
        if (files == null || files.isEmpty()) return;
        for (FileRecord f : files) {
            try {
                Path path = Paths.get(f.getFilePath());
                if (Files.exists(path)) {
                    Files.delete(path);
                }
            } catch (Exception e) {
                logger.warn("删除物理文件失败(id={}): {}", f.getId(), f.getFilePath(), e);
            }
            try {
                fileRecordMapper.deleteFileRecord(f.getId());
            } catch (Exception e) {
                logger.warn("删除文件记录失败(id={})", f.getId(), e);
            }
        }
    }

    @Override
    public void relinkFilesTo(List<Long> fileIds, String newPurpose, Long newRelatedId, Long expectedUploaderId) {
        if (fileIds == null || fileIds.isEmpty()) return;
        List<FileRecord> records = fileRecordMapper.selectByIds(fileIds);
        if (records.size() != fileIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "部分文件不存在");
        }
        for (FileRecord r : records) {
            if (!Objects.equals(r.getUploaderId(), expectedUploaderId)) {
                throw new BusinessException(ErrorCode.FILE_PERMISSION_DENIED, "不能操作他人文件");
            }
        }
        int updated = fileRecordMapper.updateRelatedByIds(fileIds, newPurpose, newRelatedId);
        if (updated <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "重绑定文件失败");
        }
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
        if (purpose == null) {
            // 兼容旧数据：回退使用 related_type
            purpose = fileRecord.getRelatedType();
        }
        // 聊天附件：发送者或接收者可访问；若关联记录尚未建立，放宽为 relatedId == 当前用户
        if ("chat".equalsIgnoreCase(String.valueOf(purpose))) {
            try {
                if (Objects.equals(fileRecord.getRelatedId(), userId)) {
                    return true;
                }
            } catch (Exception ignore) {}
            try {
                int ok = chatAttachmentMapper.existsUserAccessForFile(fileId, userId);
                if (ok > 0) return true;
            } catch (Exception ignore) {}
        }
        return switch (purpose) {
            case "avatar", "assignment" ->
                // 只有上传者可以访问
                    false;
            case "course", "lesson", "community_post",
                 "lesson_material", "course_material", "course_video",
                 "submission" ->
                // 课程/节次/社区附件与教学资料、课程视频、作业提交附件：允许教师端/系统处理访问
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

    private void validateFile(MultipartFile file, String purpose) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY, "文件不能为空");
        }

        long effectiveMax = maxFileSize;
        if (purpose != null && purposeMaxSizeBytes != null) {
            Long p = purposeMaxSizeBytes.get(purpose);
            if (p != null && p > 0) effectiveMax = p;
        }

        if (file.getSize() > effectiveMax) {
            long mb = effectiveMax / 1024 / 1024;
            String tip = mb >= 1024 ? String.format("%.1fGB", mb / 1024.0) : (mb + "MB");
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEED, "文件大小超过限制: " + tip);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (allowedExtensions != null && !allowedExtensions.contains(extension)) {
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

    private long parseSizeToBytes(String size) {
        if (size == null || size.isBlank()) return 10 * 1024 * 1024L;
        String s = size.trim().toUpperCase();
        try {
            if (s.endsWith("KB")) return Long.parseLong(s.replace("KB", "").trim()) * 1024L;
            if (s.endsWith("MB")) return Long.parseLong(s.replace("MB", "").trim()) * 1024L * 1024L;
            if (s.endsWith("GB")) return Long.parseLong(s.replace("GB", "").trim()) * 1024L * 1024L * 1024L;
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            logger.warn("无法解析文件大小配置: {}，使用默认10MB", size);
            return 10 * 1024 * 1024L;
        }
    }

    private Set<String> parseExtensions(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of("jpg","jpeg","png","gif","pdf","doc","docx","xls","xlsx","ppt","pptx","txt","zip","rar");
        }
        String[] arr = csv.split(",");
        Set<String> set = new HashSet<>();
        for (String a : arr) {
            String v = a.trim().toLowerCase();
            if (!v.isEmpty()) set.add(v);
        }
        return set.isEmpty() ? Set.of("jpg","jpeg","png","gif","pdf","doc","docx","xls","xlsx","ppt","pptx","txt","zip","rar") : set;
    }

    private Map<String, Long> parsePurposeMaxSizes(String csv) {
        Map<String, Long> map = new HashMap<>();
        if (csv == null || csv.isBlank()) return map;
        String[] pairs = csv.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                String key = kv[0].trim();
                String val = kv[1].trim();
                long bytes = parseSizeToBytes(val);
                if (!key.isEmpty() && bytes > 0) map.put(key, bytes);
            }
        }
        return map;
    }
} 