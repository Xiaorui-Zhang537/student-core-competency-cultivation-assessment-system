package com.noncore.assessment.util;

import io.swagger.v3.oas.annotations.media.Schema;



/**
 * 统一API响应格式
 *
 * @param <T> 响应数据类型
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Schema(description = "统一API响应格式")
public class ApiResponse<T> {

    @Schema(description = "响应状态码", example = "200")
    private Integer code;

    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "响应时间戳", example = "2024-12-28 10:30:00")
    private String timestamp;

    /**
     * 私有构造方法
     */
    private ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "操作成功", null);
    }

    /**
     * 成功响应（自定义消息，无数据）
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(200, message, null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }

    /**
     * 成功响应（自定义消息和数据）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null);
    }

    /**
     * 失败响应（自定义状态码）
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 失败响应（带数据）
     */
    public static <T> ApiResponse<T> error(Integer code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }

    /**
     * 未授权响应
     */
    public static <T> ApiResponse<T> unauthorized() {
        return new ApiResponse<>(401, "未授权访问", null);
    }

    /**
     * 禁止访问响应
     */
    public static <T> ApiResponse<T> forbidden() {
        return new ApiResponse<>(403, "禁止访问", null);
    }

    /**
     * 资源未找到响应
     */
    public static <T> ApiResponse<T> notFound() {
        return new ApiResponse<>(404, "资源未找到", null);
    }

    /**
     * 参数验证失败响应
     */
    public static <T> ApiResponse<T> validationError(String message) {
        return new ApiResponse<>(422, message, null);
    }

    // Getter和Setter方法
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
} 