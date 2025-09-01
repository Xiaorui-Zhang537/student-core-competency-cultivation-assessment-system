# 调试处方集（Debug Recipes）

面向日常开发的高频问题，一招一式给出“现象 → 定位 → 处置”。

## 1. 通用基线检查
- **后端存活**：访问 `http://localhost:8080/api/swagger-ui.html`
- **前端 baseURL**：`.env.development` 设 `VITE_API_BASE_URL=http://localhost:8080`
- **统一响应**：成功 `{ code:200, data }`；异常 `{ code!=200, message }`

## 2. 网络请求定位（DevTools）
- Network 面板查看请求 URL 是否带 `/api` 前缀与鉴权头
- Status=401/403/404/5xx 时展开 Response，拷贝 `message` 追溯来源
- 禁用缓存后重试，排除缓存干扰

## 3. Axios 调试开关
```ts
// src/api/config.ts 中临时加入
axiosInstance.interceptors.request.use((cfg) => {
  console.debug('[REQ]', cfg.method, cfg.url, cfg.params ?? cfg.data)
  return cfg
})
axiosInstance.interceptors.response.use(
  (res) => {
    console.debug('[RES]', res.config.url, res.status, res.data?.code)
    return res
  },
  (err) => {
    console.debug('[ERR]', err.config?.url, err.response?.status, err.response?.data)
    return Promise.reject(err)
  }
)
```

## 4. Pinia 状态排查
```ts
// main.ts 启动时
import { createPinia } from 'pinia'
const pinia = createPinia()
pinia.use(({ store }) => {
  store.$subscribe((_mutation, state) => {
    console.debug(`[STORE:${store.$id}]`, JSON.parse(JSON.stringify(state)))
  })
})
```

## 5. 鉴权 401/403
- 401：token 缺失/过期 → 重新登录或调 `/auth/refresh`
- 403：角色不足 → 检查 `@PreAuthorize` 要求与当前用户角色
```bash
# 验证 token 是否有效
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/users/profile
```

## 6. 跨域/代理与端口
- 开发模式优先用代理：确保 `vite.config.ts` 存在
```ts
// vite.config.ts
server: { proxy: { '/api': { target: 'http://localhost:8080', changeOrigin: true } } }
```
- 端口冲突：调整前端端口或后端 `server.port`；文档站点为 4174

## 7. SSE 断线重连
```ts
let delay = 1000
function reconnect() {
  setTimeout(() => {
    start()
    delay = Math.min(delay * 2, 10000) // 指数退避上限 10s
  }, delay)
}
```
- 401 先刷新 token 再重连；429/503 等遵循 `Retry-After`

## 8. 分页/排序/过滤
- 从第 1 页开始：`page>=1`、`size>0`
- 排序白名单：只允许已约定字段，防止 SQL 注入
- 空态/异常态在组件中具备可视提示与“重试”按钮

## 9. 文件上传/下载
- 上传使用 `multipart/form-data`，字段名 `file`
- 413：增大代理与 `spring.servlet.multipart.max-file-size`
- 下载通过 axios（携带 Authorization），避免直接 `<a>` 直链

## 10. 后端日志与级别
```yaml
# application.yml
logging:
  level:
    com.noncore.assessment: DEBUG
```
- 观察控制台/日志文件中 Controller→Service→Mapper 调用链

## 11. 常用 curl 模板
```bash
# 登录
curl -X POST 'http://localhost:8080/api/auth/login' -H 'Content-Type: application/json' \
  -d '{"username":"teacher1","password":"Passw0rd!"}'

# 分页查询课程
curl 'http://localhost:8080/api/courses?page=1&size=10&query=AI' -H 'Authorization: Bearer <token>'

# SSE 探活（如需）
curl -N 'http://localhost:8080/api/notifications/stream?token=<token>'
```
