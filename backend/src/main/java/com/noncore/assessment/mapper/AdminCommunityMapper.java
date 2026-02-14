package com.noncore.assessment.mapper;

import com.noncore.assessment.entity.Post;
import com.noncore.assessment.entity.PostComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 管理员-社区内容治理查询 Mapper（支持包含已删除内容）。
 *
 * @author System
 * @since 2026-02-14
 */
@Mapper
public interface AdminCommunityMapper {

    List<Post> pagePosts(@Param("keyword") String keyword,
                         @Param("category") String category,
                         @Param("status") String status,
                         @Param("pinned") Boolean pinned,
                         @Param("includeDeleted") boolean includeDeleted,
                         @Param("offset") int offset,
                         @Param("size") int size);

    Long countPosts(@Param("keyword") String keyword,
                    @Param("category") String category,
                    @Param("status") String status,
                    @Param("pinned") Boolean pinned,
                    @Param("includeDeleted") boolean includeDeleted);

    List<PostComment> pageComments(@Param("postId") Long postId,
                                   @Param("keyword") String keyword,
                                   @Param("status") String status,
                                   @Param("includeDeleted") boolean includeDeleted,
                                   @Param("offset") int offset,
                                   @Param("size") int size);

    Long countComments(@Param("postId") Long postId,
                       @Param("keyword") String keyword,
                       @Param("status") String status,
                       @Param("includeDeleted") boolean includeDeleted);
}

