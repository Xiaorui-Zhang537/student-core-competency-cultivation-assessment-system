---
title: AI 口语训练 API（Voice Practice）
description: 语音会话、回合入库、回放时长上报（行为证据事实记录）
outline: [2, 3]
---

# AI 口语训练 API（Voice Practice）

本页覆盖 `/ai/voice/**` 与 `POST /ai/voice/turns`（口语回合入库）相关接口。

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

## 1. 设计说明（重要）

- 口语训练数据是独立记录：不写入 AI 聊天会话（`/ai/chat`）的消息流。
- 口语训练产生的行为事件用于“行为证据事实记录”：
  - **只记录事实，不评价，不算分**。
  - courseId 按约定为空（全局学习行为）。

## 2. 语音会话（Sessions）

### POST `/ai/voice/sessions`

创建语音会话。

- Body：`{ title?, model?, mode?, locale?, scenario? }`
- 返回：`{ sessionId, id }`（两者相同，兼容字段）

### GET `/ai/voice/sessions`

获取当前用户会话列表（支持分页/搜索）。

- Query：`q?`、`page?`、`size?`

### GET `/ai/voice/sessions/{sessionId}`

获取会话详情。

### PATCH `/ai/voice/sessions/{sessionId}`

更新会话（标题/置顶）。

- Body：`{ title?, pinned? }`

### DELETE `/ai/voice/sessions/{sessionId}`

删除会话（软删除），回合记录将不再展示。

## 3. 语音回合（Turns）

### GET `/ai/voice/sessions/{sessionId}/turns`

获取会话下回合列表（按时间正序）。

- Query：`page?`、`size?`

### POST `/ai/voice/turns`

口语训练回合入库（转写 + 音频附件）。

:::tip
音频文件需先通过文件上传接口获取 `fileId`（如 `/files/upload`），再把 `userAudioFileId/assistantAudioFileId` 绑定到回合中。
:::

- Body（按实现为准，常见字段）：
  - `sessionId`（必填）
  - `model?`
  - `userTranscript?`、`assistantText?`
  - `userAudioFileId?`、`assistantAudioFileId?`
  - `locale?`、`scenario?`

## 4. 行为上报：复听/回放时长（事实记录）

### POST `/ai/voice/replay`

按秒上报口语训练音频复听/回放的增量时长。

- 仅学生（`STUDENT`）会写入行为证据；非学生调用会返回 `recorded=false`。
- Body：
  - `audioRole`：`user|assistant`
  - `deltaSeconds`：1..1800（单次最多 30 分钟）
  - `sessionId?`、`turnId?`、`fileId?`、`messageId?`

返回：`{ recorded: true|false }`

