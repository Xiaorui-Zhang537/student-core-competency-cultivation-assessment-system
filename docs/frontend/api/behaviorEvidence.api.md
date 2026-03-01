---
title: "`behaviorEvidence.api.ts`"
description: 行为证据（阶段一摘要，不调用 AI）
outline: [2, 3]
---

# `behaviorEvidence.api.ts`（行为证据：阶段一摘要）

对应后端 `GET /api/behavior/summary`，返回纯代码聚合的行为摘要（不触发 AI）。

## 1. 方法

- `getSummary(params)`

## 2. 参数

- `studentId?`：学生可不填（默认自己）；教师/管理员通常必填（由后端校验）
- `courseId?`：可选（按课程过滤 + 全局事件并入）
- `range?`：默认 `7d`（当前仅支持 `7d`，按后端实现为准）

## 3. 返回

- `BehaviorSummaryResponse`（包含 `activityStats/evidenceItems/nonEvaluative/signals` 等字段；详见数据契约与后端响应）

## 4. 最小调用示例

```ts
import { behaviorEvidenceApi } from '@/api/behaviorEvidence.api'

// 学生：默认取自己
const mine = await behaviorEvidenceApi.getSummary({ range: '7d' })

// 教师：查看某学生（后端会做“课程交集”权限校验）
const one = await behaviorEvidenceApi.getSummary({ studentId: 123, courseId: 10, range: '7d' })
```

