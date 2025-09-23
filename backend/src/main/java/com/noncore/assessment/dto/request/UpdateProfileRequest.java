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
    @Schema(description = "头像URL", example = "https://example.com/new-avatar.jpg")
    private String avatar;

    @Schema(description = "性别")
    private String gender;

    @Schema(description = "MBTI 人格类型(16种)")
    @Size(max = 4)
    private String mbti;

    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    @Schema(description = "个人简介", example = "这是我的新简介。")
    private String bio;

    @Schema(description = "生日，格式yyyy-MM-dd")
    private String birthday;

    @Schema(description = "国家")
    private String country;

    @Schema(description = "省份/州")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "名")
    @Size(max = 50)
    private String firstName;

    @Schema(description = "姓")
    @Size(max = 50)
    private String lastName;

    @Schema(description = "学校")
    @Size(max = 100)
    private String school;

    @Schema(description = "专业/科目")
    @Size(max = 100)
    private String subject;

    @Schema(description = "学号")
    @Size(max = 50)
    private String studentNo;

    @Schema(description = "工号")
    @Size(max = 50)
    private String teacherNo;

} 