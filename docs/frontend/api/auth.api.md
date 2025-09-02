# 前端 API：auth.api.ts

## 方法签名
- `login(data: LoginRequest): Promise<ApiResponse<AuthResponse>>`
- `register(data: RegisterRequest): Promise<ApiResponse<void>>`
- `refreshToken(refreshToken: string): Promise<ApiResponse<AuthResponse>>`
- `logout(): Promise<ApiResponse<void>>`

## 最小示例
```ts
import { authApi } from '@/api/auth.api'
const res = await authApi.login({ username, password })
localStorage.setItem('token', res.data.token)
```

## 注意事项
- 登录/注册请求不会附带旧 token（见 `src/api/config.ts`）
- 401 时拦截器会清理会话并跳转登录页

## 参数与返回
- `LoginRequest`：`{ username: string; password?: string }`
- `RegisterRequest`：`{ username: string; email: string; password: string; confirmPassword: string; role: 'student'|'teacher'; avatar?; lang? }`
- `AuthResponse`：`{ user: User; accessToken: string; refreshToken: string }`

返回值遵循统一响应：`ApiResponse<T>`，经 axios 拦截器解包后，通常直接返回 `T`（见 `src/api/config.ts`）。

## 进阶示例
```ts
import { useAuthStore } from '@/stores/auth'

const store = useAuthStore()
// 登录并保存会话
const res = await authApi.login({ username, password })
// 拦截器已将 { code, message, data } 解包为 data
store.$patch({ token: res.accessToken, user: res.user })

// 刷新 token
const refreshed = await authApi.refreshToken(localStorage.getItem('refreshToken')!)
store.$patch({ token: refreshed.accessToken })

// 登出（后端状态+前端清理）
await authApi.logout()
store.$reset()
```

## 错误处理与状态码
- 400：参数不合法（用户名/邮箱/密码格式）
- 401：凭证无效/过期；拦截器会清理本地 token 并跳登录页
- 409：注册冲突（重复用户名/邮箱）

## 权限与安全
- 登录后所有请求会自动携带 `Authorization: Bearer <token>`
- 避免在 URL 中透传明文密码/敏感信息
