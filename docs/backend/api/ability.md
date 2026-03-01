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

### 能力目标（Goals）

- GET `/ability/student/goals`
- POST `/ability/student/goals`
- PUT `/ability/student/goals/{goalId}`
- DELETE `/ability/student/goals/{goalId}`

### 自评与历史

- POST `/ability/student/self-assessment`
  - Query：`dimensionId`、`score`、`feedback?`
- GET `/ability/student/assessments`
  - Query：`page=1`、`size=20`

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
