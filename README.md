# 学生核心能力培养评估系统

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
```
cd frontend
npm install
npm run dev
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
  - `DEEPSEEK_MODEL`（默认 `deepseek/deepseek-chat-v3.1`）

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

---

## 仓库结构（总览）
- `backend/`：Spring Boot 后端
- `frontend/`：Vue 3 前端
- `docs/`：新手手册与深入文档（见下文）

---

## 更新日志（学生端改造）
- 国际化：学生端主要页面已统一采用 i18n，无中文兜底硬编码（`frontend/src/features/student/views/AnalyticsView.vue`、`AssignmentsView.vue`、`CourseDetailView.vue`、`GradesView.vue`）。
- 语言包：补充 `student.grades.*` 文案键（中英双语）。
- 品牌统一：统一采用玻璃拟态样式（`glass-*` 与 `v-glass`）、并复用 `components/ui` 组件。
- 接口对齐：前端 `studentApi` 与后端 `/api/students/*` 路由逐项核对；`CourseDetailView` 的课程进度改用 `studentApi.getCourseProgress`。
- 文档：更新 `docs/backend/api/student.md` 与 `docs/i18n-examples.md`。

### 本次（作业与通知重构，UI 玻璃风统一）
- 学生端「我的作业」列表（`features/student/views/AssignmentsView.vue`）
  - 过滤区：移除多余容器；状态/课程改用 `GlassPopoverSelect`（带 label/stacked）；搜索框玻璃化，内置放大镜；300ms 防抖
  - 列表展示：改为独立玻璃卡片（`glass-thin rounded-xl p-4`），提升分隔与可读性
  - 分页：分页处使用玻璃下拉，显示“每页显示”和“第 X 页”等文案，与教师端对齐
  - 状态：归一化 `crafted/draft → DRAFT`；未提交且已过期显示 `LATE`（红色徽标）；过滤未来 `scheduled`（基于 `publishAt`）
  - 响应性：加载列表时逐条拉取提交记录并 `new Map(...)` 强制触发 Pinia 响应
- 学生端「作业详情/提交」页（`features/student/views/AssignmentSubmitView.vue`）
  - 面包屑与分块采用玻璃样式与统一间距
  - 教师附件区：显示教师上传附件列表与下载链接
  - 评分卡：已评分时展示动态分数字动画+进度条、等级、评分时间；“教师评语/优点/可改进之处”三块按块级排版
  - 容错导航：当路由 `:id` 非作业ID时，依次尝试按“成绩ID→提交ID”反查 `assignmentId` 并重定向到 `/student/assignments/:id/submit`
  - 未找到：提示 i18n 文案后跳回“我的作业”而非“成绩页”
- 通知详情「前往处理」跳转（`features/shared/views/NotificationDetailView.vue`）
  - 解析顺序：`data.assignmentId` →（若为成绩）用 `gradeId` 查成绩取 `assignmentId` → `submissionId` 反查 → 当且仅当 `relatedType=assignment` 才用 `relatedId`
  - 跳转：学生 → `/student/assignments/:id/submit`；教师 → `/teacher/assignments/:id/submissions`
- 时间管理（教师端）
  - 新增“定时发布”：表单支持 `draft/publish/scheduled` 三种模式
  - 使用 `GlassDateTimePicker` 替代原生日期时间；新增 +1 天、+7 天等快捷设置；所有弹层玻璃化
- 表单标准化
  - 新增 `GlassInput`、`GlassTextarea`、`GlassSearchInput` 并在教师/学生相关页面替换原生 `input/textarea`
- 玻璃下拉（`GlassPopoverSelect`）体验改进
  - 使用 fixed 定位并监听滚动父元素，实时重算位置；必要时追加临时“底部占位”以保证选项可见；特定场景使用 `teleport=false` 保持本地定位
- i18n
  - 新增/补齐：`student.assignments.detail.{attachmentsTitle,noAttachments,download,ungradedHint,notFoundTitle,notFoundMsg}`、`student.grades.{level,strengths,improvements}`、`student.assignments.status.late`
- 路由
  - 学生：新增 `/student/grades`；通知跳转至 `/student/assignments/:id/submit`
- 后端 Schema 与服务
  - 数据库：`assignments.status` 增加 `'scheduled'`；新增列 `publish_at timestamp NULL`；新增索引 `idx_publish_at`
  - 成绩发布：`GradeServiceImpl.publishGrade` 自动触发通知
  - 通知内容：`NotificationServiceImpl` 以 `relatedType='assignment'`、`relatedId=assignmentId` 发送成绩通知，并格式化内容仅含“作业名+分数”

### 本次（聊天/通知/玻璃样式统一）
- 聊天：最近会话改为服务端统一接口 `/api/chat/conversations/my`；学生端“最近”按 username 去重；联系人来源按角色区分（教师端优先 `/teachers/contacts`）。
- 通知：通知详情“前往处理”打开聊天抽屉，铃铛未读数兼容多键名；通知详情卡片玻璃化。
- UI：Emoji 选择弹窗与聊天抽屉内联表情选择器统一玻璃样式；个人档案页四块大卡片与内部两块小卡片（修改密码/邮箱验证）玻璃化。
- 详情见：`docs/frontend-deep-dive.md` 第 11 节。

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
- Issues: https://github.com/Xiaorui-Zhang537/student-core-competency-cultivation-assessment-system/issues
- Discussions: https://github.com/Xiaorui-Zhang537/student-core-competency-cultivation-assessment-system/discussions
- 邮箱: xiaorui537537@Gmail.com

> Made with ❤️ by Student Core Competency Cultivation Assessment System Development Team