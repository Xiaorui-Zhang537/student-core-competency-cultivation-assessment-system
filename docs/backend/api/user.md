---
title: 用户资料 API（Users）
description: 个人资料、公开资料、头像、改密/找回密码、邮箱更换
outline: [2, 3]
---

# 用户资料 API（Users）

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

本项目后端 `context-path=/api`，下文接口路径均以 `/api/...` 展示。

## 1. 通用约定

响应封装：除极少数二进制接口外，统一返回 `ApiResponse<T>`。

鉴权：

- `GET/PUT /api/users/profile`、`POST /api/users/change-password`、`PUT /api/users/me/avatar`、邮箱更换发起：需要登录（带 `Authorization: Bearer <access_jwt>`）。
- `POST /api/users/forgot-password`、`POST /api/users/reset-password`、邮箱更换确认：通常为公开接口（是否公开以安全配置为准）。

## 2. 个人资料

### GET `/api/users/profile` 获取我的资料

响应：`ApiResponse<UserProfileResponse>`

### PUT `/api/users/profile` 更新我的资料

请求 Body：`UpdateProfileRequest`（字段均可选，按需更新）

```json
{
  "nickname": "新的昵称",
  "bio": "这是我的新简介。",
  "mbti": "INTJ",
  "school": "某大学",
  "subject": "计算机科学"
}
```

响应：`ApiResponse<User>`（后端会把 `password` 置空后再返回）

curl：

```bash
curl -X PUT 'http://localhost:8080/api/users/profile' \
  -H 'Authorization: Bearer <access_jwt>' \
  -H 'Content-Type: application/json' \
  -d '{"nickname":"新的昵称","bio":"这是我的新简介。"}'
```

## 3. 公开资料

### GET `/api/users/{id}/profile` 获取指定用户公开资料

用途：展示教师资料等（不含敏感信息）。

响应：`ApiResponse<UserProfileResponse>`

## 4. 头像

### PUT `/api/users/me/avatar` 更新头像（用已上传 fileId）

说明：先调用文件上传接口拿到 `fileId`，再设置为头像；后端会清理旧头像文件。

请求 Body：

```json
{ "fileId": 12345 }
```

响应：成功返回 200，无 data。

curl：

```bash
curl -X PUT 'http://localhost:8080/api/users/me/avatar' \
  -H 'Authorization: Bearer <access_jwt>' \
  -H 'Content-Type: application/json' \
  -d '{"fileId":12345}'
```

## 5. 密码管理

### POST `/api/users/change-password` 修改密码

权限：已登录

请求 Body：`ChangePasswordRequest`

```json
{ "currentPassword": "old", "newPassword": "newPassw0rd!", "confirmPassword": "newPassw0rd!" }
```

响应：成功返回 200，无 data。

### POST `/api/users/forgot-password?email=...` 忘记密码

权限：通常无需登录

Query：

- `email`：必填

响应：成功返回 200，无 data。

### POST `/api/users/reset-password` 重置密码

权限：通常无需登录

请求 Body：`ResetPasswordRequest`

```json
{ "token": "<reset_token>", "newPassword": "newPassw0rd!", "confirmPassword": "newPassw0rd!" }
```

响应：成功返回 200，无 data。

## 6. 邮箱更换

### POST `/api/users/me/email/change-initiate?newEmail=...&lang=zh-CN` 发起更换邮箱

权限：已登录

Query：

- `newEmail`：必填
- `lang`：可选，默认 `zh-CN`

响应：成功返回 200，无 data。

### POST `/api/users/email/change/confirm?token=...` 确认更换邮箱

权限：通常无需登录

Query：

- `token`：必填

响应：成功返回 200，无 data。

## 7. 常见错误与排查

- 400：校验失败（邮箱格式不正确、密码太短、confirm 不一致等）。
- 401：未登录或 token 过期。
- 403：越权访问。

