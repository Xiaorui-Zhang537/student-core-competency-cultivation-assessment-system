package com.noncore.assessment.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class PostCreateRequest {
    private String title;
    private String content;
    private String category;
    private Boolean allowComments;
    private Boolean anonymous;
    private Boolean pinned;
    private List<String> tags;
}

