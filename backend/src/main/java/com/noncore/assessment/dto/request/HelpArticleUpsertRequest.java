package com.noncore.assessment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class HelpArticleUpsertRequest {

    @Schema(description = "分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "文章标题", example = "学生端上传作业失败怎么办")
    private String title;

    @Schema(description = "文章 slug，留空时可根据标题生成", example = "student-upload-failed")
    private String slug;

    @Schema(description = "Markdown / 纯文本内容", example = "请先检查文件大小和网络状态。")
    private String contentMd;

    @Schema(description = "可选 HTML 内容，留空时由后端根据 contentMd 生成")
    private String contentHtml;

    @Schema(description = "标签，逗号分隔", example = "上传,作业,学生端")
    private String tags;

    @Schema(description = "是否发布", example = "true")
    private Boolean published;
}
