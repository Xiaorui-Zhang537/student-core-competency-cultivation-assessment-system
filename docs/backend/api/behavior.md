---
title: 行为证据评价 API（Behavior）
description: 阶段一纯代码摘要 + 阶段二 AI 解读（不计分）的数据契约与调用方式
outline: [2, 3]
---

# 行为证据评价 API（Behavior）

本模块用于记录与聚合学生行为事件，形成“可解释的行为证据”。强约束：**不参与任何分数计算**（唯一数值分数仍以作业/评分体系为准）。

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

本项目后端 `context-path=/api`，下文接口路径均以 `/api/...` 展示。

## 1. 设计原则

- 两阶段：
  - 阶段一：纯代码聚合为结构化摘要（不调用 AI）
  - 阶段二：基于摘要做解释/总结/建议（可调用 AI，但不产生新分数）
- 所有结论都应可回溯到 `evidenceIds` / `eventRefs`（便于审计与治理）

## 2. 阶段一：行为摘要（不调用 AI）

### GET `/api/behavior/summary`

权限：已登录（`isAuthenticated()`）

- 学生：仅可查看自己（`studentId` 不传时默认取当前用户；若传且不是本人会 403）
- 教师：`studentId` 必填，且必须与该学生存在课程交集（否则 403）

Query：

- `studentId`：可选/必填（见权限说明）
- `courseId`：可选
- `range`：默认 `7d`

curl（学生看自己）：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/behavior/summary?range=7d"
```

curl（教师看某学生）：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/behavior/summary?studentId=1001&courseId=2&range=7d"
```

响应：`ApiResponse<BehaviorSummaryResponse>`（字段摘录示例）：

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "schemaVersion": "behavior_summary.v1",
    "meta": { "studentId": 1001, "courseId": 2, "range": "7d", "inputEventCount": 42 },
    "activityStats": { "ai": { "questionCount": 5 }, "community": { "askCount": 1, "answerCount": 2 } },
    "evidenceItems": [{ "evidenceId": "ev_20260108_0001", "eventRefs": [1001, 1002] }],
    "signals": { "highIterationOnFeedback": true }
  }
}
```

说明：

- 阶段一摘要可能会复用最近一次快照；若检测到有新事件，会自动重算并写入新快照（细节以 `BehaviorAggregationService` 实现为准）。
- 指定 `courseId` 查询时，通常会同时包含“无法归属具体课程”的全局事件（例如社区行为）。

## 3. 阶段二：行为洞察（AI 解读，不算分）

### POST `/api/behavior/insights/generate`

权限：已登录（`isAuthenticated()`）

Query：

- `studentId`（必填）
- `courseId`（可选）
- `range`：默认 `7d`
- `model`：可选（不传则使用服务端默认）
- `force`：默认 `false`；学生不允许 `force=true`

curl（注意：该接口用 query 参数，不需要 body）：

```bash
curl -X POST -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/behavior/insights/generate?studentId=1001&courseId=2&range=7d"
```

响应：`ApiResponse<BehaviorInsightResponse>`（字段摘录示例）：

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "schemaVersion": "behavior_insight.v1",
    "snapshotId": 101,
    "riskAlerts": [{ "severity": "warn", "title": "近7天学习节奏不稳定", "evidenceIds": ["ev_20260108_0001"] }],
    "actionRecommendations": [{ "title": "把反馈迭代固化成自查清单", "evidenceIds": ["ev_20260108_0001"] }],
    "meta": { "status": "success", "model": "google/gemini-2.5-pro" }
  }
}
```

治理要点（实现口径）：

- 证据不足时可能返回 `meta.status=partial`（并且不会触发 AI）
- 学生侧有触发额度/冷却策略（测试策略以服务实现为准）
- 教师/管理员可复用已有结果；`force=true` 允许重跑

### GET `/api/behavior/insights` 获取最新洞察

Query：

- `studentId`（必填）
- `courseId`（可选）
- `range`：默认 `7d`

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/behavior/insights?studentId=1001&courseId=2&range=7d"
```

## 4. Troubleshooting

- 400：缺少必填参数（例如教师查摘要没传 `studentId`；或学生传了 `force=true`）
- 401：未登录
- 403：学生查看/生成非自己的洞察；教师与学生无课程交集
