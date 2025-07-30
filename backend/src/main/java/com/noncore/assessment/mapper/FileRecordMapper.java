package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.FileRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件记录数据访问层Mapper接口
 * 定义文件记录相关的数据库操作方法
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Mapper
public interface FileRecordMapper {

    /**
     * 插入文件记录
     *
     * @param fileRecord 文件记录对象
     * @return 影响行数
     */
    int insertFileRecord(FileRecord fileRecord);

    /**
     * 根据ID查询文件记录
     *
     * @param id 文件记录ID
     * @return 文件记录
     */
    FileRecord selectFileRecordById(@Param("id") Long id);

    /**
     * 根据上传者ID查询文件列表
     *
     * @param uploaderId 上传者ID
     * @return 文件记录列表
     */
    List<FileRecord> selectByUploaderId(@Param("uploaderId") Long uploaderId);

    /**
     * 根据用途和关联ID查询文件列表
     *
     * @param purpose 上传目的
     * @param relatedId 关联ID
     * @return 文件记录列表
     */
    List<FileRecord> selectByPurposeAndRelatedId(@Param("purpose") String purpose, @Param("relatedId") Long relatedId);

    /**
     * 更新文件记录
     *
     * @param fileRecord 文件记录对象
     * @return 影响行数
     */
    int updateFileRecord(FileRecord fileRecord);

    /**
     * 删除文件记录（软删除）
     *
     * @param id 文件记录ID
     * @return 影响行数
     */
    int deleteFileRecord(@Param("id") Long id);

    /**
     * 增加下载次数
     *
     * @param id 文件记录ID
     * @return 影响行数
     */
    int incrementDownloadCount(@Param("id") Long id);

    /**
     * 获取用户存储使用量
     *
     * @param userId 用户ID
     * @return 使用量（字节）
     */
    Long getUserStorageUsage(@Param("userId") Long userId);

    /**
     * 根据文件类型统计数量
     *
     * @param fileType 文件类型
     * @return 文件数量
     */
    Integer countByFileType(@Param("fileType") String fileType);
} 