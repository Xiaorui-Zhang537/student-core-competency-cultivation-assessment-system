# 前端 UI 统一规范（药丸/圆角与玻璃风格）

本规范记录了学生端与教师端在输入控件、搜索/筛选器、选择器等方面的统一视觉规范与使用方式，确保两端风格一致、复用一致。

## 总体风格
- 统一使用 `/frontend/src/components/ui` 下的玻璃风格组件。
- 输入类默认圆角：统一为更圆润的 `rounded-2xl`。
- 搜索、过滤条、选择器（下拉/弹出）、多选：统一“药丸”外观，即触发器/输入框使用 `rounded-full`。
- 弹出层（dropdown/popover）：统一 `rounded-2xl`，并保持玻璃化（`glass-thin`/`glass-regular`）。

## 组件改造要点（已完成）
- FilterBar：容器圆角从 `rounded-lg` 调整为 `rounded-full`。
- 选择器：`GlassSelect`、`GlassPopoverSelect`、`GlassMultiSelect` 触发器统一 `rounded-full`，内部 padding 调整（加左内边距）；弹出层统一 `rounded-2xl`。
- 搜索输入：`GlassSearchInput` 改为 `rounded-full`。
- 日期时间：`GlassDateTimePicker` 触发按钮改为 `rounded-full`。
- 通用输入：`GlassInput`、`GlassTextarea` 圆角统一为 `rounded-2xl`。
- 全局 `.input` 基类圆角从 `rounded-lg` 升级为 `rounded-2xl`。

## 使用建议
- 页面搜索框优先使用 `GlassSearchInput`，或使用基础输入时加上 `input input--glass rounded-full h-10 pl-10` 以保持药丸样式。
- 选择器优先使用 `GlassPopoverSelect`；简单原生 `<select>` 可使用 `GlassSelect`。
- 多选项控件统一使用 `GlassMultiSelect`。标签项自带 `rounded-full` 胶囊样式。
- 过滤工具条统一使用 `FilterBar`，如需紧凑密度可设置 `dense`。

## 国际化
本次仅调整样式，不涉及文案与 API。继续遵循现有 `i18n` 方案。

## 注意事项
- 统一使用 `/ui` 组件；避免在页面直接书写裸 `input/select`，若确有特殊场景需自定义样式，请遵循本规范的圆角与玻璃化类。
- 暗黑模式下保持足够对比度；必要时在组件内部覆盖文字色彩。

## 版本
- 变更日期：2025-09-18
- 版本号：前端样式规范 v1.1（统一药丸/圆角）































