package com.noncore.assessment.config;

import com.noncore.assessment.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 * 处理JWT令牌的验证和用户认证
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                              @NonNull HttpServletResponse response, 
                              @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 从请求中获取JWT令牌
            String token = getTokenFromRequest(request);
            
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                // 令牌有效，提取用户信息
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);
                
                // 检查是否为访问令牌
                if (jwtUtil.isAccessToken(token)) {
                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            username, 
                            null, 
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        );
                    
                    // 设置认证详情
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 将用户ID添加到认证对象中
                    authentication.setDetails(userId);
                    
                    // 设置到安全上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    logger.debug("JWT认证成功: 用户={}, 角色={}, ID={}", username, role, userId);
                } else {
                    logger.warn("令牌类型错误，期望access令牌但收到: {}", jwtUtil.getTokenTypeFromToken(token));
                }
            }
        } catch (Exception e) {
            logger.error("JWT认证失败: {}", e.getMessage(), e);
            // 不抛出异常，让请求继续，由Security配置处理未认证用户
        }
        
        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中提取JWT令牌
     * 支持从Authorization头部或URL参数中获取
     *
     * @param request HTTP请求
     * @return JWT令牌字符串
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 1. 从Authorization头部获取Bearer令牌
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        // 2. 从URL参数获取token（用于WebSocket等场景）
        String token = request.getParameter("token");
        if (StringUtils.hasText(token)) {
            return token;
        }
        
        // 3. 从自定义头部获取（兼容性）
        String customToken = request.getHeader("X-Auth-Token");
        if (StringUtils.hasText(customToken)) {
            return customToken;
        }
        
        return null;
    }

    /**
     * 检查是否应该跳过此过滤器
     * 对于公开API，跳过JWT验证
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // 检查是否为公开路径
        return SecurityConfig.isPublicPath(path);
    }
} 