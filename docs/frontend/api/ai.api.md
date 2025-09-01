# 前端 API：ai.api.ts

## 方法签名
- `chat(data: { messages: { role:'user'|'assistant'|'system'; content: string }[]; courseId?; studentIds?; model? })`
- `createConversation(data)` / `listConversations(params)` / `updateConversation(id, data)` / `removeConversation(id)`
- `listMessages(id, params)`
- `getMemory()` / `updateMemory(data)`

## 注意事项
- 学生列表上限等校验由后端控制；必要时提示用户
- 可通过环境变量配置默认 provider/model（见后端 `application.yml`）
