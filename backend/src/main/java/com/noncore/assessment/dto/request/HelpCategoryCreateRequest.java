package com.noncore.assessment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "帮助分类创建请求")
public class HelpCategoryCreateRequest {

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类标识，留空则按名称生成")
    private String slug;

    @Schema(description = "排序值")
    private Integer sort;
}
