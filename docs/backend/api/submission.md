---
title: 提交 API（Submission）
description: 学生提交/草稿、教师查看提交、导出 ZIP、学生查看成绩
outline: [2, 3]
---

# 提交 API（Submission）

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

本项目后端 `context-path=/api`，下文接口路径均以 `/api/...` 展示。

## 1. 通用约定

响应封装：除导出 ZIP 外，其余接口均返回 `ApiResponse<T>`。

角色权限（按接口注解）：

- 学生提交/草稿/查询本人提交：`STUDENT`
- 教师查看提交/导出：`TEACHER` / `ADMIN`
- 成绩详情：`STUDENT` / `TEACHER`

## 2. 数据结构：Submission（核心字段）

```json
{
  "id": 123,
  "assignmentId": 88,
  "studentId": 1001,
  "content": "My answer...",
  "submissionCount": 1,
  "status": "submitted",
  "submittedAt": "2026-03-01 12:00:00",
  "isLate": false
}
```

`status`：`submitted|graded|returned`

## 3. 教师/管理员：提交查询与导出

### GET `/api/submissions/{submissionId}` 获取提交详情

权限：`TEACHER` / `ADMIN`

响应：`ApiResponse<Submission>`

### GET `/api/assignments/{assignmentId}/submissions` 作业提交列表（分页）

权限：`TEACHER` / `ADMIN`

Query：`page`（默认 1）、`size`（默认 10）

响应：`ApiResponse<PageResult<Submission>>`

### GET `/api/submissions/{submissionId}/export` 导出提交（ZIP 二进制）

权限：`TEACHER` / `ADMIN`

返回：二进制 ZIP，响应头：

- `Content-Type: application/octet-stream`
- `Content-Disposition: attachment; filename="submission_{id}.zip"`

curl：

```bash
curl -L -OJ 'http://localhost:8080/api/submissions/123/export' \
  -H 'Authorization: Bearer <access_jwt>'
```

## 4. 学生：查询与提交

### GET `/api/assignments/{assignmentId}/submission` 我对该作业的提交

权限：`STUDENT`

响应：`ApiResponse<Submission>`

### POST `/api/assignments/{assignmentId}/submit` 提交作业（form 版本）

权限：`STUDENT`

Content-Type：`multipart/form-data` 或 `application/x-www-form-urlencoded`

参数：

- `content`：可选
- `file`：可选（单文件）

curl（带文件）：

```bash
curl -X POST 'http://localhost:8080/api/assignments/88/submit' \
  -H 'Authorization: Bearer <access_jwt>' \
  -F 'content=My answer' \
  -F 'file=@/path/to/answer.pdf'
```

响应：`ApiResponse<Submission>`

### POST `/api/assignments/{assignmentId}/submit` 提交作业（JSON 版本）

权限：`STUDENT`

Content-Type：`application/json`

请求 Body：`SubmissionRequest`

```json
{ "content": "My answer...", "fileIds": [5678, 5679] }
```

说明：`fileIds` 为已上传文件 ID 列表（先调用文件上传接口拿 `fileId`），后端会“取第一个作为主附件”。

curl：

```bash
curl -X POST 'http://localhost:8080/api/assignments/88/submit' \
  -H 'Authorization: Bearer <access_jwt>' \
  -H 'Content-Type: application/json' \
  -d '{"content":"My answer...","fileIds":[5678,5679]}'
```

响应：`ApiResponse<Submission>`

### POST `/api/assignments/{assignmentId}/draft` 保存草稿

权限：`STUDENT`

Content-Type：`application/x-www-form-urlencoded`

参数：

- `content`：必填（`@RequestParam String content`）

curl：

```bash
curl -X POST 'http://localhost:8080/api/assignments/88/draft' \
  -H 'Authorization: Bearer <access_jwt>' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'content=Draft...'
```

响应：`ApiResponse<Submission>`

## 5. 成绩相关（学生/教师）

### GET `/api/submissions/my/grades` 我的成绩列表（分页）

权限：`STUDENT`

Query：`page`（默认 1）、`size`（默认 10）

响应：`ApiResponse<PageResult<Map>>`（结构以服务端实现为准）

### GET `/api/submissions/{submissionId}/grade` 成绩详情（按提交）

权限：`STUDENT` / `TEACHER`

响应：`ApiResponse<Map>`（结构以服务端实现为准）

## 6. 常见错误与排查

- 400：参数非法（草稿缺少 content，或提交内容/附件不合规）。
- 401：未登录或 token 过期。
- 403：非学生提交，或非教师查看提交/导出。
- 404：作业/提交不存在。

