package com.noncore.assessment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger配置类
 * 配置API文档生成和安全认证
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080/api").description("开发环境"),
                        new Server().url("https://api.example.com/api").description("生产环境")
                ))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("请输入JWT令牌，格式: Bearer {token}")));
    }

    private Info apiInfo() {
        return new Info()
                .title("学生非核心能力发展评估系统 API")
                .description("为学生非核心能力发展评估系统提供后端API服务，支持用户认证、课程管理、作业系统、教师功能等完整业务流程。")
                .version("1.0.0")
                .contact(new Contact()
                        .name("系统开发团队")
                        .email("admin@example.com")
                        .url("https://example.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
} 