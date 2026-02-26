<div align="center">
  <img src="../brand/logo.png" width="96" alt="StuCoreAI logo" />
  <h1>Backend（Spring Boot）</h1>
  <p>面向可部署系统的 API 服务：LLM 稳定性控制 · 结构化输出治理 · 课程/作业/能力/社区/通知/AI</p>
  <p>
    <a href="../README.md">返回项目首页</a>
    · <a href="#quickstart">快速开始</a>
    · <a href="#llm-governance">LLM 治理</a>
    · <a href="#api-domains">API 域划分</a>
    · <a href="#security">鉴权与安全</a>
    · <a href="#config">配置</a>
  </p>
  <p>
    <img alt="Java" src="https://img.shields.io/badge/Java-17-informational" />
    <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.5.4-informational" />
    <img alt="MyBatis" src="https://img.shields.io/badge/MyBatis-3-informational" />
    <img alt="MySQL" src="https://img.shields.io/badge/MySQL-8.x-informational" />
  </p>
</div>

## 项目定位

后端提供统一 REST API，支撑学生/教师/管理员三端业务，并集成 LLM 相关能力用于过程性分析与形成性反馈。

- API 前缀：`/api`（见 `application.yml` 的 `server.servlet.context-path`）
- Swagger（开发）：`/api/swagger-ui.html`

<a id="llm-governance"></a>
## LLM 治理（Engineering Highlights）

本项目将 LLM 视为“不稳定上游”，后端负责把输出治理为 **稳定、可追溯、结构可控** 的结果：

- **多次采样稳定化（2 次 + 分差触发第 3 次）**：`mean2 / closest2of3`，并输出 `meta.ensemble` 便于排障与解释。实现见 `src/main/java/com/noncore/assessment/service/ai/AiGradingEnsembler.java`，文档见 `../docs/backend/api/ai-grading.md`。
- **结构归一化**：兼容多种模型输出结构，统一为标准 JSON contract（前端依赖字段始终存在）。实现见 `src/main/java/com/noncore/assessment/service/ai/AiGradingNormalizer.java`。
- **确定性后处理**：对子项分数均值、证据/建议去重合并、`holistic_feedback` 模板化，避免继续抖动。实现见 `AiGradingEnsembler`。
- **行为洞察（不算分）治理**：证据白名单校验 + 禁止评分字段（避免 AI 越权“算分/加权”）。实现见 `src/main/java/com/noncore/assessment/service/impl/BehaviorInsightGenerationServiceImpl.java`，文档见 `../docs/backend/api/behavior.md`。
- **流式稳定化**：提供 SSE 接口逐次推送每次取样结果，改善等待体验：`POST /ai/grade/essay/stream`（见 `AiController`）。

## 技术栈

- Java 17
- Spring Boot `3.5.4`
- Spring Security + JWT（JJWT）
- MyBatis + PageHelper
- SpringDoc OpenAPI
- MySQL 8.x
- WebSocket（AI Live）与 SSE（通知流）

## 目录结构（核心）

```text
backend/src/main/java/com/noncore/assessment/
├── controller/         # 控制器（含 admin 子目录）
├── service/            # 业务接口 + impl
├── mapper/             # MyBatis Mapper 接口
├── entity/             # 实体
├── dto/                # 请求/响应 DTO
├── config/             # 安全/JWT/AI 等配置
├── realtime/           # SSE 与 AI Live WebSocket
├── exception/          # 全局异常
├── validation/         # 自定义校验
└── util/               # 工具

backend/src/main/resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
├── mapper/*.xml
├── schema.sql
├── ability_schema.sql
└── data.sql
```

<a id="quickstart"></a>
## 快速开始

### 1) 初始化数据库

```bash
mysql -u root -p
CREATE DATABASE student_assessment_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 从仓库根目录执行时：
mysql -u root -p student_assessment_system < backend/src/main/resources/schema.sql

# 或者进入 backend/ 目录再执行：
# cd backend
# mysql -u root -p student_assessment_system < src/main/resources/schema.sql
```

### 2) 启动

```bash
cd backend
mvn spring-boot:run
```

- API 根：`http://localhost:8080/api`
- Swagger：`http://localhost:8080/api/swagger-ui.html`

<a id="api-domains"></a>
## API 域划分

控制器类级 `@RequestMapping`（不含 `/api` 前缀）：

- 认证与用户：`/auth` `/users`
- 教学：`/courses` `/chapters` `/lessons` `/assignments` `/grades`
- 角色数据：`/students` `/teachers` `/teachers/students` `/teachers/ability`
- 协作：`/community` `/chat` `/notifications`
- 文件与帮助：`/files` `/help` `/reports` `/public/project`
- 能力/行为：`/ability` `/behavior` `/behavior/insights`
- AI：`/ai` `/ai/voice`
- 管理员：`/admin/*`

说明：`SubmissionController` 为组合路径控制器（无统一类级前缀）。

<a id="security"></a>
## 鉴权与安全

实现见：`src/main/java/com/noncore/assessment/config/SecurityConfig.java`、`JwtAuthenticationFilter.java`。

- 无状态：`SessionCreationPolicy.STATELESS`
- 放行：
  - `OPTIONS /**`
  - `/auth/**`
  - 忘记/重置密码、邮箱确认等
  - `security.jwt.public-urls` 配置项
- 其余接口默认需要 JWT

<a id="config"></a>
## 配置

### 运行参数

`src/main/resources/application.yml`：
- `server.port=8080`
- `server.servlet.context-path=/api`

### 环境变量（常用）

- 数据库：`DB_HOST` `DB_PORT` `DB_NAME` `DB_USERNAME` `DB_PASSWORD`
- JWT：`JWT_SECRET` `JWT_EXPIRATION` `JWT_REFRESH_EXPIRATION`
- AI：`AI_DEFAULT_PROVIDER` `GOOGLE_API_KEY` `GOOGLE_API_BASE_URL` `GLM_API_KEY` `GLM_API_BASE_URL`
- AI 代理：`AI_PROXY_ENABLED` `AI_PROXY_HOST` `AI_PROXY_PORT` `AI_PROXY_TYPE` 等

### Profiles

- 开发：`application-dev.yml`
- 生产：`application-prod.yml`（默认关闭 Swagger）

## 数据库

- 主初始化脚本：`src/main/resources/schema.sql`
- 能力模块参考：`src/main/resources/ability_schema.sql`
- 示例数据：`src/main/resources/data.sql`

## 实时能力

- SSE：`realtime/NotificationSseService`
- AI Live WebSocket：`realtime/ai/*`

## 打包部署

```bash
cd backend
mvn clean package -DskipTests
java -jar target/student-assessment-system-1.0.5.jar --spring.profiles.active=prod
```

## 相关文档

- 后端 API 参考：`../docs/backend/api/index.md`
- 后端深入：`../docs/backend-deep-dive.md`
- 安全配置：`../docs/security-config.md`
