package com.noncore.assessment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class SubmissionRequest {
    @Schema(description = "提交内容")
    private String content;

    @Schema(description = "已上传文件ID列表（取第一个作为主附件）")
    private List<Long> fileIds;
}


