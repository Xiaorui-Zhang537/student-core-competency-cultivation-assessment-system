---
title: 聊天会话 API（Chat）
description: 最近会话、消息拉取与发送、已读/归档、未读统计
outline: [2, 3]
---

# 聊天会话 API（Chat）

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

本项目后端 `context-path=/api`，下文接口路径均以 `/api/...` 展示。

## 1. 通用约定

权限：本域接口全部要求已登录（`isAuthenticated()`）。

存储说明：聊天当前复用 `Notification` 作为消息载体（`type=message`），会话/已读等通过服务层聚合实现。

附件说明：

- 先上传文件：`POST /api/files/upload` 拿到 `fileId`
- 发送消息时带上 `attachmentFileIds`
- 预览/下载附件走文件接口（需要带 Token）

## 2. 最近会话

### GET `/api/chat/conversations/my`

Query：

- `page`：默认 1
- `size`：默认 20
- `pinned`：可选
- `archived`：可选

响应：`ApiResponse<PageResult<Map>>`（字段以服务端实现为准，常见字段包括 `peerId/peerUsername/peerAvatar/courseId/lastContent/lastMessageAt/unread`）

curl：

```bash
curl 'http://localhost:8080/api/chat/conversations/my?page=1&size=20' \
  -H 'Authorization: Bearer <access_jwt>'
```

## 3. 消息列表与发送

### GET `/api/chat/messages` 获取与某人的会话消息

Query：

- `peerId`：必填
- `courseId`：可选（课程上下文）
- `page`：默认 1
- `size`：默认 20

响应：`ApiResponse<PageResult<Notification>>`

curl：

```bash
curl 'http://localhost:8080/api/chat/messages?peerId=20002&page=1&size=20' \
  -H 'Authorization: Bearer <access_jwt>'
```

### POST `/api/chat/messages` 发送聊天消息

请求 Body（复用 `NotificationRequest` 的部分字段）：

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

curl：

```bash
curl -X POST 'http://localhost:8080/api/chat/messages' \
  -H 'Authorization: Bearer <access_jwt>' \
  -H 'Content-Type: application/json' \
  -d '{"recipientId":20002,"content":"你好","relatedType":"course","relatedId":101,"attachmentFileIds":[9876]}'
```

## 4. 已读管理

### PUT `/api/chat/conversations/{id}/read` 按会话ID标记已读

响应：`ApiResponse<Map>`（示例字段：`marked/unread`）

### PUT `/api/chat/conversations/peer/{peerId}/read` 按对端标记已读

Query：`courseId` 可选

响应（示例）：

```json
{ "code": 200, "data": { "marked": 5 } }
```

## 5. 归档与未读统计

### PUT `/api/chat/conversations/{id}/archive` 归档/取消归档

Query：`archived=true|false`

响应（示例）：

```json
{ "code": 200, "data": { "updated": 1 } }
```

### GET `/api/chat/unread/count` 聊天未读总数

响应（示例）：

```json
{ "code": 200, "data": { "unreadCount": 12 } }
```

## 6. 兼容接口

历史上聊天通过通知域接口提供：

- `GET /api/notifications/conversation`
- `POST /api/notifications/message`
- `POST /api/notifications/conversation/read`

新前端建议优先使用本页 `/api/chat/*`。

## 7. 常见错误与排查

- 401：未登录或 token 过期。
- 403：课程上下文（`relatedType=course`）不匹配，或越权访问对话。
- 附件预览失败：检查是否用带 token 的请求访问 `/api/files/{id}/preview|download|stream`。

