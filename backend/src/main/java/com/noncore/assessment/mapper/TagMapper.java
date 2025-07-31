package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 标签Mapper接口
 *
 * @author Assessment System
 */
@Mapper
public interface TagMapper {

    /**
     * 插入标签
     *
     * @param tag 标签信息
     * @return 插入的行数
     */
    int insert(Tag tag);

    /**
     * 根据ID查询标签
     *
     * @param id 标签ID
     * @return 标签信息
     */
    Tag selectById(Long id);

    /**
     * 根据名称查询标签
     *
     * @param name 标签名称
     * @return 标签信息
     */
    Tag selectByName(String name);

    /**
     * 更新标签
     *
     * @param tag 标签信息
     * @return 更新的行数
     */
    int updateById(Tag tag);

    /**
     * 批量插入标签
     * @param tags
     * @return
     */
    int batchInsert(@Param("tags") List<Tag> tags);

    /**
     * 删除标签
     *
     * @param id 标签ID
     * @return 删除的行数
     */
    int deleteById(Long id);

    /**
     * 查询标签列表
     *
     * @param params 查询参数
     * @return 标签列表
     */
    List<Tag> selectList(Map<String, Object> params);

    /**
     * 查询标签总数
     *
     * @param params 查询参数
     * @return 标签总数
     */
    long countList(Map<String, Object> params);

    /**
     * 增加标签帖子数
     *
     * @param id 标签ID
     * @return 更新的行数
     */
    int incrementPostsCount(Long id);

    /**
     * 减少标签帖子数
     *
     * @param id 标签ID
     * @return 更新的行数
     */
    int decrementPostsCount(Long id);

    /**
     * 查询热门标签
     *
     * @param limit 限制数量
     * @return 热门标签列表
     */
    List<Tag> selectHotTags(int limit);

    /**
     * 根据帖子ID查询标签列表
     *
     * @param postId 帖子ID
     * @return 标签列表
     */
    List<Tag> selectByPostId(Long postId);

    /**
     * 批量查询标签
     *
     * @param names 标签名称列表
     * @return 标签列表
     */
    List<Tag> selectByNames(@Param("names") List<String> names);

    /**
     * 搜索标签
     *
     * @param keyword 关键词
     * @param limit 限制数量
     * @return 标签列表
     */
    List<Tag> searchTags(@Param("keyword") String keyword, @Param("limit") int limit);
} 