---
title: 通知 API（Notification）
description: 通知中心（分页/已读/删除/统计）、教师发送、管理员群发、SSE 实时推送
outline: [2, 3]
---

# 通知 API（Notification）

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

本项目后端 `context-path=/api`，下文接口路径均以 `/api/...` 展示。

## 1. 通用约定

权限：除 SSE token 校验外，本域接口均要求已登录（`isAuthenticated()`），写接口另有角色限制。

响应封装：除 SSE 外，其它接口均返回 `ApiResponse<T>`。

## 2. 数据结构：Notification（核心字段）

```json
{
  "id": 10001,
  "recipientId": 20001,
  "senderId": 11,
  "type": "system",
  "category": "notice",
  "title": "课程更新",
  "content": "第3课新增练习",
  "relatedType": "course",
  "relatedId": 21001,
  "isRead": false,
  "readAt": null,
  "priority": "normal",
  "createdAt": "2026-03-01 12:00:00"
}
```

## 3. 我的通知与统计

### GET `/api/notifications/my` 我的通知列表（分页）

权限：已登录

Query：

- `type`：可选（如 `system|message|assignment|grade|course`）
- `page`：默认 1
- `size`：默认 20

响应：`ApiResponse<PageResult<Notification>>`

### GET `/api/notifications/{notificationId}` 通知详情

权限：已登录（后端会校验归属）

响应：`ApiResponse<Notification>`

### GET `/api/notifications/unread/count` 未读数量

权限：已登录

Query：`type` 可选

响应（示例）：

```json
{ "code": 200, "data": { "unreadCount": 5 } }
```

### GET `/api/notifications/stats` 通知统计

权限：已登录

响应：`ApiResponse<Map>`（结构以服务端实现为准）

## 4. 已读与删除

### PUT `/api/notifications/{notificationId}/read` 标记已读

权限：已登录

响应：成功返回 200，无 data。

### PUT `/api/notifications/batch/read` 批量已读

权限：已登录

请求 Body：`number[]`

```json
[10001, 10002, 10003]
```

响应：`ApiResponse<Map>`

### PUT `/api/notifications/all/read` 全部已读

权限：已登录

响应（示例）：

```json
{ "code": 200, "data": { "markedCount": 37 } }
```

### DELETE `/api/notifications/{notificationId}` 删除单条

权限：已登录（后端会校验归属）

响应：成功返回 200，无 data。

### DELETE `/api/notifications/batch` 批量删除

权限：已登录

请求 Body：`number[]`

响应：`ApiResponse<Map>`

## 5. 发送通知（教师/管理员）

### POST `/api/notifications/send` 发送单条通知

权限：`TEACHER`

请求 Body：`NotificationRequest`

```json
{
  "recipientId": 20001,
  "title": "作业提醒",
  "content": "今晚24:00截止",
  "type": "assignment",
  "category": "deadline",
  "priority": "high",
  "relatedType": "assignment",
  "relatedId": 31001
}
```

响应：`ApiResponse<Notification>`

### POST `/api/notifications/batch/send` 批量发送通知

权限：`TEACHER`

请求 Body：`BatchNotificationRequest`

```json
{
  "recipientIds": [20001, 20002],
  "title": "课程公告",
  "content": "明日调课",
  "type": "course",
  "category": "notice",
  "priority": "normal",
  "relatedType": "course",
  "relatedId": 21001
}
```

响应：`ApiResponse<Map>`

### POST `/api/notifications/admin/broadcast` 管理员群发

权限：`ADMIN`

请求 Body：`AdminBroadcastNotificationRequest`

```json
{
  "title": "系统维护",
  "content": "今晚 23:00-23:30 维护",
  "type": "system",
  "category": "system",
  "priority": "normal",
  "targetType": "all"
}
```

响应：`ApiResponse<Map>`

### POST `/api/notifications/assignment/{assignmentId}` 作业相关通知

权限：`TEACHER`

Query：

- `type`：必填
- `customMessage`：可选

响应：`ApiResponse<Map>`

### POST `/api/notifications/course/{courseId}` 课程相关通知

权限：`TEACHER`

Query：

- `type`：必填
- `customMessage`：可选

响应：`ApiResponse<Map>`

### POST `/api/notifications/grade/{gradeId}` 成绩相关通知

权限：`TEACHER`

Query：

- `type`：必填
- `customMessage`：可选

响应：`ApiResponse<Map>`

## 6. 聊天消息（兼容入口）

说明：聊天推荐走 `/api/chat/*`（见 [chat.md](./chat)）。`/api/notifications/message` 是“消息落库 + 触发通知”的兼容入口。

### POST `/api/notifications/message` 发送聊天消息

权限：已登录

请求 Body（示例）：

```json
{
  "recipientId": 20002,
  "content": "你好",
  "relatedType": "course",
  "relatedId": 101,
  "attachmentFileIds": [9876, 9999]
}
```

响应：`ApiResponse<Notification>`

## 7. 会话接口（旧版兼容）

说明：以下两个接口是历史兼容，建议迁移到 `/api/chat/*`：

### GET `/api/notifications/conversation`

Query：`peerId`（必填）、`page`（默认 1）、`size`（默认 20）、`courseId`（可选）

响应：`ApiResponse<PageResult<Notification>>`

### POST `/api/notifications/conversation/read`

Query：`peerId`（必填）、`courseId`（可选）

响应（示例）：

```json
{ "code": 200, "data": { "marked": 10 } }
```

## 8. 实时通知（SSE）

### GET `/api/notifications/stream`

权限：`isAuthenticated()`，且后端会额外校验 access token（支持 query token 或 Authorization header）。

请求示例：

```bash
curl -N 'http://localhost:8080/api/notifications/stream?token=<access_jwt>' \
  -H 'Accept: text/event-stream'
```

返回：`text/event-stream`（事件名与数据结构以服务端推送为准）。

## 9. 常见错误与排查

- 400：批量列表为空、type 不合法等。
- 401：未登录或 SSE token 不是合法 access token。
- 403：非教师调用发送接口；非管理员调用群发接口。
- 404：通知不存在或无权访问。

