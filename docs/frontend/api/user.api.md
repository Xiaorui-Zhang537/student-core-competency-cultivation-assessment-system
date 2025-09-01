# 前端 API：user.api.ts

## 方法签名
- `getProfile(): Promise<ApiResponse<UserProfileResponse>>`
- `updateProfile(data: UpdateProfileRequest): Promise<ApiResponse<User>>`
- `updateAvatar(fileId: number): Promise<ApiResponse<void>>`
- `changePassword(data: ChangePasswordRequest): Promise<ApiResponse<void>>`
- `forgotPassword(email: string): Promise<ApiResponse<void>>`
- `resetPassword(data: ResetPasswordRequest): Promise<ApiResponse<void>>`
- `verifyEmail(token: string): Promise<ApiResponse<void>>`
- `resendVerification(): Promise<ApiResponse<void>>`

## 注意事项
- 邮箱验证与重置密码依赖后端邮件配置
- 更新资料与头像需登录态
