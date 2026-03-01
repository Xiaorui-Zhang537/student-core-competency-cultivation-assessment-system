package com.noncore.assessment.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "帮助中心工单消息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelpTicketMessage {
    private Long id;
    private Long ticketId;
    private Long senderId;
    private String senderRole;
    private String senderSide; // user,admin
    private String content;
    private LocalDateTime createdAt;
    private String senderName;
}
