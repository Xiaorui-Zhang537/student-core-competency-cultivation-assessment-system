package com.noncore.assessment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAvatarRequest {
    @Schema(description = "头像文件ID")
    @NotNull
    private Long fileId;
}


