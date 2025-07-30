package com.noncore.assessment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security配置类
 * 配置认证、授权、会话管理等安全相关设置
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity()
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    /**
     * 公开访问的路径数组
     */
    private static final String[] PUBLIC_PATHS = {
        // 认证相关接口
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/forgot-password",
        "/api/auth/reset-password",
        "/api/auth/verify-email",
        "/api/auth/check-username",
        "/api/auth/check-email",
        
        // API文档相关
        "/api/swagger-ui/**",
        "/api/swagger-ui.html",
        "/api/v3/api-docs/**",
        "/api/swagger-resources/**",
        "/api/webjars/**",
        
        // 健康检查和监控
        "/api/actuator/health",
        "/api/actuator/info",
        
        // 静态资源
        "/static/**",
        "/public/**",
        "/favicon.ico",
        
        // 文件下载（公开文件）
        "/api/files/download/public/**"
    };

    /**
     * 配置HTTP安全
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护（因为使用JWT）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 启用CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 配置会话管理
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 配置异常处理
            .exceptionHandling(exceptions -> 
                exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            
            // 配置路径访问权限
            .authorizeHttpRequests(authz -> authz
                // 公开路径，允许所有用户访问
                .requestMatchers(PUBLIC_PATHS).permitAll()
                
                // 管理员专用接口
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 教师相关接口
                .requestMatchers("/api/teacher/**").hasAnyRole("TEACHER", "ADMIN")
                
                // 学生相关接口
                .requestMatchers("/api/student/**").hasAnyRole("STUDENT", "ADMIN")
                
                // 文件上传需要认证
                .requestMatchers("/api/files/upload/**").authenticated()
                
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            )
            
            // 添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 检查给定路径是否为公开路径
     *
     * @param requestPath 请求路径
     * @return true如果是公开路径，false如果需要认证
     */
    public static boolean isPublicPath(String requestPath) {
        if (requestPath == null) {
            return false;
        }
        
        for (String publicPath : PUBLIC_PATHS) {
            // 处理通配符匹配
            if (publicPath.endsWith("/**")) {
                String prefix = publicPath.substring(0, publicPath.length() - 3);
                if (requestPath.startsWith(prefix)) {
                    return true;
                }
            } else if (publicPath.equals(requestPath)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * CORS配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的前端域名
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173", 
            "http://localhost:8080",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:8080"
        ));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "X-Request-Time"
        ));
        
        // 允许发送Cookie
        configuration.setAllowCredentials(true);
        
        // 预检请求的缓存时间（秒）
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    public JwtAccessDeniedHandler getJwtAccessDeniedHandler() {
        return jwtAccessDeniedHandler;
    }
}