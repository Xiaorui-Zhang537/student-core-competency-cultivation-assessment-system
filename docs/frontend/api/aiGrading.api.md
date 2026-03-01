---
title: "`aiGrading.api.ts`"
description: AI 批改封装（作文/文件批改与历史）
outline: [2, 3]
---

# `aiGrading.api.ts`（AI 批改）

对应后端 `/api/ai/grade/*`，用于教师端批改与历史记录查询。

## 1. 方法清单

- `gradeEssay(data)`
- `gradeEssayBatch(data[])`
- `gradeFiles(data)`
- `listHistory(params?)`
- `getHistoryDetail(id)`
- `deleteHistory(id)`（优先 DELETE，失败则回退 POST 兼容路径）

## 2. 参数与返回（摘要）

### `gradeEssay`

- 入参：`{ messages: { role: 'user'|'assistant'|'system'; content: string }[]; model?; jsonOnly?; useGradingPrompt? }`
- 说明：前端默认会强制 `jsonOnly=true`、`useGradingPrompt=true`，并启用后端稳定化采样参数（`samples/diffThreshold`）。

### `gradeFiles`

- 入参：`{ fileIds: number[]; model?; jsonOnly?; useGradingPrompt? }`
- 说明：`fileIds` 来自文件上传/关联文件列表。

### 历史

- `listHistory({ q?, page?, size? })`
- `getHistoryDetail(id)`
- `deleteHistory(id)`

## 3. 最小调用示例

```ts
import { aiGradingApi } from '@/api/aiGrading.api'

// 1) 文本批改
const graded = await aiGradingApi.gradeEssay({
  messages: [{ role: 'user', content: essayText }],
  model: 'google/gemini-2.5-pro'
})

// 2) 按文件批改
const byFiles = await aiGradingApi.gradeFiles({ fileIds: [1001, 1002] })

// 3) 历史
const history = await aiGradingApi.listHistory({ page: 1, size: 20 })
const detail = await aiGradingApi.getHistoryDetail(history.items[0].id)
await aiGradingApi.deleteHistory(detail.id)
```

## 4. 注意事项

- 超时：批改接口超时较长（120s~180s），属于正常配置；前端不要用短超时覆盖。
- 批改输出：默认按结构化 JSON 使用；若需要原始文本，需明确传 `jsonOnly=false`（并保证 UI 兜底）。

