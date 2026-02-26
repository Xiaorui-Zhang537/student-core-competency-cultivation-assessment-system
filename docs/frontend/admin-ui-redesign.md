## 管理员端 UI 重构说明（聚合控制台）

> 对应前端版本：`frontend@0.3.7`  
> 目标：减少“页面分区过多”的割裂感，把管理员常用能力聚合到更少入口，并统一玻璃主题容器、选择器样式与图表风格。

### 1. 信息架构（新入口）

管理员端收敛为 **5 个入口**（底部 Dock 同步收敛）：

- **控制台**：KPI + 长趋势图 + 分布图 + 雷达/AI使用可视化（`/admin/console`）
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
- **展示**：折线面积图（统一 x 轴为最近 N 天，缺失天补 0）；控制台默认 180 天并使用加长画布

#### 4.3 控制台新增全局可视化

- **五维能力雷达**
  - 接口：`GET /api/admin/dashboard/ability-radar-overview?days=N`
  - 字段：`dimensions[]`（`code/name/value/sampleSize`）
  - 展示：全局五维平均能力结构（RadarChart）
- **AI 使用排行（访问数口径）**
  - 接口：`GET /api/admin/dashboard/ai-usage-overview?days=N&limit=20`
  - 字段：`summary + users[]`
  - 展示：按用户消息数排行（柱状图 + Top 列表），不使用 token 真值

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

### 7. 2026-02 管理员数据中心二期（本次实现）

- **布局调整**：`/admin/people` 改为 `KPI 在上 -> 搜索筛选在下`，并将 `Tabs` 并入搜索容器，放置在搜索框前。
- **操作交互统一**：管理员列表操作改为三点菜单（与教师端学生管理交互一致），支持：查看详情（学生/教师）、聊天、AI/口语审计、重置密码（用户 Tab）。
- **信息密度增强**：列表行新增姓名组合、邮箱验证、创建时间等字段展示；保持单次分页接口，不引入逐行详情请求（避免 N+1）。
- **详情页模板复用**：新增 `AdminPersonProfileSummary` 作为管理员学生/教师详情顶部共享骨架，统一个人信息字段与聚合指标展示。
- **教师端新增页面**：新增 `/teacher/profile/basic`（教师基本信息详情页，只读），沿用学生详情页风格模板；并在 `/teacher/profile` 增加入口按钮。

### 8. 2026-02 行为洞察历史报告补齐（管理员）

- **后端历史接口**：新增管理员行为洞察历史分页与详情接口：
  - `GET /api/admin/behavior/insights/history`
  - `GET /api/admin/behavior/insights/history/{id}`
- **查询维度**：按 `studentId + courseId(可空) + range(7d/30d/180d/365d)` 查询，返回倒序历史记录，支持分页。
- **详情数据**：详情接口返回标准 `BehaviorInsightResponse`，并补齐 `meta.generatedAt/model/promptVersion/status`，用于前端直接渲染历史报告。
- **前端入口**：`BehaviorInsightSection` 在管理员模式新增“历史洞见报告”按钮，支持列表浏览与详情查看。
- **兼容策略**：保留现有“最新报告/生成报告”流程不变，仅新增历史读取能力，不影响学生/教师端当前行为。

### 9. 2026-02 学生详情数据后端化（管理员）

- **详情接口增强**：`GET /api/admin/people/students/{id}` 新增可选参数：
  - `courseId`：按课程过滤最近学习事件
  - `eventLimit`：最近学习事件返回条数（默认 8）
- **新增聚合返回字段**：
  - `courses[]`：课程列表（含 `progress/status/lastAccessTime`）
  - `recentEvents[]`：统一事件流（lesson/submission/ai/community_ask/community_answer/visit）
- **前端切换**：管理员学生详情页课程筛选与“最近学习”优先使用上述后端聚合数据，不再依赖前端从多接口拼接事件。

### 10. 2026-02 洞察报告“最近一次优先”策略

- **展示策略调整**：行为洞察默认优先展示“最近一次可用报告”，不再因为当前行为时间窗无数据而显示空白。
- **后端兜底顺序**：`snapshot命中` → `student+course+range` → `student+course(不限range)`。
- **报告上下文补充**：洞察响应 `extra.reportContext` 新增报告关联信息：
  - 生成所依据的 `snapshotId/range/from/to/inputEventCount`
  - 当次行为统计（AI提问/追问、作业提交/重交、社区问答、反馈查看）
- **前端展示增强**：`BehaviorInsightSection` 顶部新增“报告信息”区块，展示生成时间与当次行为摘要，便于解释“这份报告基于哪次行为数据生成”。
- **教师/管理员时间窗补齐**：在学生详情页课程筛选旁新增“行为时间窗”选择器（7d/30d/180d/365d），统一驱动行为证据与洞察组件加载口径。

