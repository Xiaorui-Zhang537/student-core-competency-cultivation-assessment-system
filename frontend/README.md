<div align="center">
  <img src="../brand/logo.png" width="96" alt="StuCoreAI logo" />
  <h1>Frontend（Vue 3）</h1>
  <p>学生端 · 教师端 · 管理员端（SPA 按角色路由分流），面向“LLM 治理后”的稳定结构化输出进行渲染</p>
  <p>
    <a href="../README.md">返回项目首页</a>
    · <a href="#quickstart">快速开始</a>
    · <a href="#llm-fe">LLM 相关 UI</a>
    · <a href="#routing">路由与角色</a>
    · <a href="#api-store">API 与 Store</a>
    · <a href="#env">环境变量</a>
  </p>
  <p>
    <img alt="Vue" src="https://img.shields.io/badge/Vue-3.5-informational" />
    <img alt="Vite" src="https://img.shields.io/badge/Vite-5-informational" />
    <img alt="TypeScript" src="https://img.shields.io/badge/TypeScript-5.4-informational" />
    <img alt="Tailwind" src="https://img.shields.io/badge/Tailwind-3-informational" />
  </p>
</div>

## 项目定位

前端承载系统的主要交互界面：在不改变课堂原有教学结构的前提下，把评价、反馈与能力观察嵌入到教师熟悉的工作流里。

同时，前端默认把 AI 输出当作“可能不稳定/不完整”的上游：核心 UI 以 **后端稳定化后的 JSON contract** 为准，避免直接渲染模型原文导致抖动与不可控结构。

<a id="llm-fe"></a>
## LLM 相关 UI（与后端治理配套）

- **教师端 AI 批改**：展示多次取样结果、稳定化后的最终分，并可解释 `meta.ensemble`（mean2/closest2of3、chosenPair、pairDiff…）。后端机制见 `../docs/backend/api/ai-grading.md`。
- **SSE 流式批改体验**：支持逐次取样弹窗/进度展示（避免多次取样时等待感）。对应后端 `POST /ai/grade/essay/stream`。
- **JSON 解析失败降级展示**：对 `INVALID_JSON` 等错误做可视化提示与 raw 透出（便于教师排障/重试）。
- **行为洞察（不算分）**：以证据驱动的结构化洞察进行渲染（riskAlerts/actionRecommendations），不展示任何“行为分/权重”。后端治理见 `../docs/backend/api/behavior.md`。

## 技术栈（当前仓库）

- Vue `3.5.18`
- TypeScript `~5.4.0`
- Vite `^5.0.0`
- Pinia `^2.1.7`
- Vue Router `^4.2.5`
- Axios `^1.6.2`
- Tailwind CSS `^3.4.0` + DaisyUI `^5.1.18`
- ECharts `^5.4.3`

## 目录结构（核心）

```text
frontend/src/
├── api/              # 领域 API 封装（与 docs/frontend/api 对应）
├── stores/           # Pinia（业务状态与异步动作）
├── router/           # 路由与守卫
├── layouts/          # Public/Auth/Student/Teacher/Admin
├── features/         # 业务页面（按角色/领域拆分）
├── components/       # 通用组件与 UI 组件
├── i18n/ + locales/  # 国际化
├── styles/           # 全局样式
└── types/            # 类型
```

<a id="quickstart"></a>
## 快速开始

```bash
cd frontend
npm install
npm run dev
```

默认：
- 前端：`http://localhost:5173`
- 文档站（并行启动）：`http://localhost:4174`

<a id="routing"></a>
## 路由与角色

路由定义在 `src/router/index.ts`。

- 访客：`/`、`/auth/*`、`/docs`
- 学生：`/student/*`（dashboard/courses/lessons/assignments/analysis/community/notifications/assistant/help）
- 教师：`/teacher/*`（dashboard/courses/assignments/analytics/ai-grading/students/assistant/help/notifications）
- 管理员：`/admin/*`（console/people/moderation/moderation/center/courses/tools 等）

守卫规则：
- `meta.requiresAuth`：未登录会跳转 `/auth/login`
- `meta.role`：与用户角色不一致会跳到各自 dashboard
- `meta.requiresGuest`：已登录用户不可进入登录/注册等页面

<a id="api-store"></a>
## API 与 Store

### API 模块（`src/api`）

按领域拆分，核心包括：
- 认证用户：`auth.api.ts` `user.api.ts`
- 教学：`course.api.ts` `lesson.api.ts` `chapter.api.ts` `assignment.api.ts` `submission.api.ts` `grade.api.ts`
- 角色：`student.api.ts` `teacher.api.ts` `teacher-student.api.ts` `admin.api.ts`
- 能力/行为：`ability.api.ts` `behaviorInsight.api.ts` `behaviorEvidence.api.ts`
- 协作：`community.api.ts` `notification.api.ts` `chat.api.ts` `help.api.ts`
- AI：`ai.api.ts` `aiGrading.api.ts` `voicePractice.api.ts`
- 其它：`file.api.ts` `project.api.ts` `report.api.ts`

### Store（`src/stores`）

- 领域状态：`course` `lesson` `assignment` `submission` `grade` `ability` `community` `notifications` `chat` `ai` `help`
- 账户与 UI：`auth` `ui`

## 网络层约定（Axios）

实现见 `src/api/config.ts`：
- baseURL 来自 `VITE_API_BASE` / `VITE_API_BASE_URL`，否则回退 `/api`
- 若 baseURL 不以 `/api` 结尾，会自动补齐
- 自动附带 `Authorization: Bearer <token>`（登录/注册除外）
- 自动附带 `Accept-Language` 与 `X-Locale`
- `401` 会清理 token 并跳转登录页（在 `/auth/*` 内不强制跳转，避免打断忘记密码/注册流程）

<a id="env"></a>
## 环境变量

开发（示例，仓库已有 `frontend/.env.development`）：

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_ENV=development
VITE_MOCK_API=false
```

生产（示例，仓库已有 `frontend/.env.production`）：

```env
VITE_API_BASE=/api
VITE_ENV=production
VITE_DOCS_URL=http://docs.stucoreai.space
```

## Vite 开发代理

见 `vite.config.ts`：
- `/api` -> `VITE_BACKEND_URL` 或 `http://localhost:8080`
- `/docs` -> `http://localhost:4174`

## 联调与排错

- 先确认后端 Swagger 可访问：`http://localhost:8080/api/swagger-ui.html`
- 浏览器控制台会打印 `Axios baseURL`
- 遇到 401：检查 token 与角色

## 相关文档

- 前端 API 参考：`../docs/frontend/api/index.md`
- 前端深入：`../docs/frontend-deep-dive.md`
- 实时语音口语训练：`../docs/frontend/voice-practice.md`

## 最近更新（2026-02）

- 管理员社区治理页补齐中英文切换：状态选项、是否置顶、查看评论按钮、治理通知（成功/失败）均接入 i18n。
- 帖子治理表“操作”列标题与按钮组统一居中，按钮文案由“评论”调整为“查看评论”。
- 评论查看逻辑分流：表格按钮进入“指定帖子评论”；顶部类型选择器切换到评论时展示“全部评论”。
- 管理员帖子查询排序调整为置顶优先（`pinned DESC, created_at DESC`），与后端置顶语义一致。
