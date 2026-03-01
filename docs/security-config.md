---
title: 安全与配置（JWT / CORS / 环境变量）
description: 鉴权流程、公共 URL 白名单、CORS 与生产配置建议
outline: [2, 3]
---

# 安全与配置（JWT / CORS / 环境变量）

## 鉴权（JWT）

- 登录成功后返回 `accessToken/refreshToken`（见 `AuthResponse`），前端将 access token 存储在本地并用于后续请求。
- 请求头：`Authorization: Bearer <accessToken>`
- 刷新：`POST /api/auth/refresh?refreshToken=...`
- 401：前端会清理本地 token 并跳转到 `/auth/login`（详见 `frontend/src/api/config.ts`）

示例（curl）：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/users/profile
```

返回码（常见）：

- 200：业务成功
- 400：参数校验失败
- 401：未认证（token 缺失/过期/无效）
- 403：无权限（角色/方法级拦截）
- 404：资源不存在
- 409：状态冲突
- 5xx：服务端错误

## 公共 URL 放行（Public URLs）

后端在 `SecurityConfig.filterChain` 中放行以下路径（无需登录）：

- 预检：`OPTIONS /**`
- 认证：`/auth/**`
- 找回密码：`/users/forgot-password`、`/users/reset-password`
- 邮箱更换确认：`/users/email/change/confirm`
- 额外白名单：来自配置 `security.jwt.public-urls`（`backend/src/main/resources/application.yml`）

## CORS

当前项目的 CORS 允许列表由 `SecurityConfig` 的 `allowedOriginPatterns` 控制（包含本地 5173、以及生产域名 `stucoreai.space` 等）。

:::warning 注意
`backend/src/main/resources/application.yml` 里存在 `security.cors.allowed-origins`，但当前 `SecurityConfig` 并未读取该配置，实际生效的来源列表以 `SecurityConfig` 代码为准。
:::

建议：

- 本地开发优先使用 Vite 代理 `/api`（同源），减少 CORS 干扰。
- 生产环境若启用跨域（前后端不同域）：务必收敛允许来源到可信域名，并避免 `*`。

## Headers

- 为支持 PDF IFrame 预览，后端禁用了 `X-Frame-Options`（`frameOptions().disable()`）。
- CORS 暴露头目前为 `*`（见 `CorsFilter`），如需收敛可在生产环境按需白名单。

## 环境变量（关键）

数据库与安全：

- `DB_HOST` `DB_PORT` `DB_NAME` `DB_USERNAME` `DB_PASSWORD`
- `JWT_SECRET`（生产必须强随机）
- `JWT_EXPIRATION`、`JWT_REFRESH_EXPIRATION`（可选）

应用与文件：

- `APP_BASE_URL`（邮件链接跳转、外部访问域名等）
- `UPLOAD_DIR`、`MAX_FILE_SIZE`

AI（可选）：

- `AI_DEFAULT_PROVIDER`（默认 `google`）
- `GOOGLE_API_KEY`（Gemini）
- `GLM_API_KEY`（智谱，可选）

## 生产建议（Checklist）

- 使用强随机 `JWT_SECRET`
- CORS 收敛到可信域名
- 不在 Git 中提交任何真实密钥/密码（DB、Mail、JWT、AI）
- 上传目录独立挂载并限制权限，保持扩展名白名单
