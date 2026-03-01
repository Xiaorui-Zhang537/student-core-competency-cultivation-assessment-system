---
title: "`chat.api.ts`"
description: 站内私信会话与消息
outline: [2, 3]
---

# `chat.api.ts`（站内私信）

对应后端 `/api/chat/**`：会话列表、消息分页、已读、归档。

## 1. 数据结构（摘要）

- `ChatConversationItem`：会话条目（对端信息 + 未读数 + 最近消息摘要）
- `Paged<T>`：通用分页结构（`items/page/size/total/totalPages`）

## 2. 方法清单

- `getMyConversations(params?)`
- `markConversationRead(conversationId)`
- `getMessages(peerId, params?)`
- `sendMessage(data)`
- `markReadByPeer(peerId, courseId?)`
- `archiveConversation(conversationId, archived)`

## 3. 最小调用示例

```ts
import { chatApi } from '@/api/chat.api'

// 会话列表
const list = await chatApi.getMyConversations({ page: 1, size: 50, archived: false })

// 拉取与某人的消息
const msgs = await chatApi.getMessages(20001, { page: 1, size: 50 })

// 发送消息
await chatApi.sendMessage({ recipientId: 20001, content: '你好' })

// 已读与归档
await chatApi.markReadByPeer(20001)
await chatApi.archiveConversation(list.items[0].id, true)
```

## 4. 注意事项

- `courseId`：用于把对话限制在某课程上下文（如后端支持），未传则表示全局会话。
- 附件：`attachmentFileIds` 传 fileId 列表，音频/图片等需先走上传接口。

