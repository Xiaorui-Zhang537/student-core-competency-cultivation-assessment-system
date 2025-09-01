# 术语表（Glossary）

## 通用
- **DTO**：Data Transfer Object，接口入参/出参的结构化数据。
- **Entity**：实体类，通常与数据库表结构对应。
- **Mapper**：MyBatis 的数据访问接口，配合 XML 执行 SQL。
- **Pagination（分页）**：按页返回数据，避免一次性加载过多。
- **Idempotency（幂等）**：同一操作重复执行结果相同，防止重复提交。

## 后端
- **Controller**：接收 HTTP 请求，做参数校验与转发。
- **Service**：承载业务逻辑与事务边界。
- **Repository/Mapper**：数据访问层，执行 SQL。
- **@Transactional**：事务注解，出错时整体回滚。
- **@ControllerAdvice**：全局异常处理。
- **OpenAPI/Swagger**：接口文档与交互调试。
- **SSE（Server-Sent Events）**：服务端向浏览器推送事件的协议。

## 安全
- **JWT**：JSON Web Token，无状态认证凭证。
- **RBAC**：基于角色的访问控制（如 TEACHER、ADMIN）。
- **CORS**：跨域资源共享，需要后端允许来源与方法。
- **CSRF/XSS/SQLi**：常见 Web 安全风险，应通过校验、过滤与参数化查询防御。

## 前端
- **Pinia**：Vue 3 官方推荐状态管理。
- **Axios**：HTTP 请求库，支持拦截器。
- **Vite**：开发服务器与构建工具。
- **ECharts**：前端可视化图表库。
- **i18n**：国际化，支持中英文切换。

## 数据访问/分页
- **PageHelper**：MyBatis 的分页插件，自动生成分页 SQL 并统计总数。
- **动态 SQL**：MyBatis XML 通过 `<if>`/`<where>`/`<trim>` 组合可选条件。
- **乐观锁/并发控制**：通过版本号或条件更新避免并发覆盖。

## 协议/安全扩展
- **DPoP**：Demonstrating Proof-of-Possession，绑定客户端密钥的令牌保护机制。
- **Rate Limiting（限流）**：限制请求速率防止滥用（如 429）。
- **Backoff（退避）**：失败后逐步延长重试间隔（指数/抖动）。

## 可靠性/一致性
- **幂等 Token**：防重复提交的唯一标识。
- **缓存击穿/雪崩/穿透**：缓存相关典型故障模式与防护策略。

## 性能/可观测
- **Caching（缓存）**：提升读取速度（如 Redis）。
- **Throttling/Debouncing**：节流/防抖，减少频繁请求或渲染。
- **Tracing/Logging**：调用链追踪与日志，定位问题。
