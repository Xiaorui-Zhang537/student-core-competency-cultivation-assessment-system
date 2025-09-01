# 常见问题（FAQ）

## 启动/访问
- 启动后端 404 或 Swagger 打不开？
  - 访问 `http://localhost:8080/api/swagger-ui.html`（注意 `/api` 前缀）
  - 检查端口是否被占用；或修改 `server.port`
- 前端 API 全部 404？
  - `.env.development` 设置 `VITE_API_BASE_URL=http://localhost:8080`
  - 开发模式也可使用 Vite 代理的 `/api` → `8080`
- 文档站点首页 404？
  - 确保 `docs/index.md` 存在；`docs/package.json` 设置了 `"type": "module"`

## 鉴权/JWT
- 频繁 401？
  - 登录后是否保存 token；请求是否带 `Authorization: Bearer <token>`
  - token 过期会被重定向到登录页（拦截器已处理）
- 刷新失败？
  - 调用 `/auth/refresh` 时请传 `refreshToken` 参数
  - 检查后端刷新过期时间配置

## SSE/通知
- SSE 流不工作？
  - 使用 `GET /api/notifications/stream`，支持 `Authorization` 头或 `?token=`
  - 确认浏览器未被扩展或代理阻断 SSE
- 未读数不同步？
  - 客户端收到 SSE 事件后需刷新列表或增量更新 Store

## 文件上传/下载
- 上传 400？
  - `multipart/form-data`，字段名必须是 `file`
  - 检查大小/类型白名单（见 `application.yml`）
- 预览失败？
  - 仅允许 `image/*` 类型；非图片请使用下载
- 下载 401？
  - 使用 axios 发起并携带鉴权头，避免 `<a>` 直链

## 代理/端口/CORS
- 跨域？
  - 开发模式下建议代理 `/api` 到 `http://localhost:8080`
  - 后端 CORS 放行需包含前端端口
- 端口冲突？
  - 修改后端 `server.port` 或前端 Vite 端口；文档站点默认 `4174`

## 分页/排序/过滤
- 怎么传分页？
  - 统一 `page`（从 1 开始）、`size` 参数
- 排序怎么传？
  - `sort=field,asc|desc`，后端进行字段白名单校验
- 多条件过滤？
  - 使用查询参数（如 `status/teacherId/query`），未提供的不生效

## 缓存失效与一致性
- 修改后数据不刷新？
  - 写操作会在 Service 层失效相关缓存；若仍旧缓存，请检查 Key 设计与 TTL
- 如何避免缓存雪崩/击穿？
  - 设置随机 TTL、热点 Key 保护与限流；必要时本地缓存兜底

## SSE 断线重连
- EventSource 偶尔断开？
  - 使用指数退避（1s→2s→5s→10s 上限）重连
  - 401 时先刷新 token 再重连；429/503 等等待后再试

## 网关/代理报 502/504
- 检查开发代理是否将 `/api` 正确转发到 `8080`
- 后端是否超时（建议 `readTimeout` ≥ 30s，长任务异步化）

## 限流 429 处理
- 遵循 `Retry-After` 响应头；前端退避重试，后端记录速率与用户维度

## 文件上传 413（Payload Too Large）
- 增大 Nginx/代理与后端 `spring.servlet.multipart.max-file-size`、`max-request-size`

## 数据库/Redis
- MySQL 连接失败？
  - 数据库已创建且导入 `schema.sql`
  - 账号/密码正确；URL 中特殊字符需转义
- Redis 必须开吗？
  - 否。大部分功能可在不启 Redis 的情况下运行

## AI 集成
- 无返回？
  - 检查 `AI_DEFAULT_PROVIDER`、`OPENROUTER_API_KEY/DEEPSEEK_API_KEY`、`DEEPSEEK_MODEL`
  - 检查网络连通性与配额/限流
- 会话/记忆不生效？
  - 确认 `/ai/conversations` 与 `/ai/memory` 正常

## 构建/文档
- VitePress ESM 报错？
  - `docs/package.json` 添加 `"type": "module"`
- 首页 404？
  - 确保 `docs/index.md` 存在并作为入口页
