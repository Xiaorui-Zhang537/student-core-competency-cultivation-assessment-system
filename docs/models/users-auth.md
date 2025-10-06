# 模型对照：用户与认证（Users & Auth）

## 用户（User）
- 典型字段：`id/username/email/password(role=Hash)/role/avatar/student_no/teacher_no/first_name/last_name/nickname/phone/created_at/updated_at`
- 变更影响：`user.api.ts`、`auth.api.ts`、`stores/auth.ts`、用户资料视图
- 变更影响：`user.api.ts`、`auth.api.ts`、`stores/auth.ts`、用户资料视图

## 认证（Auth）
- 登录请求：`LoginRequest { username, password }`
- 响应：`AuthResponse { token, refreshToken, user }`
- 注册请求：`RegisterRequest { username, email, password, ... }`
- 变更影响：路由守卫、Axios 拦截器、会话存储

### 找回与重置
- 忘记密码：`POST /api/users/forgot-password { email }`
- 重置密码：`POST /api/users/reset-password { token, newPassword }`
- 更换邮箱确认：`POST /api/users/email/change/confirm { token }`

## 密码与验证
- 修改密码：`ChangePasswordRequest { oldPassword, newPassword }`
- 重置密码：`ResetPasswordRequest { token, newPassword }`
- 邮箱验证：`token`
