package com.noncore.assessment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HelpTicketStatusUpdateRequest {

    @Schema(description = "工单状态", example = "in_progress")
    @NotBlank
    private String status;
}
