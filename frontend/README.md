# 学生核心能力培养评估系统 - 前端

## 📖 项目概述

本项目是“学生核心能力培养评估系统”的前端实现，采用 Vue 3、TypeScript 和 Pinia 构建。它是一个严格遵循其后端API的单页面应用 (SPA)。

### 核心开发哲学：后端即真理

为了确保前后端的高度一致性和应用的长期可维护性，本项目遵循**“后端即真理” (Backend as Truth)**的核心开发原则。这意味着：

-   **无后端，不开发**: 所有前端功能、数据模型和UI组件都必须直接映射到由后端服务定义的、已存在的API端点。
-   **禁止臆测**: 不得基于假设或未来的API进行开发。任何在前端展示的数据或可执行的操作，都必须有确切的后端API支持。
-   **功能对齐**: 如果后端API不支持某个功能（例如，复杂的筛选、特定的用户操作），则前端不应实现该功能，即使UI/UX原型中存在。

这一原则旨在消除前后端不匹配导致的问题，简化开发流程，并确保前端应用是后端能力的真实、准确的反映。

---

## 🛠️ 技术栈

-   **核心框架**: [Vue 3](https://vuejs.org/) (Composition API)
-   **开发语言**: [TypeScript](https://www.typescriptlang.org/) (严格模式)
-   **构建工具**: [Vite](https://vitejs.dev/)
-   **状态管理**: [Pinia](https://pinia.vuejs.org/)
-   **路由**: [Vue Router 4](https://router.vuejs.org/)
-   **UI 样式**: [Tailwind CSS](https://tailwindcss.com/) + [DaisyUI](https://daisyui.com/)
-   **HTTP 客户端**: [Axios](https://axios-http.com/)
-   **图表库**: [ECharts](https://echarts.apache.org/)
-   **代码规范**: [ESLint](https://eslint.org/) + [Prettier](https://prettier.io/)

---

## 🚀 开发指南

### 环境要求

-   **Node.js**: `v18.0.0` 或更高版本。
-   **npm**: `8.x` 或更高版本。

### 环境配置

在开始之前，建议在 `frontend` 目录下创建 `.env.development` 并设置后端基础地址（不含 `/api`）。

1.  创建文件：
    ```bash
    touch .env.development
    ```

2.  配置 API 基地址：
    ```
    VITE_API_BASE_URL=http://localhost:8080
    ```
    说明：`src/api/config.ts` 会自动在末尾追加 `/api`；若未设置，则默认走本地代理 `/api`。

3.  开发代理说明（可选）

    `vite.config.ts` 已配置：
    - `/api` 代理到 `process.env.VITE_BACKEND_URL || http://localhost:8080`
    - `/docs` 代理到 `http://localhost:4174`（本地文档站）

    在终端设置一次：
    ```bash
    export VITE_BACKEND_URL=http://localhost:8080
    ```

4.  可用环境变量清单（前端）

- `VITE_API_BASE_URL`：后端基础地址（不含 `/api`），如 `http://localhost:8080`
- `VITE_BACKEND_URL`：开发代理目标（`/api` 代理）
- `VITE_DOCS_URL`：在应用内访问 `/docs` 时的跳转地址（设置后前端会直接跳转至此 URL）

### 快速开始

```bash
# 1. 克隆仓库
git clone <repository-url>

# 2. 进入前端项目目录
cd frontend

# 3. 安装依赖
npm install

# 4. 启动开发服务器
npm run dev
```
应用将在 `http://localhost:5173` 上启动，并具有热重载功能。

### 主要脚本命令

-   `npm run dev`: 启动开发服务器。
-   `npm run build`: 为生产环境构建和打包应用。
-   `npm run preview`: 在本地预览生产构建的成果。
-   `npm run lint`: 使用 ESLint 检查代码中的规范问题。
-   `npm run type-check`: 使用 `vue-tsc` 进行全局 TypeScript 类型检查，确保类型安全。

### 代码风格与提交

- ESLint + Prettier：建议在编辑器启用保存时自动修复。
- 提交信息遵循 Conventional Commits（feat/fix/docs/chore/refactor/test 等）。
- 推荐配置 Husky + lint-staged 作为预提交钩子。

---

## 🏗️ 项目结构

重构后的项目遵循清晰的分层架构，旨在实现高度的模块化和关注点分离。

```
frontend/
└── src/
    ├── api/          # API客户端层：封装对后端端点的所有HTTP请求。
    ├── assets/       # 静态资源 (图片, 全局CSS等)。
    ├── components/   # 可复用的全局UI组件 (例如 Card, Button)。
    ├── features/     # 功能模块，按用户角色或业务领域划分。
    │   ├── auth/
    │   ├── shared/   # 跨角色共享的视图 (Profile, Community)。
    │   ├── student/
    │   └── teacher/
    ├── layouts/      # 页面布局组件 (例如 StudentLayout, TeacherLayout)。
    ├── router/       # 路由配置和导航守卫。
    ├── stores/       # 状态管理层 (Pinia)，封装业务逻辑和应用状态。
    ├── types/        # 类型定义层：与后端DTO严格对齐的TypeScript接口。
    └── utils/        # 通用工具函数 (例如 api-handler)。
```

---

## 📚 页面地图（角色 → 视图 → Store → API → 类型）

```
Teacher（教师端）
  DashboardView.vue         -> useTeacherStore / useCourseStore -> teacher.api.ts / course.api.ts -> types/teacher.ts, types/course.ts
  ManageCourseView.vue      -> useCourseStore                   -> course.api.ts                  -> types/course.ts
  GradeAssignmentView.vue   -> useGradeStore                    -> grade.api.ts                   -> types/grade.ts

Student（学生端，Beta）
  DashboardView.vue         -> useStudentStore / useAbilityStore -> student.api.ts / ability.api.ts -> types/student.ts, types/ability.ts
  CourseDetailView.vue      -> useCourseStore                   -> course.api.ts                  -> types/course.ts
  AssignmentSubmitView.vue  -> useSubmissionStore               -> submission.api.ts              -> types/submission.ts

Shared（通用）
  CommunityView.vue         -> useCommunityStore                -> community.api.ts               -> types/community.ts
  PostDetailView.vue        -> useCommunityStore                -> community.api.ts               -> types/community.ts
```

---

## 🔄 数据流与错误处理（简图）

表单/交互 → Store action → API（Axios）→ 统一响应处理（`ApiResponse`）→
映射到本地状态 → 组件渲染（含加载/空态/异常态）

- Axios 请求：除登录/注册外自动附带 `Authorization: Bearer <token>`（见 `src/api/config.ts`）
- 401：清理 token 并跳转登录页
- 其它错误：按 `ApiResponse.code/message` 提示；控制台在开发模式打印错误

### 国际化（i18n）

- 语言包位于 `src/locales/zh-CN` 与 `src/locales/en-US`。
- 文案键命名采用模块化前缀（如 `student.assignments.*`、`teacher.ai.*`）。
- 组件内避免硬编码文案；新增文案需同时补齐中英两份。

---

## 🧪 联调与排错（三步）

1) baseURL：控制台打印 `Axios baseURL` 是否为 `http://localhost:8080/api`
2) 代理：开发模式访问 `/api/...` 是否正确代理到后端 8080
3) 后端：`/api/swagger-ui.html` 可打开；端点可在 Swagger 中直接成功

---

## 🧭 项目状态
- 教师端：已完成
- 学生端：开发中（Beta）；主题改造完成（v0.3.0）。

---

## 🎨 主题与彩色玻璃
- 浅色：`retro`（米黄色 `base-100` 静态底色）；深色：`synthwave`。
- 主题切换：新版（retro/synthwave）与 Legacy（动态背景），动态背景指南见 `docs/frontend/ui-backgrounds.md`。
- 彩色玻璃 tint：`glass-tint-{primary|secondary|accent|info|success|warning|error}`（仅新版主题生效）。
- 详情见 `docs/ui-theming.md`。

### UI/UX 规范

- 统一使用 `components/ui` 下的按钮、卡片、输入类、筛选器等组件。
- 新页面优先采用 `PageHeader` 放置标题/副标题与操作区。
- 表单输入统一使用 `GlassInput/GlassTextarea/GlassSearchInput`；筛选使用 `FilterBar`。

#### 组件规范案例

PageHeader（放置标题、简介与操作区）：

```vue
<PageHeader :title="t('student.courses.title')" :subtitle="t('student.courses.subtitle')">
  <template #actions>
    <FilterBar :model-value="filters" @update:model-value="onFiltersChange" tint="secondary" />
  </template>
</PageHeader>
```

Card + Tint（主要内容区统一玻璃卡）：

```vue
<Card tint="primary" class="mb-6">
  <template #title>{{ t('student.assignments.detail.title') }}</template>
  <div class="space-y-4">
    <!-- 内容 -->
  </div>
</Card>
```

---

## 🧩 核心模块详解

以下是根据“后端即真理”原则重构后的主要功能模块和视图。

### 1. 认证 (Auth) & 用户 (User)
-   **视图**:
    -   `LoginView.vue`: 用户登录。
    -   `RegisterView.vue`: 用户注册 (学生/教师)。
    -   `ForgotPasswordView.vue`: 找回密码流程。
    -   `ProfileView.vue`: 查看和更新用户个人资料、修改密码。
-   **Store**: `useAuthStore` 负责管理用户身份验证状态、Token 和当前用户信息。
-   **API**: `auth.api.ts`, `user.api.ts`

### 2. 教师功能 (Teacher)
-   **视图**:
    -   `DashboardView.vue`: 教师仪表盘，提供课程和学生分析的概览。
    -   `ManageCourseView.vue`: 教师创建、查看、编辑和管理自己的课程。
    -   `CourseStudentsView.vue`: 查看选修某门课程的学生列表。
    -   `GradeAssignmentView.vue`: 为学生提交的作业进行评分。
    -   `StudentAnalyticsView.vue`: 查看学生群体的整体能力分析。
    -   `StudentDetailView.vue`: 查看单个学生的详细成绩报告。
-   **Stores**: `useTeacherStore`, `useCourseStore`, `useGradeStore`
-   **API**: `teacher.api.ts`, `course.api.ts`, `grade.api.ts`

#### AI 批改与历史（TeacherAIGradingView / TeacherAIGradingHistoryView）

- 入口：教师端顶部“AI 批改作业”与“历史”按钮（支持面包屑携带 `courseId` 上下文）
- “从提交选择”弹层：
  - 第 1 个选择器：按当前 `courseId` 加载作业（`assignmentApi.getAssignmentsByCourse(courseId)`）
  - 第 2 个选择器：按所选作业加载提交（`submissionApi.getSubmissionsByAssignment(assignmentId)`），可预览
  - 可选择“以文本内容”或“以提交文件”加入左侧队列
- 批改过程：顶端显示动态进度条（`completed/total`），移除旧的顶层进度条
- 导出：
  - 文本导出同“AI 批改作业”页面
  - 在历史详情弹窗新增“导出 PNG / PDF”，PDF 为单页长图（非分页）
- 历史列表：
  - 移除 ID 列；分页与全局一致（左侧 page-size 玻璃选择器：10/20/50；右侧 Prev/Next 中间显示 `Page N` 且走 i18n）
  - “查看/删除”按钮带图标并 i18n；删除联动后端 `DELETE /api/ai/grade/history/{id}`（兼容 POST）
- 详情弹窗：
  - 标题改为文件名（无则回退为“记录 #ID”）
  - 内容区限制高度 `max-h-[85vh] md:max-h-[80vh]` 且内部滚动并隐藏滚动条
  - 右上角关闭改为 X 图标

相关 API：
```ts
// aiGrading.api.ts（强制 jsonOnly + useGradingPrompt）
gradeEssay(data)
gradeEssayBatch(data[])
gradeFiles({ fileIds })
listHistory({ q, page, size })
getHistoryDetail(id)
deleteHistory(id) // 失败兜底 POST /delete

// assignment.api.ts
getAssignmentsByCourse(courseId, { page, size })

// submission.api.ts
getSubmissionsByAssignment(assignmentId, { page, size })
```

### 3. 学生功能 (Student)
-   **视图**:
    -   `DashboardView.vue`: 学生仪表盘，展示近期活动、课程进度和能力概览。
    -   `CoursesView.vue`: 浏览所有可用课程并进行选课。
    -   `CourseDetailView.vue`: 查看课程详情、课时列表和学习内容。
    -   `AssignmentSubmitView.vue`: 查看作业详情并提交作业。
    -   `GradesView.vue`: 查看所有已评分课程的成绩和反馈。
    -   `AbilityView.vue`: 查看个人多维度能力分析、目标和学习建议。
-   **Stores**: `useStudentStore`, `useAbilityStore`, `useSubmissionStore`
-   **API**: `student.api.ts`, `ability.api.ts`, `submission.api.ts`

### 4. 社区 (Community)
-   **视图**:
    -   `CommunityView.vue`: 社区主页，展示帖子列表、热门话题和活跃用户，支持分类和搜索。
    -   `PostDetailView.vue`: 查看帖子详情、评论列表，并参与讨论。
-   **Store**: `useCommunityStore` 负责管理帖子、评论、社区统计等所有状态。
-   **API**: `community.api.ts`

---

## 🏛️ 架构核心

### API 层 (`src/api`)
此目录下的每个文件都对应后端的一个 `Controller`，提供类型安全的方法来调用API。这种一对一的映射关系使得追踪数据来源和调试API调用变得非常简单。
-   `auth.api.ts` -> `AuthController`
-   `course.api.ts` -> `CourseController`
-   `community.api.ts` -> `CommunityController`
-   ...等等。

### 状态管理层 (`src/stores`)
Pinia stores 是应用的“大脑”，负责处理业务逻辑、调用API和管理全局状态。它们是组件与API之间的桥梁。
-   `useAuthStore`: 管理用户认证和个人信息。
-   `useUIStore`: 管理全局UI状态，如加载指示器和通知。
-   `useCourseStore`: 管理课程列表、详情和相关操作。
-   `useCommunityStore`: 管理社区帖子、评论和互动。
-   ...每个核心功能都有其专属的store，实现了业务逻辑的内聚。

### 类型定义层 (`src/types`)
此目录包含了所有与后端数据实体 (Entity) 和数据传输对象 (DTO) 严格匹配的TypeScript接口。这确保了从API响应到组件渲染的整个链路都是类型安全的，极大地减少了运行时错误。

### 通用工具 (`src/utils`)
-   `api-handler.ts`: 提供了一个名为 `handleApiCall` 的封装函数。它统一处理了API请求中的 `loading` 状态管理、成功/错误通知的显示，简化了Store中的异步操作代码，避免了大量的重复 `try...catch` 块。

### 联调要点

- Axios `baseURL` 在开发模式会打印日志，便于确认代理是否生效。
- 登录成功后会存储 token；401 时自动清理并跳转登录。
- 与后端保持“后端即真理”一致：任何前端操作都以现有 API 能力为准。

### API 文档索引

- 前端 API 索引：`../docs/frontend/api/index.md`
- 后端 API 索引：`../docs/backend/api/index.md`

### 性能与可访问性规范（摘要）

- 性能：
  - 组件懒加载（路由 `component: () => import(...)` 已默认使用）
  - 列表分页与虚拟滚动（长列表考虑 `v-virtual-scroll` 或按页加载）
  - 图片：使用合适尺寸与懒加载；避免无必要的 base64 内联
  - 避免在 `watch` 中做重计算；复杂计算用 `computed` 与 memoization
- 可访问性（a11y）：
  - 表单控件提供 `label`/`aria-label`，组件内加可聚焦状态
  - 键盘可达：弹层与抽屉提供 `Esc` 关闭与 `Tab` 循环焦点
  - 颜色对比：遵守文本与背景对比度（WCAG AA），尽量使用主题语义色

---

## 🔧 调试与常见问题
- 开发模式会在控制台打印 `Axios baseURL`，用于确认请求根路径。
- 若出现 401，将自动清理本地 token 并跳转到登录页（见 `src/api/config.ts`）。
- 若跨域或接口 404，请检查：
  - 后端是否运行在 `http://localhost:8080` 且 `server.servlet.context-path=/api`
  - `.env.development` 中 `VITE_API_BASE_URL` 是否正确
  - `vite.config.ts` 的 `/api` 代理是否生效

---
## ✅ 代码质量与规范

项目配置了完整的工具链以保证代码质量。
-   **ESLint**: 用于静态代码分析，发现潜在的错误和不符合规范的代码。配置文件为 `.eslintrc.cjs`。
-   **Prettier**: 用于代码格式化，确保整个项目代码风格的一致性。配置文件为 `.prettierrc.json`。
-   **TypeScript**: 项目完全使用TypeScript编写，并在`tsconfig.json`中开启了严格模式，以获得最强的类型检查。
-   **Husky & lint-staged** (推荐配置): 可以在`package.json`中配置`pre-commit`钩子，在每次提交前自动运行 `lint` 和 `type-check`，从源头保证入库代码的质量。

---

## 🎆 动态背景（Legacy）

动态背景仅在 Legacy 兼容模式下保留，默认不启用；如需使用，请参考：`docs/frontend/ui-backgrounds.md`。
