# 行为证据评价（两阶段）

本模块用于记录与聚合学生行为事件，形成“可解释的行为证据”，但**不参与任何分数计算**。

## 设计原则

- **唯一数值分数仍为作业成绩**：行为数据不参与加权、均分、总评、排名。
- **两阶段**：
  - 阶段一（不调用 AI）：纯代码把事件聚合成结构化摘要 JSON。
  - 阶段二（阶段性调用 AI）：只对摘要做解释/总结/建议，不计算分数。

## 阶段一：行为摘要（不调用 AI）

### GET `/behavior/summary`

**权限**：已登录

- 学生：仅可查看自己
- 教师：可查看与自己存在课程交集的学生

**Query**

- `studentId`（教师必填；学生可不填）
- `courseId`（可选）
- `range`（可选，默认 `7d`，当前仅支持 `7d`）

**返回**

- `BehaviorSummaryResponse`（`schemaVersion=behavior_summary.v1`）
- 包含 `activityStats`、`evidenceItems`、`nonEvaluative`、`signals`

**快照与刷新（重要）**

- 阶段一摘要会优先复用最近一次摘要快照（降低重复计算与数据库压力）
- 若检测到快照生成后出现了新行为事件（如 AI 提问/作业提交/社区发问），将自动重新聚合并写入新快照，确保计数及时更新

**courseId 过滤说明（重要）**

- 当指定 `courseId` 查询摘要时，系统会返回：
  - `courseId` 对应课程内的行为事件
  - 以及**无法归属具体课程的全局事件**（`course_id` 为空，如：社区发帖/评论、未带课程上下文的 AI 提问）

## 阶段二：行为洞察（AI 解读，不算分）

### POST `/behavior/insights/generate`

**权限**：已登录

**Query**

- `studentId`（必填）
- `courseId`（可选）
- `range`（可选，默认 `7d`，当前仅支持 `7d`）
- `model`（可选，默认 `google/gemini-2.5-pro`）
- `force`（可选，默认 `false`；仅教师/管理员可用，学生禁止）

**治理**

- evidenceItems 为空时：**不调用 AI**，直接返回 `partial`（证据不足）
- **学生自助触发（测试）**：每日最多触发 7 次（仅统计 status!=partial 的次数；证据不足不计入）
- **教师/管理员**：7 天内对同一快照重复生成：优先复用已生成的 `success` 结果；`force=true` 可重跑

### GET `/behavior/insights`

**权限**：已登录

- 学生：仅可查看自己
- 教师：可查看与自己存在课程交集的学生

**Query**

- `studentId`（必填）
- `courseId`（可选）
- `range`（可选，默认 `7d`，当前仅支持 `7d`）

**返回**

- `BehaviorInsightResponse`（`schemaVersion=behavior_insight.v2`，包含结构化 `riskAlerts`/`actionRecommendations`）

