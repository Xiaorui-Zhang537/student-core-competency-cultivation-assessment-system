# 安全与配置（JWT / CORS / 环境变量）

## 鉴权（JWT）
- 登录后签发 `token`，存储在前端本地
- 请求头：`Authorization: Bearer <token>`
- 刷新：`/api/auth/refresh`（如实现）
- 公共 URL 放行：见 `security.jwt.public-urls` 列表（`application.yml`）

示例（curl）：
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/users/profile
```

返回码对照：
- 200：业务成功
- 400：参数校验失败
- 401：未认证（token 缺失/过期/无效）
- 403：无权限（角色/方法级拦截）
- 404：资源不存在
- 409：并发/状态冲突
- 5xx：服务端错误

## CORS
- 默认允许来源（开发）：`http://localhost:5173`、`http://127.0.0.1:5173`、`http://0.0.0.0:5173`、`http://*.local:5173`、`http://*.lan:5173`
- 预检请求（OPTIONS）已全局放行，确保返回 CORS 头
- 生产环境建议将允许来源收敛到受信任域名
- 如跨域失败：确认前端基址、代理和后端 CORS 配置是否一致

## Headers
- 已禁用 `X-Frame-Options`（frameOptions.disable）以支持 PDF IFrame 预览与内嵌
- 所有响应暴露头 `*`（按 `CorsFilter` 配置），如需收敛请在生产环境按需白名单

## 环境变量（关键）
- `DB_HOST/DB_PORT/DB_NAME/DB_USERNAME/DB_PASSWORD`
- `JWT_SECRET/EXPIRATION/REFRESH_EXPIRATION`
- `REDIS_HOST/PORT/PASSWORD`（可选）
- `AI_DEFAULT_PROVIDER`、`OPENROUTER_API_KEY/DEEPSEEK_API_KEY`、`DEEPSEEK_MODEL`
- 上传与日志：`file.*`、`logging.*`

## 公共 URL 放行（与代码一致）
- 允许未经身份验证访问的端点包括：
  - 预检：`OPTIONS /**`
  - 认证相关：`/auth/**`
  - 用户找回流程：`/users/forgot-password`、`/users/reset-password`
  - 邮箱更换确认：`/users/email/change/confirm`
  - 额外公共 URL：来自 `security.jwt.public-urls` 配置项（`application.yml`）

> 以上与 `SecurityConfig.filterChain` 的 `authorizeHttpRequests` 配置一致，详见后端源码。

> 生产环境务必使用强随机的 `JWT_SECRET`，限制 CORS 仅允许可信来源，并做好日志脱敏与文件扩展名白名单。
