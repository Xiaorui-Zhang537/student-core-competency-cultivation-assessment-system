package com.noncore.assessment.exception;

import lombok.Getter;

/**
 * 错误码枚举
 * 定义系统中所有业务异常的标准错误码和消息
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Getter
public enum ErrorCode {
    
    // 通用错误 (1000-1099)
    SYSTEM_ERROR(1000, "系统内部错误"),
    INVALID_PARAMETER(1001, "参数错误"),
    DATA_NOT_FOUND(1002, "数据不存在"),
    OPERATION_FAILED(1003, "操作失败"),
    PERMISSION_DENIED(1004, "权限不足"),
    INTERNAL_SERVER_ERROR(1005, "服务器内部错误"),
    UNAUTHORIZED_OPERATION(1006, "未授权操作"),
    
    // 用户认证相关 (1100-1199)
    LOGIN_FAILED(1100, "用户名或密码错误"),
    USER_NOT_FOUND(1101, "用户不存在"),
    USERNAME_EXISTS(1102, "用户名已存在"),
    EMAIL_EXISTS(1103, "邮箱已被注册"),
    USERNAME_ALREADY_EXISTS(1102, "用户名已存在"),
    EMAIL_ALREADY_EXISTS(1103, "邮箱已被注册"),
    PASSWORD_MISMATCH(1104, "两次输入的密码不一致"),
    PASSWORD_TOO_SHORT(1105, "密码长度至少6个字符"),
    CURRENT_PASSWORD_WRONG(1106, "当前密码错误"),
    TOKEN_INVALID(1107, "令牌无效或已过期"),
    TOKEN_TYPE_ERROR(1108, "令牌类型错误"),
    USER_NOT_VERIFIED(1109, "邮箱未验证"),
    EMAIL_NOT_VERIFIED(1109, "邮箱未验证"),
    EMAIL_ALREADY_VERIFIED(1110, "邮箱已验证，无需重复验证"),
    INVALID_TOKEN(1107, "无效的令牌"),
    INVALID_PASSWORD(1106, "密码错误"),
    EMAIL_NOT_REGISTERED(1111, "该邮箱未注册"),
    EMAIL_VERIFIED(1110, "邮箱已验证"),
    EMAIL_SEND_FAILED(1112, "邮件发送失败"),
    EMAIL_CHANGE_TOKEN_INVALID(1113, "邮箱更换确认令牌无效或已过期"),
    
    // 课程相关 (1200-1299)
    COURSE_NOT_FOUND(1200, "课程不存在"),
    COURSE_TITLE_EXISTS(1201, "课程标题已存在"),
    COURSE_HAS_STUDENTS(1202, "课程已有学生选课，无法删除"),
    COURSE_ACCESS_DENIED(1203, "您没有访问该课程的权限"),
    COURSE_OPERATION_DENIED(1204, "您没有权限操作该课程"),
    COURSE_NOT_PUBLISHED(1205, "课程未发布，无法选课"),
    COURSE_FULL(1206, "课程已满员，无法选课"),
    STUDENT_ALREADY_ENROLLED(1207, "已选过该课程"),
    STUDENT_NOT_ENROLLED(1208, "未选过该课程"),
    COURSE_ALREADY_PUBLISHED(1209, "课程已发布"),
    COURSE_ID_LIST_EMPTY(1210, "课程ID列表不能为空"),
    COURSE_TITLE_EMPTY(1211, "课程标题不能为空"),
    COURSE_DESCRIPTION_EMPTY(1212, "课程描述不能为空"),
    TEACHER_ID_EMPTY(1213, "教师ID不能为空"),
    MAX_STUDENTS_INVALID(1214, "最大学生数必须大于0"),
    COURSE_DURATION_INVALID(1215, "课程时长必须大于0"),
    COURSE_ENROLLMENT_CLOSED(1216, "报名已截止"),
    
    // 作业相关 (1300-1399)
    ASSIGNMENT_NOT_FOUND(1300, "作业不存在"),
    ASSIGNMENT_NOT_PUBLISHED(1301, "作业未发布，无法提交"),
    ASSIGNMENT_EXPIRED(1302, "作业已过期，无法提交"),
    ASSIGNMENT_ALREADY_SUBMITTED(1303, "作业已提交，无法保存草稿"),
    ASSIGNMENT_ACCESS_DENIED(1304, "您没有访问该作业的权限"),
    STUDENT_ID_REQUIRED(1305, "教师查看提交需要指定学生ID"),
    
    // 提交相关 (1400-1499)
    SUBMISSION_NOT_FOUND(1400, "提交记录不存在"),
    FILE_UPLOAD_FAILED(1401, "文件上传失败"),
    
    // 成绩相关 (1500-1599)
    GRADE_NOT_FOUND(1500, "成绩记录不存在"),
    
    // 章节相关 (1600-1699)
    LESSON_NOT_FOUND(1600, "章节不存在"),
    RATING_OUT_OF_RANGE(1601, "评分必须在1-5分之间"),
    
    // 文件相关 (1700-1799)
    FILE_NOT_FOUND(1700, "文件不存在"),
    FILE_LOST(1701, "文件已丢失"),
    FILE_EMPTY(1702, "文件不能为空"),
    FILE_SIZE_EXCEED(1703, "文件大小超过限制"),
    FILE_TYPE_UNSUPPORTED(1704, "不支持的文件类型"),
    FILE_PERMISSION_DENIED(1705, "没有权限访问该文件");
    
    private final Integer code;
    private final String message;
    
    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

} 