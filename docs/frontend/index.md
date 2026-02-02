## 帮助中心（Help Center）

帮助中心前端位于 `frontend/src/features/shared/views/HelpView.vue`，遵循“后端即真理”。当前功能点：

- 顶部四张 StartCard（玻璃风格）：
  - 分类总数：来自 `GET /api/help/categories`
  - 热门文章数：来自 `GET /api/help/articles?sort=hot`
  - 我的工单数：来自 `GET /api/help/tickets`（登录态）
  - 近7日新增文章：基于 `createdAt` 在前端统计
- 帮助文章：支持按分类筛选、按“最新/最热”排序，文章详情支持“有帮助/没帮助”反馈（`POST /api/help/articles/{id}/feedback`）
- FAQ：静态内容，已 i18n 化（`shared.help.faq.*`）
- 使用指南：提供学生/教师关键步骤引导，并提供跳转或文章筛选引导；页首提供“查看完整文档” CTA（跳转 `/docs`）
- 技术支持与工单：
  - 联系方式来自 i18n/配置，示例：电话 `15362612345`、邮箱 `xiaorui537@gmail.com`
  - 提交工单 `POST /api/help/tickets`，我的工单 `GET /api/help/tickets`

已按“后端即真理”移除：

- 视频教程分区（暂无后端视频接口）
- 系统状态分区（暂无后端状态接口）

国际化：

- 相关词条位于 `frontend/src/locales/zh-CN/shared.json` 与 `frontend/src/locales/en-US/shared.json` 的 `shared.help.*`

# 前端文档索引

- 指南
  - [交互与页面序列](./page-sequences.md)
  - [i18n 示例](../i18n-examples.md)
- API 约定
  - [前端 API 索引](./api/index.md)
- 口语训练
  - [实时语音口语训练](./voice-practice.md)
- 组件与样式
  - [UI 统一规范](./ui-unification.md)
  - UI 变更摘要：本次引入药丸样式与玻璃徽章（Badge），并统一全局选择器/搜索/过滤器到 `/ui` 组件；详情见“UI 统一规范”。
  - v0.2.4：统一选择器/过滤器/搜索器到“学生端-我的作业”基线；课程发现页搜索输入改为 `size="sm"`；教师端相关页下拉改为 `GlassPopoverSelect` 并使用 `tint="info"`。
  - v0.2.1：实时通知/通知下拉/聊天抽屉/语言切换/搜索输入统一为浅色玻璃（药丸），与 GlassModal 保持一致风格。
  - v0.2.2：学生作业提交页改为两行双列布局；新增 `features/shared/AssignmentInfoCard.vue`、`features/shared/AttachmentList.vue`；统一使用 `components/ui/Card.vue`；i18n 新增 `student.assignments.publish`。
  - v0.2.3：聊天抽屉层级上调为 `z-[12000]`，并新增 `Esc` 关闭；顶部按钮显式传入 `$event` 以确保点击拦截。
  - [图表主题](../ui-chart-theme.md)
  - [错误/空状态规范](../ui-error-empty-spec.md)

## 首页模块（Home）

- 入口：`/` → `PublicLayout` + `Home*` 多页面（概览/特性/对比/版本/历程/结构/走马灯）
- 组件与特效（参考 Inspira UI 示例）：
  - 文本滚动显现（Text Scroll Reveal）用于英雄区副标题
  - 流星（Meteors）用于卡片区背景点缀
  - Aurora 背景用于首屏淡化底色
  - Apple Card Carousel 展示六大模块卡片
  - Compare 展示学生端/教师端、明暗主题与玻璃风格对比
  - Timeline/Tracing Beam 展示版本更迭与开发历程
  - FileTree 展示 backend/frontend/docs 三棵根目录简版
- 国际化：文案位于 `locales/*/app.json` 下的 `app.home.*`
- 品牌统一：顶部 Dock 集成语言、主题、光标、Docs、GitHub、登录入口，复用 `/ui` 玻璃样式

