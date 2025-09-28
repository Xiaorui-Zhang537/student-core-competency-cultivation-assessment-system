# Chat 会话 API

> 以 Swagger 为准：`/api/swagger-ui.html`

## 1. 获取我的最近会话
- `GET /api/chat/conversations/my`

Query 参数：
- `page`（默认1）
- `size`（默认20）
- `pinned`（可选）
- `archived`（可选）

响应字段：
- `peerId`：对端用户ID
- `peerUsername`：对端用户名
- `peerAvatar`：对端头像
- `courseId`：会话关联课程（学生侧可能为空）
- `lastContent`：最近一条消息内容
- `lastMessageAt`：最近一条消息时间
- `unread`：未读数

示例：
```json
{
  "code": 200,
  "data": {
    "items": [
      {"peerId": 9, "peerUsername": "teacherA", "peerAvatar": null, "courseId": 101, "lastContent": "收到", "lastMessageAt": "2025-09-06T12:00:00Z", "unread": 0}
    ],
    "page": 1,
    "size": 20,
    "total": 1,
    "pages": 1
  }
}
```

## 2. 标记会话为已读
- `PUT /api/chat/conversations/{id}/read`

说明：将该会话中“我为接收者”的消息全部置为已读，并返回剩余未读数。

响应：
```json
{ "code": 200, "data": { "marked": 5, "unread": 0 } }
```


## 3. 获取与某人的会话消息（按对端）
- `GET /api/chat/messages?peerId={id}&page=1&size=20`

说明：用于按对端用户ID分页拉取消息流（目前复用通知存储结构）。

响应（示例）：
```json
{ "code": 200, "data": { "items": [{"id":1,"content":"hi","createdAt":"..."}], "total": 12, "page": 1, "size": 20 } }
```

## 4. 发送聊天消息
- `POST /api/chat/messages`

请求体：
```json
{
  "recipientId": 20002,
  "content": "你好",
  "relatedType": "course",
  "relatedId": 101,
  "attachmentFileIds": [9876, 9999]
}
```
响应（示例）：
```json
{
  "code": 200,
  "data": {
    "id": 99901,
    "content": "你好",
    "createdAt": "..."
  }
}
```

## 5. 按对端标记会话为已读
- `PUT /api/chat/conversations/peer/{peerId}/read`

说明：将我与该对端的消息（我为接收者）全部置为已读。

响应（示例）：
```json
{ "code": 200, "data": { "marked": 5 } }
```

---

迁移说明：
- 历史上聊天消息通过 `/api/notifications/conversation` 与 `/api/notifications/message` 实现；现已拆分到 `/api/chat/*`。
- 前端应优先调用 `/api/chat/messages` 与 `/api/chat/conversations/peer/{peerId}/read`。
- 后端当前仍复用 `NotificationService` 的数据访问，后续可逐步将消息持久层迁移为独立的 `messages`/`message_attachments` 表。

---

附件说明：
- 前端上传文件使用文件 API：`POST /api/files/upload`，得到 `fileId`；将 `fileId` 放入 `attachmentFileIds` 随消息发送。
- 后端将附件引用写入 `chat_message_attachments` 表；查询会话时会返回每条消息的 `attachmentFileIds`（数组）。
- 图片可走 `GET /api/files/{fileId}/preview` 直接渲染；其他文件走 `GET /api/files/{fileId}/download` 下载。


