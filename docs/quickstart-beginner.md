---
title: 零基础快速上手
description: 本地跑通后端 + 前端 + Swagger（可选启动文档站）
outline: [2, 3]
---

# 零基础快速上手（Beginner Quickstart）

## 目标（Goal）

从 0 跑通项目（后端 + 前端），并能打开 Swagger 与页面完成一次请求验证。

## 前置条件（Prerequisites）

- Java 17：`java -version`
- Node.js >= 18（建议 20）：`node -v`
- Maven 3.8+：`mvn -v`
- MySQL 8.x：可登录，字符集建议 `utf8mb4`
- 端口未占用：8080（后端）、5173（前端）、4174（文档）

## 1. 初始化数据库（DB）

创建数据库并导入结构（`schema.sql` 为准）：

```bash
mysql -u root -p
CREATE DATABASE student_assessment_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit

# 导入结构（最新）
mysql -u root -p student_assessment_system < backend/src/main/resources/schema.sql

# 可选：初始数据（导入前请自行核对内容）
mysql -u root -p student_assessment_system < backend/src/main/resources/data.sql
```

## 2. 启动后端（Backend）

后端默认端口为 8080，且 `context-path=/api`，所以最终根路径是 `http://localhost:8080/api`。

```bash
cd backend
mvn spring-boot:run
```

验证：

- Swagger：`http://localhost:8080/api/swagger-ui.html`
- Health（如开启）：`http://localhost:8080/api/actuator/health`

## 3. 启动前端（Frontend）

前端默认端口 5173。API baseURL 的规则见 `frontend/src/api/config.ts`：

- 若设置了 `VITE_API_BASE` 或 `VITE_API_BASE_URL`，会优先使用（会自动补齐 `/api`）
- 若未设置，则默认请求走相对路径 `/api`（开发期由 Vite 代理转发到后端）

```bash
cd frontend
npm install
npm run dev
```

验证：

- 前端：`http://localhost:5173`
- 打开浏览器控制台，会打印 `Axios baseURL => ...`

:::tip 代理与直连怎么选
1. 直连（默认 `.env.development`）：`VITE_API_BASE_URL=http://localhost:8080` 或 `http://localhost:8080/api`，需要后端允许 CORS（本项目已放行本地 5173）。
2. 代理：删掉/清空 `VITE_API_BASE_URL`，让 Axios 走 `/api`；然后由 `frontend/vite.config.ts` 代理到 `VITE_BACKEND_URL`（默认 `http://localhost:8080`）。
:::

## 4.（可选）启动文档站（Docs）

```bash
cd docs
npm install
npm run docs:dev
```

- 文档站：`http://localhost:4174`
- 前端开发期也可以通过 `http://localhost:5173/docs` 访问（由 Vite 代理 `/docs` 到 4174）。
- 线上/独立文档站跳转由 `VITE_DOCS_URL` 控制（见 `frontend/src/router/index.ts` 的 `/docs` 路由）。

## 验证清单（Verify）

- [ ] `http://localhost:8080/api/swagger-ui.html` 可访问
- [ ] `http://localhost:5173` 可访问
- [ ] 前端发起请求时，路径命中 `/api/**`（控制台无 CORS 报错）
- [ ] 后端启动日志无数据库连接异常
- [ ] （可选）SSE：`/api/notifications/stream` 能建立连接

## 常见问题（Troubleshooting）

```mermaid
graph TD
  A[页面/接口不可用] --> B{404/401/500?}
  B -->|404| C[检查 context-path=/api 与 Axios baseURL]
  B -->|401| D[检查 localStorage token 与 Authorization 头]
  B -->|500| E[查看后端日志 + DB 配置]
  C --> F[检查 VITE_API_BASE(_URL) / Vite 代理]
  D --> G[重新登录/清理 token]
  E --> H[确认 schema.sql 已导入/账号密码正确]
```

## 下一步（Next）

- 架构总览：`/architecture-overview`
- 逐文件导览：`/file-walkthrough`
- 端到端样例：`/e2e-examples`
- 实战任务：`/cookbook`

