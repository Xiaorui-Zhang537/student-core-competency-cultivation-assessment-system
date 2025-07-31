package com.noncore.assessment.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class BatchNotificationRequest {
    private List<Long> recipientIds;
    private String title;
    private String content;
    private String type;
    private String category;
    private String priority;
    private String relatedType;
    private Long relatedId;
} 