package com.noncore.assessment.service.admin;

import com.noncore.assessment.entity.Post;
import com.noncore.assessment.entity.PostComment;
import com.noncore.assessment.util.PageResult;

/**
 * 管理员-社区治理服务。
 *
 * @author System
 * @since 2026-02-14
 */
public interface AdminCommunityModerationService {

    PageResult<Post> pagePosts(int page, int size, String keyword, String category, String status, Boolean pinned, boolean includeDeleted);

    void moderatePost(Long postId, String status, Boolean pinned, Boolean allowComments, Boolean deleted);

    PageResult<PostComment> pageComments(int page, int size, Long postId, String keyword, String status, boolean includeDeleted);

    void moderateComment(Long commentId, String status, Boolean deleted);
}

