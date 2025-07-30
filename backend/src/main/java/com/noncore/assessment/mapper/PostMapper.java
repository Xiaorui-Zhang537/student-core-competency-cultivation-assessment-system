package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 帖子Mapper接口
 *
 * @author Assessment System
 */
@Mapper
public interface PostMapper {

    /**
     * 插入帖子
     *
     * @param post 帖子信息
     * @return 插入的行数
     */
    int insert(Post post);

    /**
     * 根据ID查询帖子
     *
     * @param id 帖子ID
     * @return 帖子信息
     */
    Post selectById(Long id);

    /**
     * 根据ID查询帖子详情（包含作者信息）
     *
     * @param id 帖子ID
     * @return 帖子详情
     */
    Post selectDetailById(Long id);

    /**
     * 更新帖子
     *
     * @param post 帖子信息
     * @return 更新的行数
     */
    int updateById(Post post);

    /**
     * 删除帖子
     *
     * @param id 帖子ID
     * @return 删除的行数
     */
    int deleteById(Long id);

    /**
     * 软删除帖子
     *
     * @param id 帖子ID
     * @return 更新的行数
     */
    int softDeleteById(Long id);

    /**
     * 查询帖子列表
     *
     * @param params 查询参数
     * @return 帖子列表
     */
    List<Post> selectList(Map<String, Object> params);

    /**
     * 查询帖子详情列表（包含作者信息）
     *
     * @param params 查询参数
     * @return 帖子详情列表
     */
    List<Post> selectDetailList(Map<String, Object> params);

    /**
     * 查询帖子总数
     *
     * @param params 查询参数
     * @return 帖子总数
     */
    long countList(Map<String, Object> params);

    /**
     * 增加帖子浏览量
     *
     * @param id 帖子ID
     * @return 更新的行数
     */
    int incrementViews(Long id);

    /**
     * 增加帖子点赞数
     *
     * @param id 帖子ID
     * @return 更新的行数
     */
    int incrementLikes(Long id);

    /**
     * 减少帖子点赞数
     *
     * @param id 帖子ID
     * @return 更新的行数
     */
    int decrementLikes(Long id);

    /**
     * 增加帖子评论数
     *
     * @param id 帖子ID
     * @return 更新的行数
     */
    int incrementComments(Long id);

    /**
     * 减少帖子评论数
     *
     * @param id 帖子ID
     * @return 更新的行数
     */
    int decrementComments(Long id);

    /**
     * 查询用户的帖子列表
     *
     * @param userId 用户ID
     * @param params 查询参数
     * @return 帖子列表
     */
    List<Post> selectByUserId(@Param("userId") Long userId, @Param("params") Map<String, Object> params);

    /**
     * 查询用户的帖子总数
     *
     * @param userId 用户ID
     * @param params 查询参数
     * @return 帖子总数
     */
    long countByUserId(@Param("userId") Long userId, @Param("params") Map<String, Object> params);

    /**
     * 查询热门帖子
     *
     * @param limit 限制数量
     * @return 热门帖子列表
     */
    List<Post> selectHotPosts(int limit);

    /**
     * 查询推荐帖子
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 推荐帖子列表
     */
    List<Post> selectRecommendPosts(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 根据标签查询帖子
     *
     * @param tagId 标签ID
     * @param params 查询参数
     * @return 帖子列表
     */
    List<Post> selectByTagId(@Param("tagId") Long tagId, @Param("params") Map<String, Object> params);

    /**
     * 根据分类查询帖子统计
     *
     * @return 分类统计列表
     */
    List<Map<String, Object>> selectCategoryStats();

    /**
     * 查询社区统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> selectCommunityStats();

    /**
     * 查询活跃用户
     *
     * @param limit 限制数量
     * @return 活跃用户列表
     */
    List<Map<String, Object>> selectActiveUsers(int limit);
} 