package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.PostTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子与标签关联Mapper接口
 *
 * @author Assessment System
 */
@Mapper
public interface PostTagMapper {

    /**
     * 批量插入帖子与标签的关联
     *
     * @param postTags 帖子ID
     * @return 影响行数
     */
    int batchInsert(@Param("postTags") List<PostTag> postTags);

    /**
     * 根据帖子ID删除所有关联
     *
     * @param postId 帖子ID
     * @return 影响行数
     */
    int deleteByPostId(@Param("postId") Long postId);

    /**
     * 根据帖子ID查询关联的标签ID
     *
     * @param postId 帖子ID
     * @return 标签ID列表
     */
    List<Long> selectTagIdsByPostId(@Param("postId") Long postId);
} 