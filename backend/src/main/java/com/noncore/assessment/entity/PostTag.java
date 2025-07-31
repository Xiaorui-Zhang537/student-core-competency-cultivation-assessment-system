package com.noncore.assessment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 帖子与标签关联实体
 *
 * @author Assessment System
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostTag {
    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 标签ID
     */
    private Long tagId;
} 