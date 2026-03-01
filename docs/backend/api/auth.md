---
title: 认证与用户 API（Auth）
description: 登录、注册、刷新令牌、登出、邮箱验证与前端对接口径
outline: [2, 3]
---

# 认证与用户 API（Auth）

以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`。

:::tip 约定
后端 `context-path=/api`，下文的接口路径均以 `/api/...` 展示。
:::

## 通用约定

- 认证：除注册/登录/邮箱验证/忘记密码等接口外，其它用户接口需要 `Authorization: Bearer <access_jwt>`。
- 响应封装：统一返回 `ApiResponse<T>`，形如 `{ code, message, data }`。

## Goal

- 明确 `AuthResponse` 字段口径（`accessToken`/`refreshToken`），避免文档与代码不一致。
- 给出可直接复用的 curl 与前端调用方式。

## Endpoints（/api/auth/*）

### 登录

- 方法/路径：`POST /api/auth/login`
- 请求体（示例）：

```json
{ "username": "teacher1", "password": "Passw0rd!" }
```

- 响应（成功示例，`data` 为 `AuthResponse`）：

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "user": { "id": 1, "role": "TEACHER", "username": "teacher1" },
    "accessToken": "<access_jwt>",
    "refreshToken": "<refresh_jwt>",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "issuedAt": "2026-03-01 12:00:00"
  }
}
```

- 常见失败：
  - 401：用户名/密码错误
  - `EMAIL_NOT_VERIFIED`：邮箱未验证时禁止登录（见后端 `AuthServiceImpl.login`）
  - 403：账号被禁用（`status=disabled`）

### 注册

- 方法/路径：`POST /api/auth/register`
- 请求体：参考 `frontend/src/types/auth.ts` 的 `RegisterRequest`
- 响应：成功时返回 `data=null`（注册后不直接下发令牌），并触发邮箱验证邮件发送（见 `AuthServiceImpl.register`）。

### 刷新令牌

- 方法/路径：`POST /api/auth/refresh?refreshToken=...`
- 响应：当前实现只返回新的 `accessToken` 与 `expiresIn`（`user/refreshToken` 可能为空），见 `AuthResponse.tokenRefresh(...)`。

成功示例：

```json
{
  "code": 200,
  "data": { "accessToken": "<access_jwt>", "expiresIn": 7200, "tokenType": "Bearer", "issuedAt": "2026-03-01 12:00:00" }
}
```

### 登出

- 方法/路径：`POST /api/auth/logout`
- 请求头：`Authorization: Bearer <accessToken>`
- 说明：当前后端未实现集中式黑名单，登出主要依赖前端清理本地 token（见 `AuthServiceImpl.logout`）。

### 邮箱验证

- 验证：`POST /api/auth/verify-email?token=...`
- 重发：`POST /api/auth/resend-verification?email=...&lang=zh-CN`

## 用户接口（/api/users/*）

用户相关接口已拆分到单独页面：`/backend/api/user`。本页仅保留“会被认证流程用到”的快捷导航：

- `GET /api/users/profile` / `PUT /api/users/profile`
- `PUT /api/users/me/avatar`
- `POST /api/users/change-password`
- `POST /api/users/forgot-password?email=...`
- `POST /api/users/reset-password`
  - `POST /api/users/me/email/change-initiate?newEmail=...`
  - `POST /api/users/email/change/confirm?token=...`

## Frontend Integration

### 调用入口

- API 封装：`frontend/src/api/auth.api.ts`、`frontend/src/api/user.api.ts`
- Axios 统一拦截器：`frontend/src/api/config.ts`
  - 会把后端统一包装 `ApiResponse<T>` 解包成 `T`（即 `data`），调用方通常直接拿到 `AuthResponse`。

### 推荐用法（走 Store）

```ts
import { useAuthStore } from '@/stores/auth'

const store = useAuthStore()
await store.login({ username, password })
```

### 直接调用 API（需要自己落地 token）

```ts
import { authApi } from '@/api/auth.api'

const data = await authApi.login({ username, password }) // 运行时通常已被拦截器解包为 AuthResponse
localStorage.setItem('token', data.accessToken)
localStorage.setItem('refreshToken', data.refreshToken)
```

## curl

登录：

```bash
curl -X POST 'http://localhost:8080/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"username":"teacher1","password":"Passw0rd!"}'
```

刷新令牌：

```bash
curl -X POST 'http://localhost:8080/api/auth/refresh?refreshToken=<refresh_jwt>'
```

获取个人资料：

```bash
curl 'http://localhost:8080/api/users/profile' \
  -H 'Authorization: Bearer <access_jwt>'
```

登出：

```bash
curl -X POST 'http://localhost:8080/api/auth/logout' \
  -H 'Authorization: Bearer <access_jwt>'
```

## Troubleshooting

- 400：参数校验失败，检查必填字段与格式（邮箱、密码长度）。
- 401：token 缺失/过期/无效；或登录失败。
- 403：账号禁用或权限不足。
- 刷新令牌接口：当前只返回 `accessToken`，如果你要在前端实现“自动刷新并续命”，需要把 `AuthResponse` 类型与刷新逻辑按实际返回体做兼容（例如把 `user/refreshToken` 改为可选字段）。
