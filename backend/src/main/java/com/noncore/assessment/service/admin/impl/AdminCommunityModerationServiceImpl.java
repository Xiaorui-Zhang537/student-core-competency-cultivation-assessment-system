package com.noncore.assessment.service.admin.impl;

import com.noncore.assessment.entity.Post;
import com.noncore.assessment.entity.PostComment;
import com.noncore.assessment.exception.BusinessException;
import com.noncore.assessment.exception.ErrorCode;
import com.noncore.assessment.mapper.AdminCommunityMapper;
import com.noncore.assessment.mapper.PostCommentMapper;
import com.noncore.assessment.mapper.PostMapper;
import com.noncore.assessment.service.admin.AdminCommunityModerationService;
import com.noncore.assessment.util.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员-社区治理服务实现。
 *
 * <p>说明：该服务为 ADMIN 提供“越权治理能力”，不进行作者归属校验。</p>
 *
 * @author System
 * @since 2026-02-14
 */
@Service
@Transactional
public class AdminCommunityModerationServiceImpl implements AdminCommunityModerationService {

    private final AdminCommunityMapper adminCommunityMapper;
    private final PostMapper postMapper;
    private final PostCommentMapper postCommentMapper;

    public AdminCommunityModerationServiceImpl(AdminCommunityMapper adminCommunityMapper,
                                               PostMapper postMapper,
                                               PostCommentMapper postCommentMapper) {
        this.adminCommunityMapper = adminCommunityMapper;
        this.postMapper = postMapper;
        this.postCommentMapper = postCommentMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Post> pagePosts(int page, int size, String keyword, String category, String status, Boolean pinned, boolean includeDeleted) {
        int p = Math.max(1, page);
        int s = Math.min(Math.max(1, size), 100);
        int offset = (p - 1) * s;
        List<Post> items = adminCommunityMapper.pagePosts(keyword, category, status, pinned, includeDeleted, offset, s);
        long total = safe(adminCommunityMapper.countPosts(keyword, category, status, pinned, includeDeleted));
        int totalPages = (int) Math.ceil(total / (double) s);
        return PageResult.of(items, p, s, total, totalPages);
    }

    @Override
    public void moderatePost(Long postId, String status, Boolean pinned, Boolean allowComments, Boolean deleted) {
        if (Boolean.TRUE.equals(deleted)) {
            postMapper.softDeleteById(postId);
            return;
        }
        Post p = new Post();
        p.setId(postId);
        p.setStatus(status);
        p.setPinned(pinned);
        p.setAllowComments(allowComments);
        int updated = postMapper.updateById(p);
        if (updated <= 0) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "帖子不存在或更新失败");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<PostComment> pageComments(int page, int size, Long postId, String keyword, String status, boolean includeDeleted) {
        int p = Math.max(1, page);
        int s = Math.min(Math.max(1, size), 100);
        int offset = (p - 1) * s;
        List<PostComment> items = adminCommunityMapper.pageComments(postId, keyword, status, includeDeleted, offset, s);
        long total = safe(adminCommunityMapper.countComments(postId, keyword, status, includeDeleted));
        int totalPages = (int) Math.ceil(total / (double) s);
        return PageResult.of(items, p, s, total, totalPages);
    }

    @Override
    public void moderateComment(Long commentId, String status, Boolean deleted) {
        if (Boolean.TRUE.equals(deleted)) {
            postCommentMapper.softDeleteById(commentId);
            return;
        }
        PostComment c = new PostComment();
        c.setId(commentId);
        c.setStatus(status);
        int updated = postCommentMapper.updateById(c);
        if (updated <= 0) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "评论不存在或更新失败");
        }
    }

    private long safe(Long v) {
        return v == null ? 0L : v;
    }
}

