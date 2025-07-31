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
import com.noncore.assessment.service.AuthService;
import com.noncore.assessment.exception.BusinessException;

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
    private final AuthService authService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, AuthService authService) {
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                              @NonNull HttpServletResponse response,
                              @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token)) {
            try {
                if (jwtUtil.validateToken(token)) {
                    // 检查令牌是否已在黑名单中（已登出）
                    if (authService.isTokenBlacklisted(token)) {
                        logger.warn("认证失败：令牌已登出，在黑名单中。");
                    } else {
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

                            // 将用户ID添加到认证对象中 (Note: This is not the standard way, but preserving logic)
                            // A better approach is a custom principal object.
                            authentication.setDetails(userId);

                            // 设置到安全上下文
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            logger.debug("JWT认证成功: 用户={}, 角色={}, ID={}", username, role, userId);
                        } else {
                            logger.warn("令牌类型错误，期望access令牌但收到: {}", jwtUtil.getTokenTypeFromToken(token));
                        }
                    }
                }
            } catch (BusinessException e) {
                // 由JwtUtil抛出的、可预期的业务异常（如令牌过期、格式错误）
                logger.warn("JWT认证失败: {}", e.getMessage());
            }
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
} 