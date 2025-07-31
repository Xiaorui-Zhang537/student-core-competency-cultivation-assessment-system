package com.noncore.assessment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 评论点赞Mapper接口
 *
 * @author Assessment System
 */
@Mapper
public interface CommentLikeMapper {

    /**
     * 插入评论点赞记录
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     * @return 影响行数
     */
    int insert(@Param("commentId") Long commentId, @Param("userId") Long userId);

    /**
     * 删除评论点赞记录
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     * @return 影响行数
     */
    int delete(@Param("commentId") Long commentId, @Param("userId") Long userId);

    /**
     * 根据评论ID和用户ID查询点赞记录
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     * @return 存在数量
     */
    Integer selectByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

    /**
     * 统计评论的点赞数
     *
     * @param commentId 评论ID
     * @return 点赞总数
     */
    int countByCommentId(@Param("commentId") Long commentId);
} 