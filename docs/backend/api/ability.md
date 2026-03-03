---
title: 能力评估 API（Ability）
description: 学生端雷达/趋势/报告与教师端能力评估写入与统计
outline: [2, 3]
---

# 能力评估 API（Ability）

本页覆盖 `/ability/**` 相关接口（能力维度、学生端能力看板/雷达/报告、教师端评估写入与统计）。

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

## 1. 权限与角色

- 学生端接口：`hasRole('STUDENT')`
- 教师端接口：`hasRole('TEACHER')`
- 通用接口（如维度列表）：无需特定角色（但通常需要登录态，按实际安全配置为准）

## 2. 维度与基础数据

### GET `/ability/dimensions`

获取系统定义的能力维度列表（包含维度编码、名称等）。

## 3. 学生端（STUDENT）

### GET `/ability/student/dashboard`

学生能力仪表板（概览类指标，具体字段按实现/版本可能变化）。

### GET `/ability/student/trends`

- Query：
  - `dimensionId`（可选）
  - `timeRange`（月数，默认 `6`）

说明：
- 当前按 `ability_assessments.assessed_at` 聚合月度均分。
- 返回项会同时带 `month/averageScore` 与前端图表可直接使用的 `x/y` 字段。
- `dimensionId` 为空时聚合全部维度；有值时会校验维度是否存在且启用。

### GET `/ability/student/radar`

学生五维雷达（学生 vs 班级/课程均值）。

- Query：
  - `courseId`（必填）
  - `classId`（可选）
  - `startDate` / `endDate`（可选；缺失则按最近 30 天）

### POST `/ability/student/radar/compare`

学生五维能力对比（A/B 区间或作业集）。

:::tip
该接口用于“对比分析”类场景：例如对比两个时间窗、或对比某些作业集合对应的能力雷达差异。
:::

### POST `/ability/student/dimension-insights`

学生维度解析/文字分析：在 A/B 条件下生成每个维度的对比分析与建议（未提供 B 则与自身 A 对比）。

### GET `/ability/student/recommendations`

- Query：
  - `dimensionId`（可选）
  - `limit`（默认 `10`）

### 学习建议的状态操作

- POST `/ability/student/recommendations/{recommendationId}/read`
- POST `/ability/student/recommendations/{recommendationId}/accept`

说明：
- 学生首次读取建议列表时，如果当前维度下还没有可用建议，服务端会基于 `student_abilities` 的最新快照自动生成基础建议并写入 `learning_recommendations`。
- 已读 / 采纳接口会校验建议归属，只允许学生操作自己的建议记录。

### 能力目标（Goals）

- GET `/ability/student/goals`
- POST `/ability/student/goals`
- PUT `/ability/student/goals/{goalId}`
- DELETE `/ability/student/goals/{goalId}`

说明：
- `targetScore` / `currentScore` 与四个非成绩维度保持同口径，按 `0-5` 存储。
- 为兼容历史输入，服务端会将 `0-100` 分值自动换算为 `0-5` 后落库。
- 达到目标分数后会自动将 `status` 标记为 `achieved`，并写入行为事件 `goal_achieve`。
- 截止日期已过但未达成时，不会强制改写数据库 `status`；接口会以派生字段 `overdue=true` 返回，前端与行为摘要按“已逾期”展示。
- 创建/编辑目标使用统一日期控件，提交给后端的日期格式仍是 `yyyy-MM-dd`。
- 系统会在每天早上预同步一次“目标即将到期 / 目标已逾期”提醒；通知中心在拉取通知列表/未读数/统计时还会再做一次按需同步，作为兜底。
- 行为摘要会附带 `goalCompletionRate`、`overdueRecoveryCount`、`goalLinkedScoreDelta` 等派生信号；`signals.goalSignals` 还会给出 `focusSummary`、`nextTargetDate`、`nearestDueInDays`、`latestActionAt`，便于前端和行为洞察统一解释目标执行情况。

### 自评与历史

- POST `/ability/student/self-assessment`
  - Query：`dimensionId`、`score`、`feedback?`
- GET `/ability/student/assessments`
  - Query：`page=1`、`size=20`

说明：
- 自评会真实写入 `ability_assessments`，`assessmentType=self`。
- 分数按四维同口径处理：支持直接传 `0-5`；若传入历史 `0-100`，服务端会自动换算到 `0-5`。
- 自评提交后会同步刷新 `student_abilities`，并联动检查对应 `ability_goals` 是否达成。

### 能力报告

- GET `/ability/student/reports`
  - Query：`page=1`、`size=10`
- GET `/ability/student/report/latest`
- GET `/ability/student/report/latest-by-context`
  - Query（可选，优先级：`submissionId` > `assignmentId` > `courseId`）：
    - `courseId?`
    - `assignmentId?`
    - `submissionId?`
- GET `/ability/student/report/{reportId}`
- POST `/ability/student/report/generate`
  - Query：`reportType=monthly`、`startDate?`、`endDate?`

## 4. 教师端（TEACHER）

### GET `/ability/teacher/report/latest`

教师按上下文获取某学生的最新能力报告（用于评分页复用 AI 建议）。

- Query（可选，优先级：`submissionId` > `assignmentId` > `courseId`）：
  - `studentId`（必填）
  - `courseId?`
  - `assignmentId?`
  - `submissionId?`

### POST `/ability/teacher/report/from-ai`

将 AI 批改“规范化 JSON”落一条能力报告记录（用于学生端展示“查看详情”）。

- 兼容两种入参：
  - Query 方式：`studentId`、`normalizedJson`、`title?`、`courseId?`、`assignmentId?`、`submissionId?`、`aiHistoryId?`
  - JSON 方式（推荐，避免 URL 过长）：Body `{ studentId, normalizedJson, title?, courseId?, assignmentId?, submissionId?, aiHistoryId? }`

### GET `/ability/teacher/class/{courseId}/stats`

教师查看班级整体能力发展统计。

- Path：`courseId`
- Query：`dimensionId?`

说明：
- 当前基于课程 `active` 状态的 `enrollments` 学生集合，聚合其 `student_abilities` 最新快照。
- 返回字段包含：`studentCount`、`assessedCount`、`averageScore`、`maxScore`、`minScore` 和按等级分布的 `distributions`。

### POST `/ability/teacher/assessment`

教师对学生进行能力评估写入。

- Query：
  - `studentId`（必填）
  - `dimensionId`（必填）
  - `score`（必填）
  - `assessmentType`（必填）
  - `relatedId`（可选）
  - `evidence`（可选）

:::warning
`assessmentType/relatedId` 的口径需要与“作业评分写入能力维度”保持一致；如做口径调整，请同步更新相关文档与前端映射逻辑。
:::

### GET `/ability/teacher/ranking`

获取学生在某个能力维度上的排名（用于教学分析/概览展示）。

- Query：`dimensionId`、`limit=50`

说明：
- 当前按 `student_abilities.current_score` 的最新快照排名，而不是按历史明细逐条排序。
- `limit` 默认 `20`，最大 `100`；返回结果会附带 `dimensionId/dimensionName/dimensionCode` 方便前端直接展示。
