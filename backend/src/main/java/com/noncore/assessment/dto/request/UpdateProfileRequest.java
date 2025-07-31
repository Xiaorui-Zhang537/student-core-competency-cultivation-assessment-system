package com.noncore.assessment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "用户资料更新请求模型")
public class UpdateProfileRequest {

    @Size(max = 50, message = "昵称长度不能超过50个字符")
    @Schema(description = "用户昵称", example = "新的昵称")
    private String nickname;

    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    @Schema(description = "头像URL", example = "http://example.com/new-avatar.jpg")
    private String avatar;

    @Schema(description = "性别")
    private String gender;

    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    @Schema(description = "个人简介", example = "这是我的新简介。")
    private String bio;

} 