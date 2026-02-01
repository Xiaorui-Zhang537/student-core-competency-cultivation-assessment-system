## AI 批改接口（稳定化版）

本项目的教师端 AI 批改接口默认启用“多次取样稳定算法”，用于降低单次大模型调用的随机性导致的分数抖动。

### 背景与设计目标

- **问题**：单次调用大模型会出现 `overall.final_score`（0~5）不稳定，影响教师端“AI 评分建议/一键应用”的一致性。
- **目标**：在不破坏现有前端渲染（仍读取 `overall.final_score` 与 `overall.dimension_averages`）的前提下，让后端输出更稳定的结果，并保留可追溯信息。

---

## 稳定算法（2 次 + 必要时第 3 次）

以 `overall.final_score`（0~5）作为稳定判断基准：

1. **默认取样 2 次**：得到 `s1`、`s2`，最终分 `final = (s1+s2)/2`（四舍五入到 0.1）。
2. **触发第 3 次**：若 `|s1 - s2| > diffThreshold`（默认 0.8），追加第 3 次得到 `s3`。
3. **三次时的最终分**：从 `s1/s2` 中选出与 `s3` 更接近的一个 `sClosest`，最终分 `final = (s3 + sClosest)/2`（四舍五入到 0.1）。

同时，后端会对“最终用于平均的那一对结果”进行：

- **子项分数平均**：每个子项的 `score` 取均值并 round 到 0.1；
- **证据 evidence 合并**：按 `quote` 去重合并（每个子项最多保留 3 条）；
- **建议 suggestions 合并**：去重合并（每个子项最多保留 5 条）；
- **整体反馈 holistic_feedback**：不拼接模型原文，改为后端确定性模板生成，避免文本继续抖动。

---

## 接口：`POST /ai/grade/files`

### 请求 Body（JSON）

- **fileIds**: `number[]`（必填）文件 ID 列表
- **model**: `string`（可选）支持 `google/*` 与 `glm-*`，否则回退默认
- **jsonOnly**: `boolean`（可选，默认 `true`）
- **useGradingPrompt**: `boolean`（可选，默认 `true`）
- **samples**: `number`（可选，默认 `1`；推荐 `2`；范围 1~3）
- **diffThreshold**: `number`（可选，默认 `0.8`；范围 0~5）

### 响应（成功）

返回 `results: [{ fileId, fileName, result, historyId }]`，其中 `result` 为标准结构（见“统一结构”），并可能包含 `meta.ensemble`（用于调试追溯）。

---

## 接口：`POST /ai/grade/essay`

### 请求 Body（AiChatRequest）

在原有字段基础上新增：

- **samples**: `number`（可选，默认 `1`，范围 1~3）
- **diffThreshold**: `number`（可选，默认 `0.8`，范围 0~5）

后端会强制 `jsonOnly=true` 执行批改，并写入批改历史（返回体含 `historyId`）。

---

## 接口：`POST /ai/grade/essay/batch`

每个元素均为 `AiChatRequest`，支持同样的 `samples/diffThreshold` 参数。

---

## 统一结构（核心字段）

后端最终输出会保证存在以下关键字段（前端依赖）：  

- **overall.final_score**：`0~5`（稳定后的最终分）  
- **overall.dimension_averages**：`{ moral_reasoning, attitude, ability, strategy }`  
- **四个维度对象**：`moral_reasoning / attitude_development / ability_growth / strategy_optimization`  

同时会附带调试信息（不影响前端展示）：

- **meta.ensemble**：
  - `samplesRequested`：请求的 samples
  - `diffThreshold`：阈值
  - `triggeredThird`：是否触发第 3 次
  - `method`：`mean2 | closest2of3`
  - `chosenPair`：最终用于平均的两次编号（1-based）
  - `pairDiff`：两次分差
  - `runs`：每次取样的 `finalScore`
  - `confidence`：简易置信度（0~1，分差越小越高）

---

## 降级策略（可靠性）

- 若第 2 次失败：退化为第 1 次结果。
- 若触发第 3 次但失败：使用前两次均值。
- 若所有取样均无法解析为 JSON：返回 `error=INVALID_JSON` 并透出 `raw`（便于排障）。

