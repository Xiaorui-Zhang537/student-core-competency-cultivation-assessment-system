# 模型对照：AI 会话 / 消息 / 记忆

## 会话（Conversation）
- 字段（示例）：`id/userId/title/model/provider/pinned/archived/createdAt`

## 消息（Message）
- 字段（示例）：`id/conversationId/role(content)/fileIds/createdAt`
- 角色：`user/assistant/system`

## 记忆（Memory）
- 字段（示例）：`userId/enabled/content/updatedAt`

## 变更影响
- 控制器：`AiController`
- 服务：`AiService`、`AiConversationService`、`AiMemoryService`
- 前端：`ai.api.ts`、AI 助手视图与 Store
