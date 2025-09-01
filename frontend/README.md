# 学生非核心能力发展评估系统 - 前端

## 📖 项目概述

本项目是“学生非核心能力发展评估系统”的前端实现，采用 Vue 3、TypeScript 和 Pinia 构建。它是一个严格遵循其后端API的单页面应用 (SPA)。

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
-   **UI 框架**: [Tailwind CSS](https://tailwindcss.com/)
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

---

## 🧪 联调与排错（三步）

1) baseURL：控制台打印 `Axios baseURL` 是否为 `http://localhost:8080/api`
2) 代理：开发模式访问 `/api/...` 是否正确代理到后端 8080
3) 后端：`/api/swagger-ui.html` 可打开；端点可在 Swagger 中直接成功

---

## 🧭 项目状态
- 教师端：已完成
- 学生端：开发中（Beta）

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

## 🎆 背景组件 FuturisticBackground 使用

FuturisticBackground 是统一的动态背景组件，默认在三大布局中启用：
- AuthLayout：默认开启强调模式（emphasis=true）、交互（拖影/涟漪）。
- StudentLayout / TeacherLayout：默认关闭强调，启用轻量交互。

基础用法：

```vue
<FuturisticBackground
  class="absolute inset-0"
  theme="auto"
  :intensity="0.18"
  :parallax="true"
  :enable3D="true"
  :logo-glow="true"
  :emphasis="false"                
  :interactions="{ mouseTrail: true, clickRipples: true }"
  :enabled="true"                  
  :respect-reduced-motion="true"   
/>
```

关键 props：
- theme: 'auto' | 'light' | 'dark'（自动跟随页面主题）
- emphasis: boolean（强调模式，粒子数=1700；否则=1000）
- interactions: { mouseTrail?: boolean; clickRipples?: boolean }（是否显示鼠标拖影与点击涟漪）
- enabled: boolean（全局开关，可通过 UI 顶栏“眼睛”按钮控制）
- respectReducedMotion: boolean（遵从系统“减少动画”偏好；true 时仅绘制静态背景）

说明：
- 浅/深色模式自动切换，浅色下拖影与涟漪颜色更柔和；深色下使用更显性的发光混合。
- 鼠标移动会产生漩涡与散开，按住左键则强化漩涡；点击触发涟漪。
- 粒子支持多形状（圆、三角、正方、六边、五角星）并随速度方向旋转，带流星尾迹。

在三大布局中集成（示例）：

```vue
<!-- AuthLayout.vue（强调开启） -->
<FuturisticBackground
  class="absolute inset-0"
  theme="auto"
  :intensity="0.18"
  :parallax="true"
  :enable3D="true"
  :logo-glow="true"
  :emphasis="true"
  :interactions="{ mouseTrail: true, clickRipples: true }"
  :enabled="uiStore.bgEnabled"
  :respect-reduced-motion="true"
/>

<!-- StudentLayout.vue / TeacherLayout.vue（强调关闭） -->
<FuturisticBackground
  class="fixed inset-0 z-0 pointer-events-none"
  theme="auto"
  :intensity="0.18"
  :parallax="true"
  :enable3D="true"
  :logo-glow="true"
  :emphasis="false"
  :interactions="{ mouseTrail: true, clickRipples: true }"
  :enabled="uiStore.bgEnabled"
  :respect-reduced-motion="true"
/>
```

UI 全局开关（已内置）：
- 顶栏“眼睛”按钮切换背景开关（持久化到 localStorage）。
- 主题切换按钮切换浅/深色主题（持久化到 localStorage）。
