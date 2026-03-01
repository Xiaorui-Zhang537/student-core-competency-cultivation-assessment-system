---
title: "`auth.api.ts`"
description: 登录/注册/刷新/登出等认证相关 API 封装
outline: [2, 3]
---

# 前端 API：auth.api.ts

## 方法签名
- `login(data: LoginRequest): Promise<AuthResponse>`
- `register(data: RegisterRequest): Promise<void>`
- `refreshToken(refreshToken: string): Promise<AuthResponse>`
- `logout(): Promise<void>`

## 最小示例
```ts
import { authApi } from '@/api/auth.api'
const data = await authApi.login({ username, password })
// 注意：axios 拦截器会把 ApiResponse<T> 解包成 T
localStorage.setItem('token', data.accessToken)
localStorage.setItem('refreshToken', data.refreshToken)
```

## 注意事项
- 登录/注册请求不会附带旧 token（见 `frontend/src/api/config.ts`）
- 401 时拦截器会清理会话并跳转登录页

## 参数与返回
- `LoginRequest`：`{ username: string; password?: string }`
- `RegisterRequest`：`{ username: string; email: string; password: string; confirmPassword: string; role: 'student'|'teacher'; avatar?; lang? }`
- `AuthResponse`：`{ user: User; accessToken: string; refreshToken: string }`

返回值遵循统一响应：`ApiResponse<T>`，经 axios 拦截器解包后，通常直接返回 `T`（见 `frontend/src/api/config.ts`）。

:::warning 刷新令牌返回体
后端当前实现的刷新令牌接口可能只返回 `{ accessToken, expiresIn, tokenType, issuedAt }`，不一定包含 `user/refreshToken`。如果要在前端实现自动刷新，需要按真实返回体做兼容（建议把 `AuthResponse` 的部分字段改为可选）。
:::

## 进阶示例
```ts
import { useAuthStore } from '@/stores/auth'

const store = useAuthStore()
await store.login({ username, password })
await store.logout()
```

## 错误处理与状态码
- 400：参数不合法（用户名/邮箱/密码格式）
- 401：凭证无效/过期；拦截器会清理本地 token 并跳登录页
- 409：注册冲突（重复用户名/邮箱）

## 权限与安全
- 登录后所有请求会自动携带 `Authorization: Bearer <accessToken>`（本地存储键名为 `token`）
- 避免在 URL 中透传明文密码/敏感信息
