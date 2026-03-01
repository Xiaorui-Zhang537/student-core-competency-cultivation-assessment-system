package com.noncore.assessment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HelpTicketReplyRequest {

    @Schema(description = "回复内容", example = "请尝试清理浏览器缓存后重试。")
    @NotBlank
    private String content;
}
