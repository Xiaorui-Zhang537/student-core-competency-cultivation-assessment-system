# 认证与用户 API（Auth & User）

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

## 1. 登录
- 方法/路径：`POST /api/auth/login`
- 请求体（示例）：
```json
{ "username": "teacher1", "password": "Passw0rd!" }
```
- 响应（成功示例）：
```json
{ "code": 200, "message": "OK", "data": { "token": "<jwt>", "refreshToken": "<jwt>", "user": { "id": 1, "role": "TEACHER", "username": "teacher1" }}}
```
- 失败示例：
```json
{ "code": 401, "message": "Invalid credentials" }
```

## 2. 注册
- 方法/路径：`POST /api/auth/register`
- 请求体：参考前端 `types/auth.ts` 的 `RegisterRequest`

## 3. 刷新令牌
- 方法/路径：`POST /api/auth/refresh?refreshToken=...`
- 响应：新的 `token` 与可选 `refreshToken`

## 4. 登出
- 方法/路径：`POST /api/auth/logout`

## 5. 用户资料
- 获取资料：`GET /api/users/profile`
- 更新资料：`PUT /api/users/profile`
- 更新头像：`PUT /api/users/me/avatar`（body: `{ fileId }`）
- 修改密码：`POST /api/users/change-password`
- 忘记密码：`POST /api/users/forgot-password?email=...`
- 重置密码：`POST /api/users/reset-password`

## 6. 邮箱验证（如启用）
- 验证：`POST /api/auth/verify-email?token=...`
- 重发：`POST /api/auth/resend-verification`

## 7. 返回码与错误对照
- 200：业务成功
- 400：参数校验失败
- 401：未认证（token 缺失/过期/无效；或登录失败）
- 403：无权限
- 404：资源不存在
- 409：状态冲突（例如重复用户名）
- 5xx：服务端错误

---

# 前端对接（auth.api.ts / user.api.ts）

## 1. 方法签名
- `authApi.login(data: LoginRequest): Promise<ApiResponse<AuthResponse>>`
- `authApi.register(data: RegisterRequest): Promise<ApiResponse<void>>`
- `authApi.refreshToken(refreshToken: string): Promise<ApiResponse<AuthResponse>>`
- `authApi.logout(): Promise<ApiResponse<void>>`
- `userApi.getProfile(): Promise<ApiResponse<UserProfileResponse>>`
- `userApi.updateProfile(data: UpdateProfileRequest): Promise<ApiResponse<User>>`
- `userApi.updateAvatar(fileId: number): Promise<ApiResponse<void>>`
- `userApi.changePassword(data: ChangePasswordRequest): Promise<ApiResponse<void>>`
- `userApi.forgotPassword(email: string): Promise<ApiResponse<void>>`
- `userApi.resetPassword(data: ResetPasswordRequest): Promise<ApiResponse<void>>`
- `userApi.verifyEmail(token: string): Promise<ApiResponse<void>>`
- `userApi.resendVerification(): Promise<ApiResponse<void>>`

## 2. 最小调用示例（登录）
```ts
import { authApi } from '@/api/auth.api'
import { useAuthStore } from '@/stores/auth'

const store = useAuthStore()
const res = await authApi.login({ username, password })
store.setSession(res.data.token, res.data.refreshToken, res.data.user)
```

## 3. 视图与路由交互
- `LoginView.vue`：表单提交 → 调用 `authApi.login` → 保存会话 → 跳转首页
- 路由守卫：401 时自动跳登录页（见 `src/api/config.ts`）

## 8. 标准响应包装（ApiResponse<T>）
所有接口返回统一包装：
```json
{ "code": 200, "message": "OK", "data": { /* 具体业务数据 */ } }
```
- code：业务状态码（200 表示成功）
- message：可读信息（错误时用于提示）
- data：具体数据负载（成功时存在）

## 9. curl 示例
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

## 10. 常见错误与排查
- 400 参数校验失败：检查必填字段与格式（邮箱、密码长度）
- 401 认证失败：账号密码错误或 token 过期/无效；确认前后端时间同步
- 403 无权限：检查用户角色是否满足 `@PreAuthorize` 条件
- 409 冲突：用户名已存在；请更换后重试
