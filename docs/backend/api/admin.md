---
title: 管理员 API（Admin）
description: 管理员仪表盘、用户/课程/社区治理、导出、AI 与行为洞察审计
outline: [2, 3]
---

# 管理员 API（Admin）

本页覆盖 `controller/admin/*` 下的管理员端点（`/api/admin/**`）。所有接口均要求 JWT 且具备 `ADMIN` 角色（控制器层 `@PreAuthorize("hasRole('ADMIN')")`）。

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

本项目后端 `context-path=/api`，下文接口路径均以 `/api/...` 展示。

## 1. 通用约定

- 认证：`Authorization: Bearer <token>`
- 响应：统一返回 `ApiResponse<T>`（`code/message/data`）
- 导出：`*.csv`/`*.zip` 接口返回二进制文件（`Content-Disposition: attachment`），用 `curl -o` 保存

## 2. 仪表盘（Dashboard）

### GET `/api/admin/dashboard/overview` 仪表盘总览

Query：`days`（默认 7）

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/admin/dashboard/overview?days=7"
```

### GET `/api/admin/dashboard/ability-radar-overview` 五维雷达总览

Query：`days`（默认 180）

### GET `/api/admin/dashboard/ai-usage-overview` AI 使用总览

Query：`days`（默认 30）、`limit`（默认 20）

## 3. 用户管理（Users）

### GET `/api/admin/users` 分页查询用户

Query：

- `page`：默认 1
- `size`：默认 20
- `keyword`：可选
- `role`：可选
- `status`：可选
- `includeDeleted`：默认 `false`

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/admin/users?page=1&size=20&role=student"
```

### POST `/api/admin/users` 创建用户（可创建管理员）

Body：`AdminUserCreateRequest`（示例）：

```json
{ "username": "admin2", "email": "admin2@example.com", "password": "P@ssw0rd!", "role": "admin", "status": "active" }
```

```bash
curl -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"username":"admin2","email":"admin2@example.com","password":"P@ssw0rd!","role":"admin","status":"active"}' \
  http://localhost:8080/api/admin/users
```

### PUT `/api/admin/users/{id}/role` 更新用户角色

Body：`{ "role": "student|teacher|admin" }`

```bash
curl -X PUT -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"role":"teacher"}' \
  http://localhost:8080/api/admin/users/1001/role
```

### PUT `/api/admin/users/{id}/status` 更新用户状态（active/disabled）

Body：`{ "status": "active|disabled" }`

```bash
curl -X PUT -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"status":"disabled"}' \
  http://localhost:8080/api/admin/users/1001/status
```

### POST `/api/admin/users/{id}/password/reset-email` 发送密码重置邮件

Query：`lang`（默认 `zh-CN`）

```bash
curl -X POST -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/admin/users/1001/password/reset-email?lang=zh-CN"
```

## 4. 课程总览（Courses）

### GET `/api/admin/courses` 分页查询课程（全量）

Query：

- `page`：默认 1
- `size`：默认 20
- `query`：可选关键词
- `category`、`difficulty`、`status`、`teacherId`：可选过滤

### GET `/api/admin/courses/{id}` 课程详情（含在读人数）

响应：`ApiResponse<AdminCourseDetailResponse>`

### GET `/api/admin/courses/{id}/students` 管理员获取课程学生列表（分页）

Query：`page,size,search,sortBy,activity,grade,progress`

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/admin/courses/2/students?page=1&size=20&sortBy=joinDate"
```

### GET `/api/admin/courses/{courseId}/lesson-notes` 分页查看课程课堂笔记（只读）

Query：

- `page`：默认 1
- `size`：默认 20（最大 200）
- `studentId` / `lessonId`：可选
- `q`：可选关键词

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/admin/courses/2/lesson-notes?page=1&size=20&q=递归"
```

## 5. 人员数据中心（People）

说明：列表分页建议直接用 `GET /api/admin/users` 通过 `role=student|teacher` 过滤；详情端点用于展示聚合指标。

### GET `/api/admin/people/students/{id}` 学生详情（聚合）

Query：

- `courseId`：可选（按课程范围聚合）
- `eventLimit`：默认 8

### GET `/api/admin/people/teachers/{id}` 教师详情（聚合）

## 6. 能力报告中心（Ability Reports）

### GET `/api/admin/ability-reports` 分页检索能力报告

Query：

- `page`：默认 1
- `size`：默认 20
- `search`、`studentId`、`reportType`、`isPublished`、`courseId`、`assignmentId`、`submissionId`：可选
- `start` / `end`：可选，格式 `yyyy-MM-dd HH:mm:ss`

### GET `/api/admin/ability-reports/{id}` 能力报告详情

## 7. 能力雷达（管理员版）

### GET `/api/admin/ability/radar` 管理员获取学生五维能力雷达

Query：

- `studentId`（必填）
- `courseId`（必填）
- `classId`、`startDate`、`endDate`（可选，日期格式 `YYYY-MM-DD`）

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/admin/ability/radar?studentId=1001&courseId=2"
```

### POST `/api/admin/ability/radar/compare` 管理员获取学生五维能力对比（A/B）

入参：Body 复用 `AbilityCompareQuery`；`studentId` 可放在 body，或放在 query（body 未提供时使用 query）。

```bash
curl -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"courseId":2,"studentId":1001,"startDateA":"2026-03-01","endDateA":"2026-03-31","startDateB":"2026-04-01","endDateB":"2026-04-30"}' \
  "http://localhost:8080/api/admin/ability/radar/compare"
```

## 8. 分析看板（Analytics）

### GET `/api/admin/analytics/series/overview` 获取分析序列（活跃/新增/选课）

Query：`days`（默认 30）

## 9. 社区内容治理（Community Moderation）

### GET `/api/admin/community/posts` 分页查询帖子（治理视角）

Query：`page,size,keyword,category,status,pinned,includeDeleted`

### PUT `/api/admin/community/posts/{id}` 治理帖子（状态/置顶/禁评/删除）

Body：`AdminPostModerationRequest`（字段按需传）：

```json
{ "status": "published", "pinned": false, "allowComments": true, "deleted": false }
```

### GET `/api/admin/community/comments` 分页查询评论（治理视角）

Query：`page,size,postId,keyword,status,includeDeleted`

### PUT `/api/admin/community/comments/{id}` 治理评论（状态/删除）

Body：`AdminCommentModerationRequest`（字段按需传）：

```json
{ "status": "published", "deleted": false }
```

## 10. 导出（Exports）

导出接口均在 `/api/admin/exports/*`，多数导出上限为 50000 行。建议用浏览器或 `curl -L -o` 方式下载。

### GET `/api/admin/exports/capabilities` 查询支持的导出能力

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/admin/exports/capabilities
```

### GET `/api/admin/exports/users.csv` 导出用户列表（CSV）

Query：`keyword,role,status,includeDeleted`

```bash
curl -L -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/admin/exports/users.csv?role=student" \
  -o users.csv
```

### GET `/api/admin/exports/community.csv` 导出社区帖子及评论（CSV）

```bash
curl -L -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/admin/exports/community.csv \
  -o community_posts_comments.csv
```

### GET `/api/admin/exports/ability-reports.csv` 导出能力报告列表（CSV）

Query：`studentId,reportType,isPublished,courseId,assignmentId,submissionId,start,end`

### GET `/api/admin/exports/course-students.csv` 导出课程学生名单（CSV）

Query：`courseId`（必填）以及 `search,sortBy,activity,grade,progress`

### GET `/api/admin/exports/ai-conversations.csv` 导出 AI 会话索引（CSV）

Query：`studentId`（必填）以及 `q,pinned,archived`

### GET `/api/admin/exports/voice-sessions.csv` 导出口语训练会话索引（CSV）

Query：`studentId`（必填）以及 `q`

### GET `/api/admin/exports/course-lesson-notes.csv` 导出课程课堂笔记（CSV）

Query：`courseId`（必填）以及 `studentId,lessonId,q`

### GET `/api/admin/exports/course-lesson-notes.zip` 导出课程课堂笔记（ZIP）

Query：同 `/course-lesson-notes.csv`

### GET `/api/admin/exports/course-student-data.zip` 导出单学生课程全量数据（ZIP）

Query：`courseId`、`studentId`（均必填）

### GET `/api/admin/exports/course-data.zip` 导出课程全量数据（ZIP）

Query：`courseId`（必填）

## 11. AI 问答审计（Admin AI Audit）

### GET `/api/admin/ai/conversations` 管理员按学生查询 AI 会话列表（分页）

Query：`studentId`（必填）以及 `q,pinned,archived,page,size`

### GET `/api/admin/ai/conversations/{id}` 管理员获取 AI 会话详情

Query：`studentId`（必填）

### GET `/api/admin/ai/conversations/{id}/messages` 管理员查询会话消息（分页）

Query：`studentId`（必填）以及 `page,size`

## 12. 口语训练审计（Admin Voice Audit）

### GET `/api/admin/ai/voice/sessions` 管理员按学生查询语音会话列表

Query：`studentId`（必填）以及 `q,page,size`

### GET `/api/admin/ai/voice/sessions/{sessionId}` 管理员获取语音会话详情

Query：`studentId`（必填）

### GET `/api/admin/ai/voice/sessions/{sessionId}/turns` 管理员获取语音回合列表

Query：`studentId`（必填）以及 `page,size`

## 13. 行为洞察（Admin Behavior Insights）

### POST `/api/admin/behavior/insights/generate` 管理员生成行为洞察

Query：`studentId`（必填）以及 `courseId,range,model,force`

### GET `/api/admin/behavior/insights` 管理员获取最新行为洞察

Query：`studentId`（必填）以及 `courseId,range`

### GET `/api/admin/behavior/insights/history` 管理员分页查询行为洞察历史

Query：`studentId`（必填）以及 `courseId,range,page,size`

### GET `/api/admin/behavior/insights/history/{id}` 管理员查看行为洞察历史详情

说明：若记录不存在，会返回 `data=null`（仍为 200）。

### 补充：`/api/admin/behavior/summary`

前端 `admin.api.ts` 中有 `GET /api/admin/behavior/summary` 的调用封装，但当前后端未提供对应控制器；管理员侧如需查看摘要，可直接调用公共接口 `GET /api/behavior/summary?studentId=...`（见 [行为证据评价](/backend/api/behavior)）。

## 14. 审计日志（Admin Audit Logs）

管理员端对高风险动作会写入 `admin_audit_logs`（例如：创建/变更用户、社区治理、导出、举报状态更新、AI/行为洞察审计访问等）。表结构在 `backend/src/main/resources/schema.sql` 中维护。
