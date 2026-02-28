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

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getCategory() {
        return category;
    }

    public Boolean getAllowComments() {
        return allowComments;
    }

    public Boolean getAnonymous() {
        return anonymous;
    }

    public Boolean getPinned() {
        return pinned;
    }

    public List<String> getTags() {
        return tags;
    }
}
