# 主题与 UI 指南（DaisyUI + Glass）

本指南说明本项目前端的主题体系（浅色 retro / 深色 synthwave）、彩色玻璃（Glass Tint）用法与开发约定，确保品牌与视觉一致性。

---

## 1. 主题总览
- **新版主题**：
  - 浅色：`retro`（米黄色 `base-100` 纯色底）
  - 深色：`synthwave`
- **旧版主题（Legacy）**：沿用历史配色与动态背景，仅供兼容。

切换机制：
- 顶栏右上角“主题选择”菜单切换（新版/旧版 + 明暗）。
- Pinia：`useUIStore` 负责 `themeName`（retro/synthwave/...）、`themeFamily`（new/legacy）与暗黑状态。
- DOM：`<html>` 同时使用 `class="dark"` 与 `data-theme="retro|synthwave|..."`。

---

## 2. 全局样式与变量
- 基于 TailwindCSS + DaisyUI（已在 `tailwind.config.js` 集成）。
- DaisyUI 语义变量：`--color-primary`、`--color-secondary`、`--color-accent`、`--color-base-100`、`--color-base-content` 等。
- 全局映射：在 `src/styles/main.postcss` 将项目自有 tokens 映射到 DaisyUI 变量；`html, body, #app` 的背景固定使用 `--color-base-100`（retro 下为米黄色）。

建议：
- 组件内不要硬编码颜色；优先使用语义类或 `var(--color-*)`。
- 文本弱化请用 `.text-muted` / `.text-subtle` 等已定义的工具类。

---

## 3. 彩色玻璃（Glass Tint）
- 工具类：`glass-tint-{primary|secondary|accent|info|success|warning|error}`。
- 只在“新版主题”（retro/synthwave）下生效；旧版主题保持原外观。
- 用法示例：
```vue
<div class="glass-regular glass-tint-secondary rounded-2xl" v-glass>
  ...
</div>
```
- 推荐分配：
  - 筛选/导航区：`secondary`
  - 强提示/空状态：`info`
  - 主要内容卡：`primary`/`accent`
  - 成功/警告/危险：`success` / `warning` / `error`

---

## 4. 组件与页面约定
- 统一使用 `frontend/src/components/ui` 下的组件（Button、Card、Badge、Modal、Inputs、PageHeader、Progress 等）。
- 页面大卡：优先使用 `Card`，必要时加 `tint` 属性（如：`<Card tint="primary">`）。
- 输入类：使用 `GlassInput`/`GlassTextarea`/`GlassSearchInput`，避免直接写原生 `input/textarea` 样式。
- 过滤条：使用 `FilterBar`，在新版主题建议加 `tint="secondary"`。

---

## 5. 背景与动效
- 新版主题：静态米黄色底（retro），不启用动态背景。
- 旧版主题：动态背景组件 `FuturisticBackground` 保持启用。

---

## 6. 开发清单（必做）
- 新增页面：
  1) 根容器不要覆盖全局 `base-100` 背景；
  2) 主分区用 `Card` 或 `glass-*` + `glass-tint-*`；
  3) 按语义替换硬编码颜色（如蓝/灰）为 `--color-*` 或 DaisyUI 类；
  4) 文案走 i18n；
  5) 复用 `/ui` 组件，必要时在 `/ui` 扩展样式能力（而非页面内重复实现）。

- 变更现有页面：
  - 移除 `bg-*-50`、`bg-gradient-*` 等会盖住全局底的类；
  - 按上面“推荐分配”为主要分区加 `tint`。

---

## 7. 故障排查
- “背景还是蓝色/纯白”：
  - 检查是否仍渲染了 `FuturisticBackground`（新版主题应禁用）；
  - 检查根容器是否有 `bg-*` 或渐变覆盖；
  - 检查 `<html data-theme>` 是否为 `retro`/`synthwave`，以及 `--color-base-100` 是否可用。

- “颜色没跟随主题变动”：
  - 是否使用硬编码颜色；
  - 是否遗漏了 `tint` 或使用了未映射的局部变量。

---

## 8. 版本与变更
- 本次主题改造版本：前端 `v0.3.0`。
- 详见 `docs/README.md` 的“变更记录（Changelog）”。
