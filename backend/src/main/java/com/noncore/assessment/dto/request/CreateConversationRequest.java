package com.noncore.assessment.dto.request;

import lombok.Data;

@Data
public class CreateConversationRequest {
    private String title;
    private String model;
    private String provider;
}


