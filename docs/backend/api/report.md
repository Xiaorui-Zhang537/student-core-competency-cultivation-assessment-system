---
title: 举报/报表 API（Report）
description: 举报提交、查询与管理员处理流程
outline: [2, 3]
---

# 举报/报表 API（Report）

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

本项目后端 `context-path=/api`，下文接口路径均以 `/api/...` 展示。

## 1. 端点总览

- POST `/api/reports`：提交举报（`TEACHER` / `ADMIN`）
- GET `/api/reports/{id}`：举报详情（`TEACHER` / `ADMIN`）
- GET `/api/reports`：分页列表（`ADMIN`）
- PUT `/api/reports/{id}/status`：更新举报状态（`ADMIN`，使用 query `status`）

## 2. POST `/api/reports` 提交举报

权限：`TEACHER` / `ADMIN`

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{
    "reportedStudentId": 1001,
    "courseId": 2001,
    "assignmentId": 3001,
    "submissionId": 4001,
    "reason": "Academic integrity concern",
    "details": "Suspected plagiarism",
    "evidenceFileId": 5678
  }' \
  http://localhost:8080/api/reports
```

响应：`ApiResponse<Report>`

## 3. GET `/api/reports/{id}` 查看详情

权限：`TEACHER` / `ADMIN`

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/reports/90001
```

响应：`ApiResponse<Report>`

## 4. GET `/api/reports` 分页列表（管理员）

权限：`ADMIN`

Query：

- `status`：可选
- `page`：默认 1
- `size`：默认 10

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/reports?status=PENDING&page=1&size=10"
```

响应：`ApiResponse<PageResult<Report>>`

## 5. PUT `/api/reports/{id}/status` 更新状态（管理员）

权限：`ADMIN`

Query：`status`（必填；取值以服务实现为准）

curl：

```bash
curl -X PUT -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/reports/90001/status?status=RESOLVED"
```

响应：成功返回 200（无 data）：

```json
{ "code": 200, "message": "Success", "data": null }
```

## 6. Troubleshooting

- 401：未认证
- 403：无权限（非教师/管理员创建/查看；非管理员列表与更新状态）
- 404：举报单不存在
