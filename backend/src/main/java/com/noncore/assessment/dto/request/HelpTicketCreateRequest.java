package com.noncore.assessment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HelpTicketCreateRequest {

    @Schema(description = "工单标题", example = "播放异常")
    @NotBlank
    private String title;

    @Schema(description = "工单描述", example = "视频无法播放，报错码 xxx")
    @NotBlank
    private String description;
}


