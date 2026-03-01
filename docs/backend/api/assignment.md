---
title: 作业 API（Assignment）
description: 作业创建/发布/查询/提醒等接口（后端 /api/assignments）
---

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

本项目后端 `context-path=/api`，下文接口路径均以 `/api/...` 展示。

## 通用约定

认证：除少数公开接口外，均需 `Authorization: Bearer <access_jwt>`。

响应封装：统一返回 `ApiResponse<T>`。

时间格式：`yyyy-MM-dd HH:mm:ss`，时区 `Asia/Shanghai`。

## 数据结构：Assignment（核心字段）

```json
{
  "id": 88,
  "courseId": 2,
  "teacherId": 11,
  "lessonId": null,
  "assignmentType": "normal",
  "title": "HW1",
  "description": "完成课后习题 1-5",
  "requirements": "上传 PDF",
  "maxScore": 100,
  "dueDate": "2026-03-10 23:59:59",
  "allowLate": false,
  "maxAttempts": 1,
  "fileTypes": "[\"pdf\",\"docx\"]",
  "status": "draft",
  "submissionCount": 0,
  "createdAt": "2026-03-01 12:00:00",
  "updatedAt": "2026-03-01 12:00:00"
}
```

`assignmentType`：

- `normal`：普通作业，可设置 `dueDate`；禁止直接绑定 `lessonId`。
- `course_bound`：课程绑定-无截止，`dueDate` 必须为 `null`；可通过专用接口绑定/解绑 `lessonId`。

后端校验（创建/更新时）：

- `title/description/courseId/maxScore` 必填，`maxScore > 0`。
- `dueDate` 若不为空，不能早于当前时间。
- `course_bound` 类型不允许设置 `dueDate`。
- `normal` 类型不允许携带 `lessonId`。

## 列表与详情

### GET `/api/assignments` 分页查询作业

权限：无需特殊角色（未登录也可调用，但具体放行以安全配置为准）

Query（全部可选，`page/size` 有默认值）：

- `page`：默认 1
- `size`：默认 10
- `courseId`
- `teacherId`
- `status`：`draft|published|closed`
- `keyword`：标题/描述关键字（实现以服务端为准）

响应：`ApiResponse<PageResult<Assignment>>`

curl：

```bash
curl 'http://localhost:8080/api/assignments?page=1&size=10&courseId=2'
```

### GET `/api/assignments/{id}` 获取作业详情

权限：无需特殊角色（同上）

响应：`ApiResponse<Assignment>`

## 创建/更新/删除

### POST `/api/assignments` 创建作业

权限：`TEACHER` / `ADMIN`

说明：`teacherId` 会被后端强制设置为当前登录用户（前端可不传，但传了也会被覆盖）。

请求 Body（普通作业示例）：

```json
{
  "courseId": 2,
  "assignmentType": "normal",
  "title": "HW1",
  "description": "完成课后习题 1-5",
  "maxScore": 100,
  "dueDate": "2026-03-10 23:59:59",
  "allowLate": false,
  "maxAttempts": 1
}
```

请求 Body（课程绑定作业示例）：

```json
{
  "courseId": 2,
  "assignmentType": "course_bound",
  "title": "第3讲课堂练习",
  "description": "完成课堂练习并上传截图",
  "maxScore": 10
}
```

响应：`ApiResponse<Assignment>`

curl：

```bash
curl -X POST 'http://localhost:8080/api/assignments' \
  -H 'Authorization: Bearer <access_jwt>' \
  -H 'Content-Type: application/json' \
  -d '{"courseId":2,"assignmentType":"normal","title":"HW1","description":"完成课后习题 1-5","maxScore":100,"dueDate":"2026-03-10 23:59:59"}'
```

### PUT `/api/assignments/{id}` 更新作业

权限：`TEACHER` / `ADMIN`

说明：更新同样会把 `teacherId` 覆盖为当前登录用户；同时会执行与创建相同的字段校验。

响应：`ApiResponse<Assignment>`

### DELETE `/api/assignments/{id}` 删除作业

权限：`TEACHER` / `ADMIN`

说明：若该作业已有提交记录，后端会拒绝删除。

响应：成功返回 200，无 data。

## 绑定节次（仅 course_bound）

### PUT `/api/assignments/{id}/lesson` 绑定/解绑作业到节次

权限：`TEACHER` / `ADMIN`

请求 Body：

```json
{ "lessonId": 3001 }
```

解绑（将 lessonId 置空）：

```json
{ "lessonId": null }
```

约束：仅 `assignmentType=course_bound` 允许绑定；普通作业会返回 400。

响应：成功返回 200，无 data。

## 状态变更

### POST `/api/assignments/{id}/publish` 发布作业

权限：`TEACHER` / `ADMIN`

响应：成功返回 200，无 data。

### POST `/api/assignments/{id}/unpublish` 下架作业

权限：`TEACHER` / `ADMIN`

响应：成功返回 200，无 data。

### POST `/api/assignments/{id}/close` 关闭作业

权限：`TEACHER` / `ADMIN`

响应：成功返回 200，无 data。

### PUT `/api/assignments/batch-status` 批量更新状态

权限：`TEACHER` / `ADMIN`

请求 Body：

```json
{ "assignmentIds": [88, 89], "status": "published" }
```

响应：成功返回 200，无 data。

## 课程/我的作业/学生可见列表

### GET `/api/assignments/course/{courseId}` 获取课程下作业（非分页）

权限：无需特殊角色（同列表接口）

响应：`ApiResponse<Assignment[]>`

### GET `/api/assignments/my-assignments` 获取我的作业（教师）

权限：`TEACHER` / `ADMIN`

响应：`ApiResponse<Assignment[]>`

### GET `/api/assignments/for-student` 学生可做作业（简单列表）

权限：`STUDENT` / `ADMIN`

Query：`courseId`（可选）

响应：`ApiResponse<Assignment[]>`

### GET `/api/assignments/due-soon` 即将到期作业（简单列表）

权限：无需特殊角色（同列表接口）

Query：`days`（默认 7）

响应：`ApiResponse<Assignment[]>`

## 可提交性与统计

### GET `/api/assignments/{id}/can-submit` 当前作业是否可提交

权限：无需特殊角色（同列表接口）

说明：后端逻辑为 `status=published` 且满足：

- `assignmentType=course_bound`：视为无截止限制。
- 其他类型：`dueDate` 为空或未过期，或者 `allowLate=true`。

响应：

```json
{ "code": 200, "data": true }
```

### GET `/api/assignments/statistics` 作业统计

权限：`TEACHER` / `ADMIN`

Query：

- `teacherId`：可选。不传且当前为教师时，后端会默认统计“我自己”的作业。
- `courseId`：可选

响应：`ApiResponse<Map>`（示例字段：`totalAssignments/publishedAssignments/draftAssignments/totalSubmissions/details`）

### GET `/api/assignments/{id}/submission-stats` 提交统计

权限：`TEACHER` / `ADMIN`

响应：`ApiResponse<AssignmentSubmissionStatsResponse>`

### POST `/api/assignments/{id}/remind-unsubmitted` 提醒未提交学生

权限：`TEACHER` / `ADMIN`

请求 Body（可选）：

```json
{ "message": "请尽快提交作业，截止今晚 23:59" }
```

说明：若 `dueDate` 已过期，后端会拒绝发送提醒；`course_bound` 类型通常 `dueDate=null`，因此不受该限制。

响应（示例）：

```json
{ "code": 200, "data": { "sent": 10, "failed": 0 } }
```

curl：

```bash
curl -X POST 'http://localhost:8080/api/assignments/88/remind-unsubmitted' \
  -H 'Authorization: Bearer <access_jwt>' \
  -H 'Content-Type: application/json' \
  -d '{"message":"请尽快提交作业，截止今晚23:59"}'
```

## 常见错误与排查

- 400：字段校验失败。重点检查：`title/description/courseId/maxScore`、`dueDate` 格式与是否早于当前时间、`assignmentType` 与 `dueDate/lessonId` 的约束。
- 401：未登录或 token 过期。
- 403：非教师/管理员调用了写接口。
- 409：状态重复操作（例如已发布的作业再次发布）。
