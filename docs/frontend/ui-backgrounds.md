### 背景与主题（Legacy 兼容指南）

本项目自 v0.3.0 起采用 DaisyUI 新版主题：浅色 `retro`、深色 `synthwave`，全局背景固定为 `base-100`（米黄色静态底）。动态背景仅在 Legacy 模式下保留，用于兼容历史页面与特定场景展示。

要点：
- 新版主题（推荐）：不启用动态背景；页面主体以玻璃拟态（glass + tint）与 DaisyUI 语义色统一风格。
- Legacy 模式：可启用动态背景组件（如 `FuturisticBackground`），仅用于需要保留历史观感的页面或 Demo。

使用规范：
- 若启用动态背景，务必确保背景层 `pointer-events: none`，避免遮挡交互；根容器不要再覆盖 `base-100`。
- 页面主要视觉请使用 `components/ui` 下的 Card、FilterBar、GlassInputs 等统一样式，不得在页面中重复实现。
- 对新页面与重构页面，一律采用新版主题规范，禁止默认启用动态背景。

相关实现（按代码实际为准）：
- 主题指南：`docs/ui-theming.md`
- 背景组件（若仍启用）：`frontend/src/layouts/AuthLayout.vue`、`StudentLayout.vue`、`TeacherLayout.vue`
- 控制开关：顶栏背景按钮或 UI Store 中的开关（如 `uiStore.bgEnabled`）

常见问题排查：
- “页面还是蓝/白背景”：检查是否有页面根容器覆盖了全局 `base-100`；或 Legacy 背景仍在渲染。
- “滚动/点击被遮挡”：确认背景层使用 `pointer-events: none` 且位于合适层级；必要时降低 z-index。

更多配色与玻璃 Tint 的使用规范，请参考 `docs/ui-theming.md`。
