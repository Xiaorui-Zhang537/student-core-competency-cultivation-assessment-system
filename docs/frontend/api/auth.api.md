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
