---
title: 章节（Chapter）API
description: 章节分组（大章）CRUD，仅教师/管理员
outline: [2, 3]
---

# 章节（Chapter）API

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

本项目后端 `context-path=/api`，下文接口路径均以 `/api/...` 展示。

## 1. 权限

本域接口均要求：`hasAnyRole('TEACHER','ADMIN')`。

## 2. 数据结构：Chapter

```json
{
  "id": 10,
  "courseId": 2,
  "title": "第一章 导论",
  "description": "课程概览",
  "orderIndex": 1,
  "createdAt": "2026-03-01 12:00:00"
}
```

## 3. 端点

### GET `/api/chapters/course/{courseId}` 课程章节列表

响应：`ApiResponse<Chapter[]>`

curl：

```bash
curl 'http://localhost:8080/api/chapters/course/2' \
  -H 'Authorization: Bearer <access_jwt>'
```

### POST `/api/chapters` 创建章节

请求 Body（示例）：

```json
{ "courseId": 2, "title": "第一章 导论", "description": "课程概览", "orderIndex": 1 }
```

响应：`ApiResponse<Chapter>`

### PUT `/api/chapters/{id}` 更新章节

请求 Body：`Chapter`（后端会用 path 里的 `{id}` 覆盖 body.id）

响应：成功返回 200，无 data。

### DELETE `/api/chapters/{id}` 删除章节

响应：成功返回 200，无 data。

## 4. 常见错误与排查

- 401：未登录或 token 过期。
- 403：非教师/管理员。
- 404：章节或课程不存在（以服务端实现为准）。

