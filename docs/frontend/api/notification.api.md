---
title: "`notification.api.ts`"
description: 通知中心与站内通知 API 封装
outline: [2, 3]
---

# 前端 API：notification.api.ts

## 方法签名（节选）
- `getMyNotifications(params)` / `getNotification(id)`
- `markAsRead(id)` / `markAllAsRead()`
- `deleteNotification(id)`
- `getUnreadCount()` / `getNotificationStats()`
- `batchSend(data)` / `adminBroadcast(data)`

## 注意事项
- 发送/批量发送通常需教师或管理员权限；会话操作基于 `peerId`
- 通知详情页点击“查看详情”会按 `relatedType` 做前端跳转；其中能力目标提醒（`goal_deadline` / `goal_overdue`）会直接跳到学生端 `/student/analysis`，并滚动到“能力目标”区块。

## 参数与返回
- 列表分页：`{ page?: number; size?: number; type?: string; isRead?: boolean; priority?: string }`
- 通知类型：`Notification`、统计：`NotificationStats`（见 `frontend/src/api/notification.api.ts` 顶部类型）
- 发送：当前前端直接使用批量发送体（`batchSend`）或管理员群发体（`adminBroadcast`）

## 示例
```ts
// 我的通知（未读优先）
const page = await notificationAPI.getMyNotifications({ page: 1, size: 20, isRead: false })

// 标记已读/全部已读
await notificationAPI.markAsRead('901')
await notificationAPI.markAllAsRead()

// 删除
await notificationAPI.deleteNotification('901')

// 未读计数 + 统计
const { unreadCount } = await notificationAPI.getUnreadCount()
const stats = await notificationAPI.getNotificationStats()

// 教师/管理员：批量通知
await notificationAPI.batchSend({
  recipientIds: ['1001', '1002'],
  title: '课程提醒',
  content: '今晚截止',
  type: 'message'
})
```

## 错误处理与权限
- 401：未登录
- 403：权限不足（发送/批量操作）
- 404：通知不存在
