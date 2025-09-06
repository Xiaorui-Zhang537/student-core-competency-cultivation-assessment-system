package com.noncore.assessment.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "帮助中心工单")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelpTicket {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String status; // open,in_progress,resolved,closed
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


