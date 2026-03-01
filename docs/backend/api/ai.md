---
title: AI 聊天与会话 API（AI）
description: AI 聊天（含 SSE 流式）、会话管理、用户记忆
outline: [2, 3]
---

# AI 聊天与会话 API（AI）

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

本项目后端 `context-path=/api`，下文接口路径均以 `/api/...` 展示。

## 1. 端点总览

- `POST /api/ai/chat`：AI 聊天（非流式）
- `POST /api/ai/chat/stream`：AI 聊天（SSE 流式）
- `POST /api/ai/conversations`：新建会话
- `GET /api/ai/conversations`：会话列表
- `PUT /api/ai/conversations/{id}`：更新会话（标题/置顶/归档）
- `DELETE /api/ai/conversations/{id}`：删除会话
- `GET /api/ai/conversations/{id}/messages`：会话消息列表
- `GET /api/ai/memory`：获取用户记忆
- `PUT /api/ai/memory`：更新用户记忆

扩展能力文档：

- AI 批改（作文/文件）：见 [ai-grading.md](./ai-grading)
- AI 实时语音（Gemini Live WS）：见 [ai-live.md](./ai-live)
- AI 口语训练（会话/回合入库/回放上报）：见 [ai-voice-practice.md](./ai-voice-practice)

## 2. 通用约定

认证：所有 `/api/ai/**` 接口都需要登录（`Authorization: Bearer <access_jwt>`）。

响应封装：统一返回 `ApiResponse<T>`。

```json
{ "code": 200, "message": "Success", "data": {} }
```

## 3. 聊天：POST `/api/ai/chat`

权限：已登录用户（`isAuthenticated()`）

请求 Body：`AiChatRequest`

```json
{
  "messages": [{ "role": "user", "content": "请总结这段文本要点" }],
  "courseId": 2,
  "studentIds": [1001],
  "model": "google/gemini-2.5-pro",
  "provider": "google",
  "conversationId": 7777,
  "attachmentFileIds": [12345]
}
```

说明：

- `messages` 必填，且 `studentIds` 最多 5 个（超过会返回 400）。
- `conversationId` 可选：不传时后端会自动创建会话；传入时会校验会话归属。
- `attachmentFileIds` 可选：多模态附件文件 ID（先通过文件上传接口获得 `fileId`）。若模型不支持，后端会忽略附件。

响应：`ApiResponse<AiChatResponse>`

```json
{
  "code": 200,
  "data": {
    "answer": "这里是回答...",
    "conversationId": 7777,
    "messageId": 8888
  }
}
```

curl：

```bash
curl -X POST 'http://localhost:8080/api/ai/chat' \
  -H 'Authorization: Bearer <access_jwt>' \
  -H 'Content-Type: application/json' \
  -d '{"messages":[{"role":"user","content":"请总结这段文本要点"}],"model":"google/gemini-2.5-pro"}'
```

## 4. 聊天（SSE）：POST `/api/ai/chat/stream`

权限：已登录用户（`isAuthenticated()`）

请求 Body：同 `POST /api/ai/chat`

响应：`text/event-stream`（SSE）

事件约定（后端实现）：

- `event: ping` -> `{ ts }`（心跳）
- `event: meta` -> `{ conversationId, model }`
- `event: token` -> `{ text }`
- `event: done` -> `{ messageId, fullText }`
- `event: error` -> `{ message }`

curl（观察流）：

```bash
curl -N 'http://localhost:8080/api/ai/chat/stream' \
  -H 'Authorization: Bearer <access_jwt>' \
  -H 'Content-Type: application/json' \
  -d '{"messages":[{"role":"user","content":"给我一个学习计划"}],"model":"google/gemini-2.5-pro"}'
```

## 5. 学生端使用限额（配额）

配额由后端强制执行（按“成功落库的 assistant 消息条数”计次）。

- Gemini（`google/*`）：学生每周最多 10 次
- GLM（`glm-*`）：学生每周最多 20 次

若达到上限，会返回 403（提示“本周使用次数已达上限”）。

## 6. 会话管理

### POST `/api/ai/conversations` 新建会话

权限：已登录用户

请求 Body：

```json
{ "title": "班级分析", "model": "google/gemini-2.5-pro", "provider": "google" }
```

响应：`ApiResponse<AiConversation>`

### GET `/api/ai/conversations` 会话列表（分页）

权限：已登录用户

Query（均可选）：

- `q`：搜索关键字
- `pinned`：是否置顶
- `archived`：是否归档
- `page`
- `size`

响应：`ApiResponse<PageResult<AiConversation>>`

### PUT `/api/ai/conversations/{id}` 更新会话

权限：已登录用户

请求 Body：

```json
{ "title": "班级分析-期中", "pinned": true, "archived": false }
```

响应：成功返回 200，无 data。

### DELETE `/api/ai/conversations/{id}` 删除会话

权限：已登录用户

响应：成功返回 200，无 data。

### GET `/api/ai/conversations/{id}/messages` 会话消息列表

权限：已登录用户

Query（可选）：

- `page`
- `size`

响应：`ApiResponse<PageResult<AiMessage>>`

## 7. 用户记忆

### GET `/api/ai/memory` 获取记忆

权限：已登录用户

响应：`ApiResponse<AiMemory>`

### PUT `/api/ai/memory` 更新记忆

权限：已登录用户

请求 Body：

```json
{ "enabled": true, "content": "我的输出偏好：用中文，给出分点步骤。" }
```

响应：`ApiResponse<AiMemory>`

## 8. 常见错误与排查

- 400：参数非法。重点检查 `messages` 是否为空，`studentIds` 是否超过 5。
- 401：未登录或 token 过期。
- 403：会话不属于当前用户；或学生配额已用完。
- 5xx：上游模型限流/错误或服务异常（可切换模型或稍后重试）。

