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

## 参数与返回
- `UpdateProfileRequest`：昵称/头像/性别/简介/生日/地区/电话/学号/工号 等可选字段
- `ChangePasswordRequest`：`{ currentPassword?: string; newPassword?: string }`
- `ResetPasswordRequest`：`{ token: string; newPassword?: string }`
- `UserProfileResponse`：在 `User` 基础上扩展个人字段

返回值经拦截器解包后，直接为业务数据对象。

## 示例
```ts
// 获取个人资料
const profile = await userApi.getProfile()

// 更新资料
await userApi.updateProfile({ nickname: 'Rui', bio: 'CS Student' })

// 更新头像（先用 file.api.ts 上传获得 fileId）
await userApi.updateAvatar(12345)

// 修改密码
await userApi.changePassword({ currentPassword: 'Old1!', newPassword: 'New1!' })

// 忘记/重置密码
await userApi.forgotPassword('user@example.com')
await userApi.resetPassword({ token, newPassword: 'New1!' })
```

## 错误处理
- 400：字段校验失败（长度/格式/必填）
- 401：未登录或 token 过期
- 409：冲突（邮箱/电话被占用）
