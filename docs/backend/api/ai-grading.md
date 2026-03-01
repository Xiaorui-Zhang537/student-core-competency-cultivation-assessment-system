---
title: AI 批改 API（AI Grading）
description: 教师端 AI 作文批改（稳定化取样、SSE、历史记录）
outline: [2, 3]
---

# AI 批改 API（AI Grading）

> 以 Swagger 与后端实现为准：`http://localhost:8080/api/swagger-ui.html`

本项目后端 `context-path=/api`，下文接口路径均以 `/api/...` 展示。

## 1. 权限与返回封装

权限：以下批改与历史接口均要求 `TEACHER` 角色（见各接口注解）。

响应封装：统一返回 `ApiResponse<T>`。

## 2. 稳定化取样算法（samples + diffThreshold）

批改接口支持“多次取样稳定化”，用于降低单次大模型随机性导致的分数抖动。

- `samples`: 1~3，默认 1
- `diffThreshold`: 0~5（基于 `overall.final_score` 的 0~5 标尺），默认 0.8

规则（简述）：

- 先取样 2 次得到 `s1/s2`。
- 若 `|s1-s2| > diffThreshold`，会补第 3 次 `s3`。
- 最终分数由后端对“更稳定的两次”做平均并做字段合并（证据/建议去重，整体反馈使用确定性模板，附带 `meta.ensemble` 调试信息）。

## 3. 接口：按文件批改

### POST `/api/ai/grade/files`

用途：传 `fileIds`，后端下载文件并抽取文本（doc/docx/pdf/txt），再调用批改模型。

权限：`TEACHER`

请求 Body（JSON）：

```json
{
  "fileIds": [1, 2, 3],
  "model": "google/gemini-2.5-pro",
  "jsonOnly": true,
  "useGradingPrompt": true,
  "samples": 2,
  "diffThreshold": 0.8
}
```

说明：

- `model` 仅允许 `google/*` 或 `glm-*`，否则回退为 `google/gemini-2.5-pro`。
- `jsonOnly` 默认 `true`。当 `jsonOnly=true` 时会写入批改历史，并在每条 result 中返回 `historyId`。
- `samples>1` 仅在 `jsonOnly=true` 场景启用稳定化；`jsonOnly=false` 仅做一次调用（调试用）。

响应（示例）：

```json
{
  "code": 200,
  "data": {
    "results": [
      {
        "fileId": 1,
        "fileName": "essayA.docx",
        "result": {
          "overall": { "final_score": 4.2, "dimension_averages": { "ability": 4.0 } },
          "meta": { "ensemble": { "samplesRequested": 2, "diffThreshold": 0.8 } }
        },
        "historyId": 101
      },
      { "fileId": 2, "fileName": "essayB.pdf", "error": "..." }
    ]
  }
}
```

curl：

```bash
curl -X POST 'http://localhost:8080/api/ai/grade/files' \
  -H 'Authorization: Bearer <access_jwt>' \
  -H 'Content-Type: application/json' \
  -d '{"fileIds":[1,2,3],"model":"google/gemini-2.5-pro","jsonOnly":true,"useGradingPrompt":true,"samples":2,"diffThreshold":0.8}'
```

## 4. 接口：单篇文本批改（非流式）

### POST `/api/ai/grade/essay`

用途：批改一段文本（或前端拼装的 messages），后端强制 JSON 输出并写入历史。

权限：`TEACHER`

请求 Body：`AiChatRequest`（关键字段）

```json
{
  "messages": [{ "role": "user", "content": "<essay text>" }],
  "model": "google/gemini-2.5-pro",
  "samples": 2,
  "diffThreshold": 0.8
}
```

说明：

- 后端会强制 `jsonOnly=true`，并对返回 JSON 做 normalize（补齐 `overall/*` 等前端依赖字段）。
- 成功时会把 `historyId` 注入到返回对象里（与批改结果同层级）。

响应（示例，截断）：

```json
{
  "code": 200,
  "data": {
    "overall": { "final_score": 4.2 },
    "meta": { "ensemble": { "samplesRequested": 2 } },
    "historyId": 102
  }
}
```

当模型返回非 JSON 时（示例）：

```json
{ "code": 200, "data": { "error": "INVALID_JSON", "message": "...", "raw": "..." } }
```

## 5. 接口：单篇文本批改（SSE 流式逐次取样）

### POST `/api/ai/grade/essay/stream`

用途：当 `samples>1` 时避免前端长时间等待，通过 SSE 逐次推送每次 run 的分数，最后推送聚合结果。

权限：`TEACHER`

请求 Body：同 `POST /api/ai/grade/essay`

响应：`text/event-stream`（SSE）

事件约定（后端实现）：

- `event: ping` -> `{ ts }`（心跳）
- `event: meta` -> `{ samplesRequested, diffThreshold }`
- `event: run` -> `{ index, ok, finalScore05?, error? }`
- `event: diff` -> `{ s1, s2, diff12, threshold, triggeredThird }`
- `event: final` -> `{ result: <mergedNormalized>, historyId }`
- `event: error` -> `{ message }`

curl（观察流）：

```bash
curl -N 'http://localhost:8080/api/ai/grade/essay/stream' \
  -H 'Authorization: Bearer <access_jwt>' \
  -H 'Content-Type: application/json' \
  -d '{"messages":[{"role":"user","content":"<essay text>"}],"model":"google/gemini-2.5-pro","samples":2,"diffThreshold":0.8}'
```

## 6. 接口：批量文本批改

### POST `/api/ai/grade/essay/batch`

权限：`TEACHER`

请求 Body：`AiChatRequest[]`

响应：`ApiResponse<object[]>`（每个元素为一次批改结果；若某项返回非 JSON，会返回 `error/message/raw` 结构）

## 7. 批改历史（History）

### GET `/api/ai/grade/history`

权限：`TEACHER`

Query：

- `q`：可选，模糊搜索（实现以服务端为准）
- `page`：默认 1
- `size`：默认 20

响应：`ApiResponse<PageResult<AiGradingHistory>>`

`AiGradingHistory` 字段：

```json
{
  "id": 101,
  "teacherId": 11,
  "fileId": 8888,
  "fileName": "essayA.docx",
  "model": "google/gemini-2.5-pro",
  "finalScore": 4.2,
  "rawJson": "{...}",
  "createdAt": "2026-03-01 12:00:00"
}
```

### GET `/api/ai/grade/history/{id}`

权限：`TEACHER`

响应：`ApiResponse<AiGradingHistory>`

### DELETE `/api/ai/grade/history/{id}`

权限：`TEACHER`

响应：成功返回 200，无 data。

### POST `/api/ai/grade/history/{id}/delete`（兼容）

用途：兼容部分环境禁用 DELETE 的情况。

权限：`TEACHER`

响应：成功返回 200，无 data。

