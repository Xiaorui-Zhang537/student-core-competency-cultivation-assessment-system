# 前端 API：ai.api.ts / aiGrading.api.ts

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
const conv = await aiApi.createConversation({ title: '课程问答', model: 'deepseek/deepseek-chat-v3.1' })
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

---

## AI 批改封装（aiGrading.api.ts）

位于 `frontend/src/api/aiGrading.api.ts`，与后端 `/api/ai/grade/*` 一致：

```ts
type ChatRole = 'user' | 'assistant' | 'system'

export const aiGradingApi = {
  gradeEssay: (data: { messages: { role: ChatRole; content: string }[]; model?: string; jsonOnly?: boolean; useGradingPrompt?: boolean }) => {
    const payload = { jsonOnly: true, useGradingPrompt: true, ...data }
    return api.post('/ai/grade/essay', payload, { timeout: 120000 })
  },
  gradeEssayBatch: (data: { messages: { role: ChatRole; content: string }[]; model?: string; jsonOnly?: boolean; useGradingPrompt?: boolean }[]) => {
    const normalized = data.map(d => ({ jsonOnly: true, useGradingPrompt: true, ...d }))
    return api.post('/ai/grade/essay/batch', normalized, { timeout: 180000 })
  },
  gradeFiles: (data: { fileIds: number[]; model?: string; jsonOnly?: boolean; useGradingPrompt?: boolean }) => {
    const payload = { jsonOnly: true, useGradingPrompt: true, ...data }
    return api.post('/ai/grade/files', payload, { timeout: 180000 })
  },
  listHistory: (params?: { q?: string; page?: number; size?: number }) => api.get('/ai/grade/history', { params }),
  getHistoryDetail: (id: number | string) => api.get(`/ai/grade/history/${id}`),
  deleteHistory: (id: number | string) => api.delete(`/ai/grade/history/${id}`).catch(() => api.post(`/ai/grade/history/${id}/delete`))
}
```

### 使用示例

```ts
// 单篇文本批改
const { data } = await aiGradingApi.gradeEssay({
  messages: [{ role: 'user', content: essayText }],
  model: 'google/gemini-2.5-pro'
})

// 批量文本批改
const batch = await aiGradingApi.gradeEssayBatch([
  { messages: [{ role: 'user', content: essay1 }] },
  { messages: [{ role: 'user', content: essay2 }] }
])

// 按文件 ID 批改
const files = await aiGradingApi.gradeFiles({ fileIds: [1001, 1002], model: 'google/gemini-2.5-pro' })

// 历史分页
const history = await aiGradingApi.listHistory({ q: '', page: 1, size: 20 })

// 详情
const detail = await aiGradingApi.getHistoryDetail(101)

// 删除
await aiGradingApi.deleteHistory(101)
```
