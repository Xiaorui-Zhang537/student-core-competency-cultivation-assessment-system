---
title: 文档与实现差异清单（Backlog）
description: 文档站与代码实现之间的已知差异、修复状态与建议
outline: [2, 3]
---

# 文档与实现差异清单（Backlog）

本页用于记录“文档与代码/接口实现”的已知差异，避免靠口口相传。每条差异都应当能定位到具体文件，并给出修复建议。

## 1. 已修复（本轮已处理）

### 1.1 前端 axios 返回类型口径不一致

- 现象：`frontend/src/api/config.ts` 会把后端 `{ code, message, data }` 自动解包成 `data`，但部分 `frontend/src/api/*.ts` 的 TypeScript 返回类型仍写成 `Promise<ApiResponse<T>>`。
- 影响：类型提示误导，导致调用处容易写出 `res.data` 这类无效访问；文档也难写一致。
- 修复：本轮已把主要 API 封装改为返回 `T`（即解包后的数据），并补充前端通用约定页。

相关文档：

- `/frontend/api/conventions`

### 1.2 重新发送验证邮件接口缺少 email 参数

- 现象：后端 `POST /api/auth/resend-verification` 要求 `email`，但旧前端封装未传参。
- 修复：前端 `userApi.resendVerification(email, lang?)` 已改为显式传参，Profile 页调用已补齐。

## 2. 待处理（建议后续排期）

### 2.1 `/students/my-submissions` 的 `courseId` 参数未生效

- 后端：`backend/src/main/java/com/noncore/assessment/controller/StudentController.java`
- 现象：控制器签名包含 `courseId`，但当前实现未将其传入 service 层（筛选不生效）。
- 影响：前端/文档容易误以为可按课程过滤。
- 建议：
  - 要么实现 `courseId` 筛选（service+mapper 支持）
  - 要么移除该参数并同步前端与文档（避免误导）

### 2.2 前端类型 `ApiResponse` 字段与后端不完全一致

- 前端：`frontend/src/types/api.ts`
- 后端：`backend/src/main/java/com/noncore/assessment/util/ApiResponse.java`
- 现象：前端 `ApiResponse` 里包含 `timestamp`，后端未必提供；且在当前 axios 设计下通常不会直接使用 `ApiResponse` 类型。
- 建议：明确 `ApiResponse` 是否仅用于“raw response 调试”；若不是，建议调整为与后端一致或移除对业务层暴露。

## 3. 约定

- 新增/修改接口：同时更新 `docs/backend/api/*.md` 与 `docs/frontend/api/*.md`。
- 修改响应结构：优先修改代码，再修改文档示例与前端类型，避免“文档先行但实现没跟上”。

