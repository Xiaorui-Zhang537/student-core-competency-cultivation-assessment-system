<<<<<<< HEAD
# **学生非核心能力发展评估系统**

> **最后更新：2025‑08‑01 | 当前版本：v1.0.1**

------



## **📚 项目概述**

本系统是一个基于 **AI** 的学生非核心能力发展评估与学习平台，旨在帮助学生发展批判性思维、创新能力、协作能力等 21 世纪核心素养。平台通过多维度评估、个性化学习路径推荐和智能分析，为教育工作者和学生提供 **全栈式能力发展支持**。

------



## **🏗️ 系统架构**

### **技术栈**

| **层次** | **技术**                                                     | **说明**                  |
| -------- | ------------------------------------------------------------ | ------------------------- |
| 后端     | Spring Boot 3.2.1 · Java 21 · MyBatis · Spring Security + JWT · Redis | RESTful API、鉴权、缓存   |
| 前端     | Vue 3.5.18 · TypeScript · Vite 5 · Tailwind CSS · Pinia · Element Plus | SPA 界面、状态管理        |
| 数据库   | MySQL 9.0.1                                                  | 22 张核心表，主从分离预留 |
| DevOps   | Maven 3.x · npm · GitHub Actions                             | CI/CD、代码质量扫描       |

------



## **🧩 核心功能模块**

1. **用户管理** – 学生 / 教师 / 管理员的注册、登录、RBAC 授权
2. **课程管理** – 课程发布、选课、章节与资源管理
3. **作业系统** – 多模态作业提交（文档 / 音视频 / PPT 等）、AI 评分
4. **能力评估** – 雷达图、趋势分析、学习建议
5. **学习路径** – 基于画像的个性化路径推荐
6. **社交学习** – 论坛、讨论区、@邀请、点赞
7. **数据分析** – 实时可视化面板、关键指标跟踪
8. **通知系统** – 邮件与 WebSocket 实时推送

------



## **🚀 快速开始（本地开发）**

### **环境要求**

- **Java 21+**
- **Node.js 20.18.0+**
- **MySQL 8.0+**
- **Redis 6.0+**（可选，用于黑名单、会话）
- **Maven 3.6+**

### **数据库初始化**

```
mysql -u root -p
CREATE DATABASE student_assessment_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
# 导入结构 & 初始数据
mysql -u root -p student_assessment_system < backend/src/main/resources/schema.sql
mysql -u root -p student_assessment_system < backend/src/main/resources/data.sql
```

### **启动后端**

```
cd backend
mvn spring-boot:run
```

后端默认监听 http://localhost:8080。

### **启动前端**

=======
# 学生非核心能力发展评估系统

> 面向“从0开始”的学习与实践平台：帮助学生发展批判性思维、创新能力、协作等非核心能力，提供评估、学习与分析的一体化支持。

### 项目状态
- 教师端：已完成
- 学生端：开发中（Beta）

---

## 技术栈（以代码为准）
- 后端：Java 17、Spring Boot 3.5.4、MyBatis、PageHelper、Spring Security（JWT）、Redis、SpringDoc OpenAPI
- 前端：Vue 3.5.18、TypeScript、Vite 5、Pinia、Tailwind CSS、Axios、ECharts
- 数据库：MySQL 8.x

---

## 环境要求
- Java 17
- Node.js 18+（建议 20+）
- Maven 3.8+
- MySQL 8.x、Redis 6.x（Redis 可选）

---

## 快速开始（本地）
1) 初始化数据库（推荐）
```
mysql -u root -p
CREATE DATABASE student_assessment_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
# 导入结构（已更新至最新）
mysql -u root -p student_assessment_system < backend/src/main/resources/schema.sql
# 可选：初始数据（data.sql 当前并非最新，如需请自行核对）
mysql -u root -p student_assessment_system < backend/src/main/resources/data.sql
```

2) 启动后端
```
cd backend
mvn spring-boot:run
# 默认 http://localhost:8080/api
```

3) 启动前端
>>>>>>> fd13d2c (ver 1.7 实现学生端工作台的互联，同时重构了进度的实现逻辑，现在教师可以设置每一节课的视频资料还有作业，只有当学生完成后，进度条才会增加。同时新增/docx目录用于让小白能够学习同时由浅入深的理解该项目。)
```
cd frontend
npm install
npm run dev
<<<<<<< HEAD
```

前端默认监听 http://localhost:5173。

> 访问 http://localhost:8080/api/swagger-ui.html 可查看在线 API 文档。

------



## **📦 运行与部署（生产环境）**

**打包后端** 

```
cd backend
mvn clean package -DskipTests
# 生成 target/student-assessment-system.jar
java -jar target/student-assessment-system.jar --spring.profiles.active=prod
```

**构建前端静态文件** 

```
cd frontend
npm run build
# 输出到 dist/
```

**Nginx 示例配置**（可选）

=======
# 默认 http://localhost:5173
```

4) 开发常用地址
- Swagger 文档：`http://localhost:8080/api/swagger-ui.html`
- API 根路径：`http://localhost:8080/api`

---

## 配置与环境变量
后端（摘自 `backend/src/main/resources/application.yml`）：
- 端口与上下文：`server.port=8080`，`server.servlet.context-path=/api`
- 数据源：`DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USERNAME`、`DB_PASSWORD`
- Redis（可选）：`REDIS_HOST`、`REDIS_PORT`、`REDIS_PASSWORD`
- JWT：`JWT_SECRET`、`JWT_EXPIRATION`、`JWT_REFRESH_EXPIRATION`
- SpringDoc：`/v3/api-docs`、`/swagger-ui.html`
- 文件上传：目录/大小/扩展名限制
- 日志：级别与日志路径
- AI：
  - `AI_DEFAULT_PROVIDER`（默认 `openrouter`）
  - `OPENROUTER_API_KEY` 或 `DEEPSEEK_API_KEY`
  - `DEEPSEEK_MODEL`（默认 `deepseek/deepseek-reasoner`）

前端：
- 统一使用 `VITE_API_BASE_URL` 指向后端基础地址（不含 `/api`），例如：
  - `.env.development`：`VITE_API_BASE_URL=http://localhost:8080`
- 说明：`src/api/config.ts` 会自动在末尾追加 `/api`；若未设置，则默认走本地代理 `/api`。

---

## 生产部署（示例）
后端
```
cd backend
mvn clean package -DskipTests
java -jar target/student-assessment-system-1.0.0.jar --spring.profiles.active=prod
```

前端
```
cd frontend
npm run build
# 产物位于 dist/
```

Nginx（示例）
>>>>>>> fd13d2c (ver 1.7 实现学生端工作台的互联，同时重构了进度的实现逻辑，现在教师可以设置每一节课的视频资料还有作业，只有当学生完成后，进度条才会增加。同时新增/docx目录用于让小白能够学习同时由浅入深的理解该项目。)
```
server {
  listen 80;
  server_name assessment.example.com;

  location / {
    root   /var/www/assessment/dist;
    try_files $uri $uri/ /index.html;
  }

  location /api/ {
    proxy_pass http://127.0.0.1:8080/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
  }
}
```

<<<<<<< HEAD
------



## **📈 系统状态**

- **后端服务**：✅ Spring Boot 启动成功（8080）
- **前端服务**：✅ Vite 开发服务器正常（5173）
- **数据库**：✅ 22 张表结构完整
- **缓存服务**：✅ Redis 连接正常

------



## **🗂️ 文件详解**

| **路径 / 文件**                      | **类型** | **说明**                                        |
| ------------------------------------ | -------- | ----------------------------------------------- |
| **backend/**                         | 目录     | Spring Boot 服务端代码与配置                    |
| ├── **pom.xml**                      | 配置     | 后端依赖与构建脚本                              |
| ├── **src/main/java/**               | 源码     | Controller / Service / Mapper / Entity 分层结构 |
| ├── **src/main/resources/**          | 资源     | application.yml、schema.sql、mapper/*.xml 等    |
| **frontend/**                        | 目录     | Vue 3 单页应用代码                              |
| ├── **package.json**                 | 配置     | Node 依赖与脚本命令                             |
| ├── **src/**                         | 源码     | 组件、页面、Pinia store、路由等                 |
| ├── **public/**                      | 资源     | 静态资源（favicon、图标等）                     |
| **API_CRITICAL_ISSUES_SUMMARY.md**   | 文档     | 关键 API 问题追踪与总结                         |
| **API_FIX_COMPLETION_REPORT.md**     | 文档     | 已修复缺陷清单                                  |
| **API_INTERFACE_TEST_REPORT.md**     | 文档     | 接口测试覆盖与结果                              |
| **ENV_CONFIG_EXAMPLE.md**            | 文档     | 环境变量示例（本地 / CI）                       |
| **ESLINT_CLEANUP_GUIDE.md**          | 文档     | 前端 ESLint 警告修复指北                        |
| **SECURITY_FIXES_SUMMARY.md**        | 文档     | 安全漏洞修复记录                                |
| **UNIMPLEMENTED_FEATURES_REPORT.md** | 文档     | 待实现功能及优先级                              |
| **pom.xml** / **package.json**       | 配置     | 根级别聚合 Maven & npm 脚本                     |

> 如需深入了解后端 / 前端目录下的包结构，请阅读各自模块内的 README.md 或查看源码注释。

------



## **🛡️ 测试与质量保障**

| **范围**      | **工具**                                 | **命令**                              |
| ------------- | ---------------------------------------- | ------------------------------------- |
| 后端单元测试  | JUnit 5 · Spring Boot Test               | mvn test                              |
| 前端单元测试  | Vitest                                   | npm run test                          |
| 静态代码扫描  | SonarLint (IDE) · SonarQube (CI)         | GitHub Actions 自动触发               |
| 格式化 & Lint | Spotless (Java) · ESLint + Prettier (TS) | mvn spotless:apply / npm run lint:fix |
| 安全检查      | OWASP Dependency‑Check                   | CI 自动执行                           |

持续集成流程存放在 .github/workflows/，包括 **测试 → 构建 → 静态扫描 → 部署** 四阶段步骤。

------



## **🤝 贡献指南**

1. Fork → 创建分支 (feat/xxx) → 提交 PR
2. Commit 遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范
3. 在 PR 描述中关联 Issue，并附带测试或截图

------



## **🔮 未来计划**

- **v1.1**：移动端 H5、离线数据同步、多语言 i18n
- **v2.0**：AI 推荐算法优化、实时协作编辑、直播授课
- **v3.0**：云原生微服务拆分、大数据分析平台、元宇宙学习空间

------



## **📝 许可证**

项目遵循 **MIT License**，详见 LICENSE 文件。

------



## **📬 支持与联系**

- Issues: https://github.com/Xiaorui-Zhang537/student-assessment-system/issues
- Discussions: https://github.com/Xiaorui-Zhang537/student-assessment-system/discussions
- 邮箱: **xiaorui537537@Gmail.com**

------



> *Made with ❤️ by Student Assessment Development Team*
=======
---

## 仓库结构（总览）
- `backend/`：Spring Boot 后端
- `frontend/`：Vue 3 前端
- `docs/`：新手手册与深入文档（见下文）

---

## 全量功能矩阵（按域）

| 功能域 | 核心能力 | 后端 Controller（示例端点） | 前端视图 | Store | API 文件 | 主要类型 |
| --- | --- | --- | --- | --- | --- | --- |
| 认证/用户 | 登录、注册、刷新、登出、资料 | `AuthController`：`POST /api/auth/login`、`/register`、`/refresh` | `features/auth/views/LoginView.vue`、`RegisterView.vue` | `stores/auth.ts` | `api/auth.api.ts`、`api/user.api.ts` | `types/auth.ts`、`types/user.ts` |
| 课程 | 课程 CRUD、分页、筛选 | `CourseController`：`GET /api/courses`、`POST /api/courses` | 教师：`ManageCourseView.vue`，学生：`CourseDetailView.vue` | `stores/course.ts` | `api/course.api.ts` | `types/course.ts` |
| 课时 | 课时 CRUD、排序 | `LessonController`：`/api/lessons` | 课程详情内嵌 | `stores/lesson.ts` | `api/lesson.api.ts` | `types/lesson.ts` |
| 作业 | 布置、提交、列表 | `AssignmentController`：`/api/assignments` | 教师：`AssignmentSubmissionsView.vue`；学生：`AssignmentSubmitView.vue` | `stores/assignment.ts`、`stores/submission.ts` | `api/assignment.api.ts`、`api/submission.api.ts` | `types/assignment.ts`、`types/submission.ts` |
| 评分 | 打分、批改、反馈 | `GradeController`：`/api/grades` | 教师：`GradeAssignmentView.vue` | `stores/grade.ts` | `api/grade.api.ts` | `types/grade.ts` |
| 能力评估 | 维度、雷达/趋势、建议 | `AbilityController`：`/api/ability/*` | 学生：`AbilityView.vue`（Beta） | `stores/ability.ts` | `api/ability.api.ts` | `types/ability.ts` |
| 社区 | 帖子、评论、点赞 | `CommunityController`：`/api/community/*` | `CommunityView.vue`、`PostDetailView.vue` | `stores/community.ts` | `api/community.api.ts` | `types/community.ts` |
| 通知 | 列表、已读、中心 | `NotificationController`：`/api/notifications` | `NotificationBell.vue`、`NotificationCenter.vue` | `stores/notifications.ts` | `api/notification.api.ts` | `types/api.ts` |
| 文件 | 上传/下载（白名单） | `FileController`：`/api/files/upload`、`/download/*` | `components/forms/FileUpload.vue` | - | `api/file.api.ts` | `types/file.ts` |
| AI 助手 | 非流式聊天、会话、记忆 | `AiController`：`POST /api/ai/chat`、`/ai/conversations` | `features/shared/views/AiAssistantView.vue` | `stores/ai.ts` | `api/ai.api.ts` | `types/api.ts` |
| 报表/分析 | 指标、导出（如有） | `ReportController`、`Dashboard*` | `DashboardView.vue` | `stores/ui.ts` 等 | `api/report.api.ts` | `types/api.ts` |

> 注：学生端仍在开发中（Beta）。详细端点与示例请见 docs 的 Backend/Frontend API Reference。

---

## 三类用户旅程（端到端）

- 教师：登录 → 创建课程 → 发布作业 → 学生提交 → 批改评分 → 仪表盘查看指标
- 学生：登录 → 选课 → 学习课时 → 提交作业 → 查看成绩与能力建议
- 管理/教务（如有）：用户与权限管理 → 全局指标与导出（选配）

每个步骤在 docs 的“端到端样例（E2E）”有详细步骤、请求/响应示例与常见错误排查。

---

## 快速排错卡片（常见 5 条）

1) Swagger 404：确认地址包含 `/api` 前缀：`http://localhost:8080/api/swagger-ui.html`
2) 前端全 404：`.env.development` 设置 `VITE_API_BASE_URL=http://localhost:8080`；或检查 Vite 代理 `/api`
3) 频繁 401：登录后 token 是否保存；Axios 是否附带 `Authorization: Bearer <token>`；过期会跳登录
4) DB 连接失败：数据库已创建+导入 `schema.sql`；`DB_USERNAME/DB_PASSWORD` 正确；URL 特殊字符转义
5) 文件上传失败：扩展名是否在白名单；大小是否超限；查看 `application.yml` 的 `file.*` 配置

## 新手手册（docs/）
为“完全小白”准备的分步文档：
- `docs/README.md`：导航页
- `docs/quickstart-beginner.md`：零基础快速上手
- `docs/architecture-overview.md`：端到端架构与请求流程
- `docs/backend-deep-dive.md`：后端深度讲解
- `docs/frontend-deep-dive.md`：前端深度讲解
- `docs/file-walkthrough.md`：逐文件导览（关键文件逐条说明）
- `docs/cookbook.md`：任务驱动的“怎么做”食谱
- `docs/faq.md`：常见问题与排坑
- `docs/glossary.md`：术语表（面向小白）
- `docs/conventions.md`：团队约定
- `docs/learning-path.md`：1–2 周学习路线
 - `docs/e2e-examples.md`：端到端样例矩阵（接口→API→Store→视图）
 - Backend/Frontend API Reference、数据模型、配置与安全（站点侧边栏可达）

---

## 贡献
1. Fork → 分支（feat/xxx）→ 提交 PR
2. Commit 遵循 Conventional Commits
3. PR 关联 Issue，并附测试或截图

---

## 许可证
本项目采用 **MIT License**。

---

## 支持与联系
- Issues: https://github.com/Xiaorui-Zhang537/student-assessment-system/issues
- Discussions: https://github.com/Xiaorui-Zhang537/student-assessment-system/discussions
- 邮箱: xiaorui537537@Gmail.com

> Made with ❤️ by Student Assessment Development Team
>>>>>>> fd13d2c (ver 1.7 实现学生端工作台的互联，同时重构了进度的实现逻辑，现在教师可以设置每一节课的视频资料还有作业，只有当学生完成后，进度条才会增加。同时新增/docx目录用于让小白能够学习同时由浅入深的理解该项目。)
