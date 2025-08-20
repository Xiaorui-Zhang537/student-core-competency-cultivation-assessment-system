package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子点赞Mapper接口
 *
 * @author Assessment System
 */
@Mapper
public interface PostLikeMapper {

    /**
     * 插入点赞记录
     *
     * @param postLike 点赞信息
     * @return 插入的行数
     */
    int insert(PostLike postLike);

    /**
     * 删除点赞记录
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 删除的行数
     */
    int delete(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 查询用户是否已点赞
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 点赞记录
     */
    PostLike selectByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 查询帖子的点赞用户列表
     *
     * @param postId 帖子ID
     * @return 点赞用户ID列表
     */
    List<Long> selectUserIdsByPostId(Long postId);

    /**
     * 查询用户点赞的帖子列表
     *
     * @param userId 用户ID
     * @return 帖子ID列表
     */
    List<Long> selectPostIdsByUserId(Long userId);

    /**
     * 统计帖子的点赞数
     *
     * @param postId 帖子ID
     * @return 点赞数
     */
    long countByPostId(Long postId);

    /**
     * 统计用户的点赞数
     *
     * @param userId 用户ID
     * @return 点赞数
     */
    long countByUserId(Long userId);

    /**
     * 批量查询用户对帖子的点赞状态
     *
     * @param postIds 帖子ID列表
     * @param userId 用户ID
     * @return 点赞的帖子ID列表
     */
    List<Long> selectLikedPostIds(@Param("postIds") List<Long> postIds, @Param("userId") Long userId);

    /**
     * 删除某帖子的所有点赞
     */
    int deleteByPostId(@Param("postId") Long postId);
} 