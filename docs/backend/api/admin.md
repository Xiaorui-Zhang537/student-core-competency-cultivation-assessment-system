# 管理员 API（/api/admin）

本页描述管理员首期模块相关端点。所有接口均要求 JWT 且具备 `ADMIN` 角色（后端通过 `@PreAuthorize("hasRole('ADMIN')")` 保护）。

> 说明：后端 `server.servlet.context-path=/api`，因此下文路径均以 `/api` 为前缀。

## 1. 仪表盘

- `GET /api/admin/dashboard/overview`
  - **Query**：`days`（默认 7）
  - **返回**：用户/课程/社区/举报等全局计数与近 N 天活跃用户数（基于 `behavior_events`）。

- `GET /api/admin/dashboard/ability-radar-overview`
  - **Query**：`days`（默认 180，最大 365）
  - **返回**：全局五维雷达聚合（`dimensions[]`，含维度编码、名称、平均分、样本量）。

- `GET /api/admin/dashboard/ai-usage-overview`
  - **Query**：`days`（默认 30，最大 365）、`limit`（默认 20，最大 100）
  - **返回**：
    - `summary`：会话总数、消息总数、活跃用户数
    - `users[]`：按用户聚合的会话数、消息数、最后活跃时间（按消息数降序）

## 2. 课程总览

- `GET /api/admin/courses`
  - **Query**：`page`、`size`、`query`、`category`、`difficulty`、`status`、`teacherId`
  - **返回**：分页课程列表（复用课程发现查询，默认不包含已删除课程）。

- `GET /api/admin/courses/{id}`
  - **返回**：课程详情 + `activeEnrollments`（在读人数，来自 `enrollments.status='active'`）。

## 3. 用户管理（含创建管理员）

- `GET /api/admin/users`
  - **Query**：`page`、`size`、`keyword`、`role`、`status`、`includeDeleted`
  - **返回**：分页用户列表（不返回密码）。

- `POST /api/admin/users`
  - **Body**：`username/email/password/role/status/...`
  - **说明**：可创建 `admin` 角色账号（“管理员创建管理员”）。

- `PUT /api/admin/users/{id}/role`
  - **Body**：`{ "role": "student|teacher|admin" }`

- `PUT /api/admin/users/{id}/status`
  - **Body**：`{ "status": "active|disabled" }`
  - **联动**：登录/刷新令牌会拒绝 `disabled` 账号。

- `POST /api/admin/users/{id}/password/reset-email`
  - **Query**：`lang`（默认 `zh-CN`）
  - **说明**：复用“忘记密码”流程向用户邮箱发送重置邮件。

## 4. 学生/教师数据中心（详情聚合）

> 列表分页建议直接用 `/api/admin/users` 通过 `role=student|teacher` 过滤；详情端点用于展示聚合指标。

- `GET /api/admin/people/students/{id}`
  - **返回**：学生基础信息 + 选课数、平均成绩（`grades.percentage`）、能力报告数量、最后活跃时间（来自 `behavior_events`）。

- `GET /api/admin/people/teachers/{id}`
  - **返回**：教师基础信息 + 课程数、作业数、在读选课量。

## 5. 能力报告中心（跨学生检索）

- `GET /api/admin/ability-reports`
  - **Query**：`page/size/studentId/reportType/isPublished/courseId/assignmentId/submissionId/start/end`
  - **返回**：分页能力报告（包含 `studentName/studentNumber` 冗余字段）。

- `GET /api/admin/ability-reports/{id}`
  - **返回**：能力报告详情（包含学生冗余信息）。

## 6. 分析看板（序列数据）

- `GET /api/admin/analytics/series/overview`
  - **Query**：`days`（默认 30，最大 365）
  - **返回**：
    - `activeUsersDaily`：按天去重活跃用户数
    - `newUsersDaily`：按天新增用户数
    - `enrollmentsDaily`：按天选课数

## 7. 数据导出（CSV，同步）

- `GET /api/admin/exports/capabilities`
  - 返回支持格式/上限/可导出项列表（首期为 CSV，最大 50000 行）。

- `GET /api/admin/exports/users.csv`
- `GET /api/admin/exports/ability-reports.csv`

> 首期为同步导出并直接返回文件字节；后续可升级为“导出任务 + 文件存储 + 异步生成”。

## 8. 社区内容治理

- `GET /api/admin/community/posts`
  - **Query**：`page/size/keyword/category/status/pinned/includeDeleted`
  - **返回**：帖子分页列表（支持 `includeDeleted=true`）。

- `PUT /api/admin/community/posts/{id}`
  - **Body**：`status/pinned/allowComments/deleted`（按需传）
  - **说明**：管理员可越权下架/置顶/删除。

- `GET /api/admin/community/comments`
  - **Query**：`page/size/postId/keyword/status/includeDeleted`

- `PUT /api/admin/community/comments/{id}`
  - **Body**：`status/deleted`（按需传）

## 9. 审计日志（Admin Audit Logs）

首期对以下高风险动作写入 `admin_audit_logs`：

- 创建用户/管理员、变更用户角色、变更用户状态、发送重置密码邮件
- 帖子/评论治理（下架/置顶/删除等）
- CSV 导出下载
- 更新举报状态（`PUT /api/reports/{id}/status`）

表结构在 `backend/src/main/resources/schema.sql` 中维护。

