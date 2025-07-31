package com.noncore.assessment.exception;

/**
 * 自定义业务异常类
 * 用于处理业务逻辑中的异常情况
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
public class BusinessException extends RuntimeException {

    private Integer code;
    private String message;

    /**
     * 默认构造方法
     */
    public BusinessException() {
        super();
        this.code = 400;
    }

    /**
     * 构造方法
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
        this.message = message;
    }

    /**
     * 构造方法
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 基于ErrorCode的构造方法
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    /**
     * 基于ErrorCode的构造方法（带自定义消息）
     */
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
        this.message = customMessage;
    }

    /**
     * 基于ErrorCode的构造方法（带自定义消息和异常原因）
     */
    public BusinessException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.code = errorCode.getCode();
        this.message = customMessage;
    }

    /**
     * 构造方法
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 400;
        this.message = message;
    }

    /**
     * 构造方法
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    /**
     * 基于ErrorCode的构造方法（带异常原因）
     */
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
} 