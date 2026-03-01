---
title: "`behaviorInsight.api.ts`"
description: 行为洞察（阶段二 AI 解读，不算分）
outline: [2, 3]
---

# `behaviorInsight.api.ts`（行为洞察：阶段二）

对应后端：

- `GET /api/behavior/insights`：获取洞察结果（复用/最新）
- `POST /api/behavior/insights/generate`：按需生成洞察（后端治理规则会限制重跑与配额）

## 1. 方法

- `getLatest(params)`
- `generate(params)`

## 2. 参数

通用参数：

- `studentId`（必填）
- `courseId?`、`range?`（默认 `7d`，按后端实现为准）

生成参数（仅 `generate`）：

- `model?`
- `force?`（学生端通常不可用，后端会限制）

## 3. 返回

- `BehaviorInsightResponse`
  - `schemaVersion`：建议校验（前端可按版本做兼容）
  - `riskAlerts[]` / `actionRecommendations[]`：结构化预警与行动建议（用于 UI 直接渲染）
  - `meta.status`：`success|failed|partial`

## 4. 最小调用示例

```ts
import { behaviorInsightApi } from '@/api/behaviorInsight.api'

// 获取最新（或复用已生成）
const latest = await behaviorInsightApi.getLatest({ studentId: 123, courseId: 10, range: '7d' })

// 触发生成（后端会做配额与复用策略）
const generated = await behaviorInsightApi.generate({ studentId: 123, courseId: 10, range: '7d' })
```

