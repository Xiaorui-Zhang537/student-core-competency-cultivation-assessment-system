package com.noncore.assessment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noncore.assessment.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT访问拒绝处理器
 * 处理权限不足地请求，返回403错误
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(JwtAccessDeniedHandler.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException {
        
        logger.warn("访问被拒绝: {} {} - {}", request.getMethod(), request.getRequestURI(), 
                   accessDeniedException.getMessage());
        
        // 设置响应状态和内容类型
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // 创建错误响应
        ApiResponse<Object> errorResponse = ApiResponse.error(403, "权限不足，无法访问该资源");
        
        // 写入响应
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
} 