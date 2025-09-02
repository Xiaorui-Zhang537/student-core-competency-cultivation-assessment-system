# 前端 API：ai.api.ts

## 方法签名
- `chat(data: { messages: { role:'user'|'assistant'|'system'; content: string }[]; courseId?; studentIds?; model? })`
- `createConversation(data)` / `listConversations(params)` / `updateConversation(id, data)` / `removeConversation(id)`
- `listMessages(id, params)`
- `getMemory()` / `updateMemory(data)`

## 注意事项
- 学生列表上限等校验由后端控制；必要时提示用户
- 可通过环境变量配置默认 provider/model（见后端 `application.yml`）

## 参数与返回
- `chat`：`{ messages: { role:'user'|'assistant'|'system'; content: string }[]; courseId?; studentIds?; model? }`
  - 返回：`{ answer: string }`
- 会话：创建/列表/更新/删除，消息分页：`{ page?: number; size?: number }`
- 记忆：`getMemory()` / `updateMemory({ enabled?: boolean; content?: string })`

## 示例
```ts
// 单轮对话
const { answer } = await aiApi.chat({ messages: [{ role: 'user', content: '你好' }] })

// 会话流
const conv = await aiApi.createConversation({ title: '课程问答', model: 'deepseek-r1' })
await aiApi.listMessages(conv.id, { page: 1, size: 50 })
await aiApi.updateConversation(conv.id, { pinned: true })
await aiApi.removeConversation(conv.id)

// 记忆开关
const mem = await aiApi.getMemory()
await aiApi.updateMemory({ enabled: true })
```

## 错误处理
- 400：消息格式或上下文长度超限
- 401/403：未登录或无权限
