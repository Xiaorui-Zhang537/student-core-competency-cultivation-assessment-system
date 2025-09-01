# 前端 API：notification.api.ts

## 方法签名（节选）
- `getMyNotifications(params)` / `getNotification(id)`
- `markAsRead(id)` / `batchMarkAsRead(ids)` / `markAllAsRead()`
- `deleteNotification(id)` / `batchDeleteNotifications(ids)`
- `getUnreadCount()` / `getNotificationStats()`
- `sendNotification(data)` / `batchSend(data)`
- `getConversation(peerId, params)` / `readConversation(peerId)`
- `sendAssignmentNotification(assignmentId, data)` / `sendCourseNotification(courseId, data)`

## 注意事项
- 发送/批量发送通常需教师或管理员权限；会话操作基于 `peerId`
