package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.PostComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 帖子评论Mapper接口
 *
 * @author Assessment System
 */
@Mapper
public interface PostCommentMapper {

    /**
     * 插入评论
     *
     * @param comment 评论信息
     * @return 插入的行数
     */
    int insert(PostComment comment);

    /**
     * 根据ID查询评论
     *
     * @param id 评论ID
     * @return 评论信息
     */
    PostComment selectById(Long id);

    /**
     * 根据ID查询评论详情（包含作者信息）
     *
     * @param id 评论ID
     * @return 评论详情
     */
    PostComment selectDetailById(Long id);

    /**
     * 更新评论
     *
     * @param comment 评论信息
     * @return 更新的行数
     */
    int updateById(PostComment comment);

    /**
     * 删除评论
     *
     * @param id 评论ID
     * @return 删除的行数
     */
    int deleteById(Long id);

    /**
     * 软删除评论
     *
     * @param id 评论ID
     * @return 更新的行数
     */
    int softDeleteById(Long id);

    /**
     * 查询评论列表
     *
     * @param params 查询参数
     * @return 评论列表
     */
    List<PostComment> selectList(Map<String, Object> params);

    /**
     * 查询评论详情列表（包含作者信息）
     *
     * @param params 查询参数
     * @return 评论详情列表
     */
    List<PostComment> selectDetailList(Map<String, Object> params);

    /**
     * 查询评论总数
     *
     * @param params 查询参数
     * @return 评论总数
     */
    long countList(Map<String, Object> params);

    /**
     * 根据帖子ID查询评论列表
     *
     * @param postId 帖子ID
     * @param params 查询参数
     * @return 评论列表
     */
    List<PostComment> selectByPostId(@Param("postId") Long postId, @Param("params") Map<String, Object> params);

    /**
     * 根据帖子ID查询评论总数
     *
     * @param postId 帖子ID
     * @param params 查询参数
     * @return 评论总数
     */
    long countByPostId(@Param("postId") Long postId, @Param("params") Map<String, Object> params);

    /**
     * 根据用户ID查询评论列表
     *
     * @param userId 用户ID
     * @param params 查询参数
     * @return 评论列表
     */
    List<PostComment> selectByUserId(@Param("userId") Long userId, @Param("params") Map<String, Object> params);

    /**
     * 根据用户ID查询评论总数
     *
     * @param userId 用户ID
     * @param params 查询参数
     * @return 评论总数
     */
    long countByUserId(@Param("userId") Long userId, @Param("params") Map<String, Object> params);

    /**
     * 查询帖子的最新评论
     *
     * @param postId 帖子ID
     * @param limit 限制数量
     * @return 最新评论列表
     */
    List<PostComment> selectRecentByPostId(@Param("postId") Long postId, @Param("limit") int limit);

    /**
     * 查询帖子的最后一条评论
     *
     * @param postId 帖子ID
     * @return 最后一条评论
     */
    PostComment selectLastByPostId(Long postId);

    /**
     * 增加评论点赞数
     *
     * @param id 评论ID
     * @return 更新的行数
     */
    int incrementLikes(Long id);

    /**
     * 减少评论点赞数
     *
     * @param id 评论ID
     * @return 更新的行数
     */
    int decrementLikes(Long id);

    /**
     * 查询子评论列表
     *
     * @param parentId 父评论ID
     * @param params 查询参数
     * @return 子评论列表
     */
    List<PostComment> selectReplies(@Param("parentId") Long parentId, @Param("params") Map<String, Object> params);

    /**
     * 查询子评论总数
     *
     * @param parentId 父评论ID
     * @return 子评论总数
     */
    long countReplies(Long parentId);
} 