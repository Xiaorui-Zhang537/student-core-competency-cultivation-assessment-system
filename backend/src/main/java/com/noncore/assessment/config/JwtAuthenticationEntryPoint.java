package com.noncore.assessment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noncore.assessment.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT认证入口点
 * 处理未认证的请求，返回401错误
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        
        logger.warn("未授权访问: {} {}", request.getMethod(), request.getRequestURI());
        
        // 设置响应状态和内容类型
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // 创建错误响应
        ApiResponse<Object> errorResponse = ApiResponse.error(401, "未授权访问，请先登录");
        
        // 写入响应
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
} 