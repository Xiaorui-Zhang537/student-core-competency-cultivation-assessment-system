# 模型对照：社区与通知（Community & Notifications）

## 社区（Community）
- Post：`id/title/content/category/tags/authorId/createdAt/updatedAt/likeCount/commentCount`
- Comment：`id/postId/parentId/content/authorId/createdAt/likeCount`
- Stats：帖子数、评论数、活跃用户、热门话题等

## 通知（Notification）
- Notification：
  - `id/title/content/type(priority)/isRead/relatedType/relatedId/expiredAt/createdAt`
  - 扩展：`senderId/senderName/category/actionUrl/data`
- 聊天附件：
  - `chat_message_attachments` 通过 `notification_id + file_id` 关联消息与文件
  - 用途：聊天消息附件展示与文件权限校验（发送者/接收者）
- Stats：`totalCount/unreadCount/todayCount/typeDistribution`
- 前端当前直接使用批量发送体（`recipientIds/title/content/type/category/priority/...`）与管理员群发体（`targetType/role/targetIds/...`）

## 变更影响
- 社区：`community.api.ts` 与相关视图（列表/详情/评论）
- 通知：`notification.api.ts`、通知中心（未读角标、批量操作）
