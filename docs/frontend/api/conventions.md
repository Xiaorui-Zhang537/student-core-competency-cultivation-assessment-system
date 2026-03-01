---
title: 前端 API 通用约定
description: axios 配置、baseURL、鉴权注入与“自动解包 ApiResponse”的返回类型约定
outline: [2, 3]
---

# 前端 API 通用约定

本节解释 `frontend/src/api/config.ts` 中的 axios 约定，以及 `frontend/src/api/*.ts` 的“返回类型口径”。

## 1. baseURL 规则

axios `baseURL` 计算逻辑（见 `frontend/src/api/config.ts`）：

- 优先使用环境变量：`VITE_API_BASE` 或 `VITE_API_BASE_URL`
- 若变量未以 `/api` 结尾，会自动补上 `/api`
- 若环境变量未配置，默认 `baseURL='/api'`

## 2. 自动注入认证与语言头

除 `/auth/login`、`/auth/register` 外，其它请求会自动附加：

- `Authorization: Bearer <token>`（来自 `localStorage.token`）
- `Accept-Language`、`X-Locale`（来自 i18n locale / localStorage）

## 3. 关键约定：自动“解包”后端 `ApiResponse<T>`

后端大多数接口返回 `{ code, message, data }`。前端响应拦截器会做两件事：

- `code != 200` 时，直接 `Promise.reject({ code, message })`
- `code == 200` 时，直接返回 **`data`**（而不是整个 `ApiResponse`）

因此：

- `api.get<T>(...)` 的 `T` 应该写成“业务数据类型”，例如 `Course[]`、`PageResult<User>`、`boolean`
- 文件/导出等 blob 接口，通常不走 `ApiResponse`，会直接返回二进制

:::warning 常见坑
如果在调用处还写 `res.data`，很可能拿到的是 `undefined`。因为拦截器已经把 `ApiResponse.data` 解包返回了。
:::

## 4. 站内文档链接

- 后端 API：见 [/backend/api/conventions](/backend/api/conventions)
- 前端 API 索引：见 [/frontend/api/index](/frontend/api/index)

