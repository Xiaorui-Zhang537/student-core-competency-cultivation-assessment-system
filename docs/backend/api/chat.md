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


