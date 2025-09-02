package com.noncore.assessment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 学生非核心能力发展评估系统 - 后端应用启动类
 * 
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@SpringBootApplication
@EnableCaching
@MapperScan("com.noncore.assessment.mapper")
@EnableConfigurationProperties
public class AssessmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssessmentApplication.class, args);
        System.out.println("========================================");
        System.out.println("学生核心能力培养评估系统后端启动成功！");
        System.out.println("API文档地址: http://localhost:8080/api/swagger-ui.html");
        System.out.println("健康检查: http://localhost:8080/api/actuator/health");
        System.out.println("========================================");
    }
} 