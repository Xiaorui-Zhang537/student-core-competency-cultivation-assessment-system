package com.noncore.assessment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HelpTicketCreateRequest {

    @Schema(description = "工单标题", example = "播放异常")
    @NotBlank
    private String title;

    @Schema(description = "支持单正文", example = "视频无法播放，报错码 xxx")
    private String content;

    @Schema(description = "兼容旧版字段，等同于 content", example = "视频无法播放，报错码 xxx")
    private String description;

    @Schema(description = "渠道：support=支持工单，feedback=意见反馈", example = "support")
    private String channel;

    @Schema(description = "问题类型", example = "technical")
    private String ticketType;

    @Schema(description = "优先级", example = "medium")
    private String priority;

    @Schema(description = "联系方式", example = "student@example.com")
    private String contact;

    @Schema(description = "是否匿名（仅对普通展示匿名，管理员仍可见）", example = "false")
    private Boolean anonymous;

    public String getResolvedContent() {
        if (content != null && !content.isBlank()) return content;
        return description;
    }
}

