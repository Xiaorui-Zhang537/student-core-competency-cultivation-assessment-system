## 管理员端 UI 重构说明（聚合控制台）

> 对应前端版本：`frontend@0.3.6`  
> 目标：减少“页面分区过多”的割裂感，把管理员常用能力聚合到更少入口，并统一玻璃主题容器、选择器样式与图表风格。

### 1. 信息架构（新入口）

管理员端收敛为 **5 个入口**（底部 Dock 同步收敛）：

- **控制台**：KPI + 趋势图 + 分布图 + 快捷入口（`/admin/console`）
- **数据中心**：用户/学生/教师合并为同页 Tabs（`/admin/people`）
- **内容治理**：能力报告 / 帖子 / 评论合并为治理台 Tabs（`/admin/moderation`）
- **课程中心**：课程列表 + 概览 KPI + 状态分布（`/admin/courses`）
- **工具中心**：导出能力 + 常用导出卡片（`/admin/tools`）
- **排名**：按课程查看学生表现排行（复用教师端 course students analytics）（`/admin/rankings`）

补充能力（不改变入口结构）：

- **全局通知**：在工具中心提供群发入口（写入每个用户的通知中心）
- **一对一聊天**：在用户列表与学生/教师详情页提供“聊天”入口（复用 ChatDrawer）
- **AI/口语审计**：提供独立审计页（`/admin/audit/:userId`），并在用户列表/详情页提供入口

### 2. 路由兼容（旧入口重定向）

为避免旧链接失效，保留旧路径并做重定向（保留 query 参数）：

| 旧路径 | 新路径 |
| --- | --- |
| `/admin/dashboard` | `/admin/console` |
| `/admin/analytics` | `/admin/console?panel=analytics` |
| `/admin/users` | `/admin/people?tab=users` |
| `/admin/students` | `/admin/people?tab=students` |
| `/admin/teachers` | `/admin/people?tab=teachers` |
| `/admin/reports` | `/admin/moderation?tab=ability` |
| `/admin/community` | `/admin/moderation?tab=posts` |
| `/admin/exports` | `/admin/tools` |

> 学生/教师详情页仍保留：`/admin/students/:id`、`/admin/teachers/:id`，并将返回按钮指向新的数据中心 Tab。

### 3. UI 统一规范（玻璃容器 / 选择器 / Tabs）

- **容器**：统一使用 `components/ui/Card.vue`（`rounded-2xl` + LiquidGlass），并按业务语义使用 `tint`（`info/success/warning/accent/secondary`）形成“主题色分区”。  
- **选择器/搜索**：统一使用 `GlassPopoverSelect`、`GlassSearchInput`（药丸触发器 + 玻璃下拉），禁止页面内自定义圆角与下拉样式分叉。  
- **Tabs**：统一使用 `SegmentedPills`（玻璃超薄底 + 语义色激活态）。  

### 4. 图表（ECharts）与数据来源（后端即真理）

项目已内置 ECharts 主题同步能力：`frontend/src/charts/echartsTheme.ts`。控制台与课程中心的图表均遵循该主题与玻璃 tooltip 规范。

#### 4.1 控制台趋势图

- **接口**：`GET /api/admin/analytics/series/overview?days=N`
- **字段**：`activeUsersDaily/newUsersDaily/enrollmentsDaily`（每项为 `{ day, value }`）
- **展示**：折线面积图（统一 x 轴为最近 N 天，缺失天补 0）

#### 4.2 分布类图表（基于聚合或 totals 统计）

- 用户角色/课程状态：来自 `GET /api/admin/dashboard/overview` 的汇总字段
- 帖子等状态分布：基于分页接口的 `total`（统一以 `size=1` 获取 totals，不拉全量数据）  

### 5. 主要改动文件（便于回溯）

- 路由：`frontend/src/router/index.ts`
- 管理员布局 Dock 收敛：`frontend/src/layouts/AdminLayout.vue`
- 新视图：
  - `frontend/src/features/admin/views/ConsoleView.vue`
  - `frontend/src/features/admin/views/PeopleView.vue`
  - `frontend/src/features/admin/views/ModerationView.vue`
  - `frontend/src/features/admin/views/ToolsView.vue`
- 管理员端复用组件/统计聚合：
  - `frontend/src/features/admin/components/AdminKpiRow.vue`
  - `frontend/src/features/admin/components/AdminTrendsPanel.vue`
  - `frontend/src/features/admin/components/AdminDistributionPanel.vue`
  - `frontend/src/features/admin/components/AdminUserTable.vue`
  - `frontend/src/features/admin/composables/useAdminCounts.ts`

### 6. 后续可选增强（不影响首期）

- 为“更复杂统计”补充后端聚合端点（例如：课程按分类/难度、用户按学院/年级等），减少多次 totals 请求。
- 工具中心增加“导出任务 + 异步生成 + 审计日志列表”闭环（当前后端仅记录审计，不提供列表端点时前端不做臆测实现）。

