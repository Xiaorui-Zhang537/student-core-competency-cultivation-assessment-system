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

## 参数与返回
- 列表分页：`{ page?: number; size?: number; type?: string; isRead?: boolean; priority?: string }`
- 通知类型：`Notification`、统计：`NotificationStats`（见 `frontend/src/api/notification.api.ts` 顶部类型）
- 发送：`CreateNotificationRequest` 或批量发送体

## 示例
```ts
// 我的通知（未读优先）
const page = await notificationAPI.getMyNotifications({ page: 1, size: 20, isRead: false })

// 标记已读/全部已读/批量已读
await notificationAPI.markAsRead('901')
await notificationAPI.markAllAsRead()
await notificationAPI.batchMarkAsRead(['901','902'])

// 删除/批量删除
await notificationAPI.deleteNotification('901')
await notificationAPI.batchDeleteNotifications(['901','902'])

// 未读计数 + 统计
const { unreadCount } = await notificationAPI.getUnreadCount()
const stats = await notificationAPI.getNotificationStats()

// 会话读取
const conv = await notificationAPI.getConversation('2001', { page: 1, size: 50 })
await notificationAPI.readConversation('2001')

// 教师：作业/课程通知
await notificationAPI.sendAssignmentNotification('5001', { title: '作业提醒', content: '今晚截止', type: 'assignment' })
await notificationAPI.sendCourseNotification('1001', { title: '新公告', content: '周三调课', type: 'course' })
```

## 错误处理与权限
- 401：未登录
- 403：权限不足（发送/批量操作）
- 404：通知不存在
