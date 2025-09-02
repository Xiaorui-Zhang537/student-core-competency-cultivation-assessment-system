# 学生核心能力培养评估系统 – 后端服务

本模块基于 **Java 17** 与 **Spring Boot 3.5.4** 构建，为前端提供稳定、安全、易扩展的 RESTful API。功能涵盖：认证与授权、课程/作业、能力评估、社区通知、AI 助手等。

---

## 1 项目概览

* **多维能力评估**：自定义能力维度，融合作业成绩、课程学习、社区活跃度等数据，生成雷达图与趋势分析，并给出学习建议。
* **课程与作业管理**：教师创建课程/章节/作业，学生选课、提交作业，系统实时跟踪学习进度与成绩。
* **认证与授权**：Spring Security 6.5 + JWT，无状态登录、刷新令牌、细粒度接口权限。
* **课程发现与推荐**：`CourseDiscoveryService` 提供关键词搜索、分类过滤与基于能力短板的推荐。
* **社区互动**：帖子、评论、点赞与通知统一推送，敏感词过滤，支持图片/文件上传。
* **实时教学分析**：`AnalyticsQueryService` 输出班级能力分布、作业完成率、成绩分段等指标，可对接 Prometheus + Grafana 实时可视化。

---

## 2 技术栈（以代码为准）

| 类别   | 组件                                | 版本         | 说明                     |
|------|-----------------------------------|------------|------------------------|
| 核心框架 | Spring Boot                       | **3.5.4**  | 自动装配、Actuator 监控       |
| 语言   | Java                              | 17           | LTS 支持                 |
| 构建   | Maven                             | 3.9+       | 生命周期管理                 |
| Web  | Spring MVC                        | 6.x        | REST/Validation        |
| 安全   | Spring Security                   | 6.5        | JWT/CORS/方法级授权         |
| JWT  | JJWT                              | 0.12.6     | 令牌生成/解析/刷新             |
| 数据库  | MySQL                             | 8.0+       | 主数据存储                  |
| ORM  | MyBatis‑Spring                    | 3.0.5      | Mapper + XML/注解        |
| 分页   | PageHelper                        | 2.1.0      | 物理分页插件                 |
| 缓存   | Redis                             | 6.0+       | 热点缓存/分布式锁              |
| 文档   | SpringDoc OpenAPI                 | 2.3.0      | Swagger UI & OpenAPI 3 |
| 上传   | Commons FileUpload                | 1.5        | 大文件分片上传                |
| JSON | Jackson                           | 内置         | 序列化/反序列化               |
| 测试   | JUnit 5, Mockito                  | 最新         | 单元 / 集成测试              |
| 监控   | Micrometer + Prometheus / Grafana | 可选         | 指标采集与可视化               |

---

## 3 架构与目录结构

```
Controller → Service → Mapper → Entity → Database
          ↘ Util / Config / Validation / Exception ↙
```

### 3.1 目录结构

```
backend/
├── pom.xml                         # Maven 配置
├── src/main/java/com/noncore/assessment
│   ├── AssessmentApplication.java   # 应用入口（@SpringBootApplication, @EnableCaching, @MapperScan）
│   ├── config/                     # 安全、JWT、Redis、Swagger 等配置
│   ├── controller/                # REST API 控制器
│   ├── dto/                       # 请求/响应 DTO
│   ├── entity/                    # 数据库实体
│   ├── exception/                 # 统一异常处理
│   ├── mapper/                    # MyBatis 接口
│   ├── service/                   # 业务接口
│   │   └── impl/                  # 服务实现
│   ├── util/                      # 工具类（JwtUtil, ApiResponse, PageResult…）
│   └── validation/                # 自定义校验注解与校验器
├── src/main/resources
│   ├── mapper/                    # MyBatis XML
│   ├── application.yml            # 公共配置（含 server.context-path=/api）
│   ├── application-dev.yml        # 开发配置（可选）
│   ├── application-prod.yml       # 生产配置（可选）
│   ├── schema.sql                 # 主表结构 DDL
│   ├── ability_schema.sql         # 能力评估表结构 DDL
│   └── data.sql                   # 初始数据
└── src/test/java/com/noncore/assessment
    └── controller/ApiIntegrationTest.java  # MockMvc 集成测试（如已添加）
```

---

## 4 文件索引（自顶向下）

| 路径                                                 | 类型    | 主要职责                                                    |
|----------------------------------------------------|-------|---------------------------------------------------------|
| `pom.xml`                                          | Build | 定义依赖与插件；Profile：`dev`、`prod`                            |
| `src/main/java/com/.../AssessmentApplication.java` | 启动类   | `@SpringBootApplication`、`@EnableCaching`、`@MapperScan` |
| `config/SecurityConfig.java`                       | 配置    | 密码编码器、CORS、静态资源放行、`JwtAuthenticationFilter`               |
| `config/JwtAuthenticationFilter.java`              | 过滤器   | 解析/校验 JWT + DPoP；设置 `SecurityContext`                   |
| `config/RedisConfig.java`                          | 配置    | `RedisTemplate` 序列化、缓存失效策略                              |
| `controller/AuthController.java`                   | 控制器   | 注册、登录、刷新令牌、登出                                           |
| `controller/CourseController.java`                 | 控制器   | 课程 CRUD、分页搜索、分类过滤                                       |
| `controller/AbilityController.java`                | 控制器   | 能力维度 CRUD、评估查询、报告导出                                     |
| `service/impl/CourseServiceImpl.java`              | 服务实现  | 课程业务：教师/学生鉴权、状态流转                                       |
| `service/impl/AbilityServiceImpl.java`             | 服务实现  | **加权平均 + 指数衰减** 评分算法                                    |
| `mapper/UserMapper.xml`                            | SQL   | 用户表 CRUD、动态条件分页                                         |
| `mapper/CourseMapper.xml`                          | SQL   | 课程与教师/学生关联查询                                            |
| `util/JwtUtil.java`                                | 工具    | 生成/解析/刷新 JWT；JTI 黑名单                                    |
| `util/PageResult.java`                             | 工具    | 统一分页响应体                                                 |
| `exception/GlobalExceptionHandler.java`            | 全局异常  | 捕获校验/业务异常，标准化输出                                         |
| `schema.sql`                                       | DDL   | `user`、`course`、`assignment` 等核心表                       |
| `ability_schema.sql`                               | DDL   | `ability_dimension`、`ability_record` 表                  |
| `data.sql`                                         | 初始化   | 角色、默认能力维度、示例课程                                          |
| （无）                                             | 运维    | 当前仓库未提供 Docker/Compose 文件                         |

---

## 5 核心功能模块与实现文件

### 5.1 认证与授权

* **功能**：JWT 鉴权；刷新令牌；权限校验
* **实现**：`SecurityConfig`, `JwtAuthenticationFilter`, `AuthController`, `JwtUtil`, `RedisConfig`

### 5.2 课程与作业管理

* **功能**：课程/章节/作业全生命周期；草稿、截止日期、评分流水线
* **实现**：`CourseController`, `LessonController`, `AssignmentController`, `CourseServiceImpl`, `SubmissionServiceImpl`, `CourseMapper.xml`

### 5.3 能力评估

* **功能**：融合成绩、社区活跃度；算法：指数衰减加权；输出雷达/趋势图
* **实现**：`AbilityController`, `AbilityServiceImpl`, `ability_schema.sql`, `AbilityMapper.xml`

### 5.4 社区互动与通知

* **功能**：帖子/评论/点赞；WebSocket & 邮件通知；敏感词过滤
* **实现**：`CommunityController`, `NotificationService`, `NotificationMapper.xml`, `util/SensitiveWordUtil.java`

### 5.5 实时教学分析

* **功能**：Micrometer → Prometheus → Grafana 仪表盘；班级能力、成绩分布、RPS
* **实现**：`AnalyticsQueryService`, `application-prod.yml` (Actuator 端点)

### 5.6 运维说明

* 当前仓库未纳入 Docker/Compose 与 CI 配置，按需自行添加。

---

## 6 运行与部署

### 6.1 本地开发

```bash
mvn spring-boot:run
# Swagger UI（注意 context-path=/api）
open http://localhost:8080/api/swagger-ui.html
```

### 6.2 生产部署

```bash
mvn clean package -DskipTests
java -jar target/student-assessment-system-1.0.0.jar --spring.profiles.active=prod
```

---

## 7 配置与环境变量（关键项）

来自 `application.yml`（可通过环境变量覆盖）：
- 端口/上下文：`server.port=8080`、`server.servlet.context-path=/api`
- 数据源：`DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME`、`DB_PASSWORD`
- Redis：`REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`（可选）
- JWT：`JWT_SECRET`、`JWT_EXPIRATION`、`JWT_REFRESH_EXPIRATION`
- SpringDoc：`/v3/api-docs`、`/swagger-ui.html`
- 上传：`file.upload-dir`、大小与扩展名限制
- 日志：控制台与文件输出
- AI：`AI_DEFAULT_PROVIDER`、`OPENROUTER_API_KEY` 或 `DEEPSEEK_API_KEY`、`DEEPSEEK_MODEL`

---

## 8 数据库初始化
```bash
mysql -u root -p
CREATE DATABASE student_assessment_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
mysql -u root -p student_assessment_system < src/main/resources/schema.sql
# 可选：初始数据（当前非最新）
mysql -u root -p student_assessment_system < src/main/resources/data.sql
```

---

## 9 测试与质量

| 类型   | 工具                | 范围             |
|------|-------------------|----------------|
| 单元测试 | JUnit 5 + Mockito | Service / Util |
| 集成测试 | MockMvc + H2      | Controller 层   |
| 静态扫描 | SonarQube         | 代码质量 & 覆盖率     |
| 压测   | Gatling / JMeter  | RPS & P95 延迟   |

---

## 10 关键用例导航

- 分页查询：`PageHelper` 使用与 `PageResult<T>` 返回体（见 docs/cookbook 与 Backend Deep Dive）
- 统一返回体：`ApiResponse<T>` 格式与错误码约定（见 docs/backend-deep-dive）
- 文件上传：`/api/files/upload` 配置（白名单、大小、目录），前端 `FileUpload.vue` 对接（见 docs/e2e-examples）
- 鉴权与放行：`JwtAuthenticationFilter` 与 `security.jwt.public-urls` 列表（见 docs/security-config）

---

## 11 排错指南（常见问题）

- Swagger 404：确认 `http://localhost:8080/api/swagger-ui.html`（含 `/api` 前缀）
- 401：检查登录与 `Authorization: Bearer <token>`，公共 URL 是否在放行清单
- 403：方法级权限不足，检查角色/注解与业务条件
- 500：查看 `logs/application.log`，关注 Mapper SQL 与空指针
- DB 连接：库已创建且导入 `schema.sql`；`DB_*` 环境变量正确

## 12 推荐阅读

| 类型   | 覆盖范围             | 工具                         |
|------|------------------|----------------------------|
| 单元测试 | Service, Util    | JUnit 5 + Mockito          |
| 集成测试 | Controller & 数据层 | Spring Boot Test + MockMvc |
| 代码质量 | 依赖漏洞 & 规约        | SonarQube                  |

---

## 13 许可证
本模块遵循 **MIT License**（与仓库一致）。
