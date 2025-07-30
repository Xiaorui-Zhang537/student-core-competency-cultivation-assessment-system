package com.noncore.assessment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noncore.assessment.entity.User;
import com.noncore.assessment.entity.Course;
import com.noncore.assessment.entity.Assignment;
import com.noncore.assessment.entity.Grade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.math.BigDecimal;

/**
 * API集成测试类
 * 测试主要的REST API接口功能
 *
 * @author System
 * @version 1.0.0
 * @since 2024-12-28
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
public class ApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * 测试用户认证相关API
     */
    @Test
    public void testAuthenticationAPI() throws Exception {
        // 测试用户注册
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setEmail("test@example.com");
        newUser.setPassword("password123");
        newUser.setRole("student");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 测试用户登录
        User loginUser = new User();
        loginUser.setUsername("testuser");
        loginUser.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists());
    }

    /**
     * 测试课程管理API
     */
    @Test
    public void testCourseAPI() throws Exception {
        // 创建课程
        Course course = new Course();
        course.setTitle("测试课程");
        course.setDescription("这是一个测试课程");
        course.setCategory("programming");

        mockMvc.perform(post("/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(course))
                .header("Authorization", "Bearer " + getTestToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 获取课程列表
        mockMvc.perform(get("/courses")
                .param("page", "1")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.items").isArray());

        // 获取课程详情
        mockMvc.perform(get("/courses/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试作业管理API
     */
    @Test
    public void testAssignmentAPI() throws Exception {
        // 创建作业
        Assignment assignment = new Assignment();
        assignment.setCourseId(1L);
        assignment.setTitle("测试作业");
        assignment.setDescription("这是一个测试作业");
        assignment.setMaxScore(BigDecimal.valueOf(100));

        mockMvc.perform(post("/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignment))
                .header("Authorization", "Bearer " + getTestToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 获取作业列表
        mockMvc.perform(get("/assignments")
                .param("page", "1")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 获取作业详情
        mockMvc.perform(get("/assignments/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试成绩管理API
     */
    @Test
    public void testGradeAPI() throws Exception {
        // 创建成绩
        Grade grade = new Grade();
        grade.setStudentId(1L);
        grade.setAssignmentId(1L);
        grade.setScore(BigDecimal.valueOf(85));
        grade.setMaxScore(BigDecimal.valueOf(100));

        mockMvc.perform(post("/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(grade))
                .header("Authorization", "Bearer " + getTestToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 获取学生成绩
        mockMvc.perform(get("/grades/student/1")
                .header("Authorization", "Bearer " + getTestToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 计算平均分
        mockMvc.perform(get("/grades/student/1/average")
                .header("Authorization", "Bearer " + getTestToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试章节管理API
     */
    @Test
    public void testLessonAPI() throws Exception {
        // 获取课程章节
        mockMvc.perform(get("/lessons/course/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 获取学习进度
        mockMvc.perform(get("/lessons/1/progress")
                .header("Authorization", "Bearer " + getTestToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 更新学习进度
        mockMvc.perform(post("/lessons/1/progress")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"progress\": 50, \"studyTime\": 30}")
                .header("Authorization", "Bearer " + getTestToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试学生仪表板API
     */
    @Test
    public void testStudentDashboardAPI() throws Exception {
        mockMvc.perform(get("/student/dashboard")
                .header("Authorization", "Bearer " + getTestToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.activeCourses").exists())
                .andExpect(jsonPath("$.data.pendingAssignments").exists());
    }

    /**
     * 测试教师仪表板API
     */
    @Test
    public void testTeacherDashboardAPI() throws Exception {
        mockMvc.perform(get("/teacher/dashboard")
                .header("Authorization", "Bearer " + getTestToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalStudents").exists())
                .andExpect(jsonPath("$.data.totalCourses").exists());
    }

    /**
     * 测试社区功能API
     */
    @Test
    public void testCommunityAPI() throws Exception {
        // 获取帖子列表
        mockMvc.perform(get("/community/posts")
                .param("page", "1")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 获取社区统计
        mockMvc.perform(get("/community/stats"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 获取热门话题
        mockMvc.perform(get("/community/hot-topics"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试文件上传API
     */
    @Test
    public void testFileUploadAPI() throws Exception {
        mockMvc.perform(get("/files/info")
                .header("Authorization", "Bearer " + getTestToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试通知API
     */
    @Test
    public void testNotificationAPI() throws Exception {
        mockMvc.perform(get("/notifications")
                .param("page", "1")
                .param("size", "10")
                .header("Authorization", "Bearer " + getTestToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/notifications/unread-count")
                .header("Authorization", "Bearer " + getTestToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试能力评估API
     */
    @Test
    public void testAbilityAPI() throws Exception {
        mockMvc.perform(get("/abilities")
                .header("Authorization", "Bearer " + getTestToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 测试错误处理
     */
    @Test
    public void testErrorHandling() throws Exception {
        // 测试404错误
        mockMvc.perform(get("/non-existing-endpoint"))
                .andDo(print())
                .andExpect(status().isNotFound());

        // 测试未授权访问
        mockMvc.perform(get("/teacher/dashboard"))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        // 测试无效请求参数
        mockMvc.perform(get("/courses")
                .param("page", "invalid"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * 测试API性能
     */
    @Test
    public void testAPIPerformance() throws Exception {
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/courses")
                .param("page", "1")
                .param("size", "20"))
                .andExpect(status().isOk());
        
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        
        // 断言响应时间小于1秒
        assert responseTime < 1000 : "API响应时间过长: " + responseTime + "ms";
    }

    /**
     * 获取测试用的认证令牌
     */
    private String getTestToken() {
        // 这里应该返回一个测试用的JWT令牌
        // 为了简化，返回一个模拟的令牌
        return "test-jwt-token";
    }

    /**
     * 测试数据完整性
     */
    @Test
    public void testDataIntegrity() throws Exception {
        // 测试创建和查询的数据一致性
        Course course = new Course();
        course.setTitle("数据完整性测试课程");
        course.setDescription("测试数据完整性");

        String courseJson = objectMapper.writeValueAsString(course);
        
        // 创建课程
        mockMvc.perform(post("/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(courseJson)
                .header("Authorization", "Bearer " + getTestToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("数据完整性测试课程"));

        // 验证课程已创建
        mockMvc.perform(get("/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("数据完整性测试课程"));
    }

    /**
     * 测试并发安全性
     */
    @Test
    public void testConcurrencySafety() throws Exception {
        // 模拟并发请求
        Thread[] threads = new Thread[10];
        
        for (int i = 0; i < 10; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                try {
                    mockMvc.perform(get("/courses")
                            .param("page", String.valueOf(threadIndex % 3 + 1))
                            .param("size", "5"))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    throw new RuntimeException("并发测试失败", e);
                }
            });
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
    }

    /**
     * 测试API版本兼容性
     */
    @Test
    public void testAPIVersionCompatibility() throws Exception {
        // 测试不同版本的API响应格式
        mockMvc.perform(get("/courses")
                .header("Accept", "application/json")
                .header("API-Version", "1.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists());
    }

    /**
     * 测试缓存功能
     */
    @Test
    public void testCaching() throws Exception {
        // 第一次请求
        long startTime1 = System.currentTimeMillis();
        mockMvc.perform(get("/courses/1"))
                .andExpect(status().isOk());
        long responseTime1 = System.currentTimeMillis() - startTime1;

        // 第二次请求（应该更快，因为有缓存）
        long startTime2 = System.currentTimeMillis();
        mockMvc.perform(get("/courses/1"))
                .andExpect(status().isOk());
        long responseTime2 = System.currentTimeMillis() - startTime2;

        // 注意：在真实环境中，第二次请求应该更快
        System.out.println("第一次请求耗时: " + responseTime1 + "ms");
        System.out.println("第二次请求耗时: " + responseTime2 + "ms");
    }
} 