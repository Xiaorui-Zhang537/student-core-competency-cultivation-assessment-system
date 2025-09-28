# 前端文档索引

- 指南
  - [交互与页面序列](./page-sequences.md)
  - [i18n 示例](../i18n-examples.md)
- API 约定
  - [前端 API 索引](./api/index.md)
- 组件与样式
  - [UI 统一规范](./ui-unification.md)
  - UI 变更摘要：本次引入药丸样式与玻璃徽章（Badge），并统一全局选择器/搜索/过滤器到 `/ui` 组件；详情见“UI 统一规范”。
  - v0.2.1：实时通知/通知下拉/聊天抽屉/语言切换/搜索输入统一为浅色玻璃（药丸），与 GlassModal 保持一致风格。
  - v0.2.2：学生作业提交页改为两行双列布局；新增 `features/shared/AssignmentInfoCard.vue`、`features/shared/AttachmentList.vue`；统一使用 `components/ui/Card.vue`；i18n 新增 `student.assignments.publish`。
  - v0.2.3：聊天抽屉层级上调为 `z-[12000]`，并新增 `Esc` 关闭；顶部按钮显式传入 `$event` 以确保点击拦截。
  - [图表主题](../ui-chart-theme.md)
  - [错误/空状态规范](../ui-error-empty-spec.md)

