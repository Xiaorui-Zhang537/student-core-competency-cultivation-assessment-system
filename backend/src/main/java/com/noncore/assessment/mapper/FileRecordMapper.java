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
     * 根据上传者与关联类型查询文件列表
     */
    List<FileRecord> selectByUploaderIdAndRelatedType(@Param("uploaderId") Long uploaderId,
                                                      @Param("relatedType") String relatedType);

    /**
     * 根据用途与关联ID删除文件记录
     */
    int deleteByPurposeAndRelatedId(@Param("purpose") String purpose, @Param("relatedId") Long relatedId);

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

    /**
     * 根据ID集合查询文件记录
     */
    List<FileRecord> selectByIds(@Param("ids") java.util.List<Long> ids);

    /**
     * 批量重绑定文件到新的用途与关联ID
     */
    int updateRelatedByIds(@Param("ids") java.util.List<Long> ids,
                           @Param("newPurpose") String newPurpose,
                           @Param("newRelatedId") Long newRelatedId);

    /**
     * 通过关联表 lesson_materials 查询某节次的资料文件
     */
    List<FileRecord> selectByLessonId(@Param("lessonId") Long lessonId);

    /**
     * 清空某节次的资料关联
     */
    int deleteLessonMaterialsByLessonId(@Param("lessonId") Long lessonId);

    /**
     * 为某节次批量建立资料关联
     */
    int insertLessonMaterials(@Param("lessonId") Long lessonId, @Param("fileIds") java.util.List<Long> fileIds);

    /**
     * 按文件ID清理在关联表中的引用
     */
    int deleteLessonMaterialsByFileId(@Param("fileId") Long fileId);
} 