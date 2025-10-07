package com.noncore.assessment.config;

import com.noncore.assessment.service.AuthService;
import com.noncore.assessment.util.JwtUtil;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.http.HttpMethod;
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
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

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

    @Getter
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final SecurityProperties securityProperties;

    public SecurityConfig(JwtAccessDeniedHandler jwtAccessDeniedHandler,
                          SecurityProperties securityProperties) {
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.securityProperties = securityProperties;
    }

    /**
     * 配置HTTP安全
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAccessDeniedHandler jwtAccessDeniedHandler,
                                           JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                                           AuthService authService,
                                           JwtUtil jwtUtil) throws Exception {

        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, authService);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 允许被前端页面内嵌 (PDF IFrame 预览需要取消 X-Frame-Options)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions ->
                        exceptions
                                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                                .accessDeniedHandler(jwtAccessDeniedHandler))
                .authorizeHttpRequests(authz ->
                        authz
                            // 预检请求直接放行，确保返回CORS头
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            // allow all auth endpoints under /auth (login/register/verify/resend)
                            .requestMatchers("/auth/**")
                                .permitAll()
                            // allow forgot/reset password endpoints
                            .requestMatchers("/users/forgot-password")
                                .permitAll()
                            .requestMatchers("/users/reset-password")
                                .permitAll()
                            // allow public confirm email change endpoint
                            .requestMatchers("/users/email/change/confirm")
                                .permitAll()
                            // preserve any additional public URLs from config
                            .requestMatchers(securityProperties.getJwt().getPublicUrls().toArray(new String[0]))
                                .permitAll()
                            .anyRequest()
                                .authenticated()
                )
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
     * CORS配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow the development origins (both local IP and localhost)
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://0.0.0.0:5173",
            "http://*.local:5173",
            "http://*.lan:5173",
            "https://www.stucoreai.space",
            "http://www.stucoreai.space",
            "https://stucoreai.space",
            "http://stucoreai.space"
        ));

        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("*"));

        // 允许发送Cookie
        configuration.setAllowCredentials(true);

        // 预检请求的缓存时间（秒）
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * 全局 CORS 过滤器（最高优先级），确保预检与实际响应都带上 CORS 头
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://0.0.0.0:5173",
            "http://*.local:5173",
            "http://*.lan:5173",
            "https://www.stucoreai.space",
            "http://www.stucoreai.space",
            "https://stucoreai.space",
            "http://stucoreai.space"
        ));
        config.addAllowedHeader("*");
        config.addExposedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

}