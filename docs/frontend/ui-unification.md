## UI 统一改造说明（选择器/过滤器/搜索框药丸化，容器圆润一致）

本次改造目标：
- 学生端与教师端的所有选择器、过滤器、搜索框统一为药丸形态；
- 容器卡片、输入类控件的圆角与玻璃风格与学生端一致；
- 样式集中在 `/ui` 并可复用，禁止各页面自定义分叉样式。

### 新增样式与入口
- 新文件：`frontend/src/components/ui/pill.postcss`
- 全局引入：`frontend/src/styles/main.postcss` 中添加
  `@import '../components/ui/pill.postcss';`

### 提供的工具类
- `.ui-pill`：基础药丸圆角
- `.ui-pill--input`：药丸输入/触发器的通用样式（玻璃、交互、边框、占位符）
- `.ui-pill--select`：下拉触发器（等同于 input，但 text-left）
- `.ui-pill--sm` / `.ui-pill--md`：尺寸高度
- `.ui-pill--pl` / `.ui-pill--pr-select`：左右内边距（含右侧为下拉箭头预留）
- `.ui-popover-menu`：下拉/弹层玻璃容器（圆角 2xl）
- `.ui-chip`：多选值展示的小圆角标签
- `.ui-filter-bar`：过滤条外层容器（等同 `filter-container` 且圆角 full）

### 组件改造点
- 选择器：`GlassSelect.vue`、`GlassMultiSelect.vue`、`GlassPopoverSelect.vue`
  - 触发按钮统一使用：`class="ui-pill--select ui-pill--pl ui-pill--pr-select"` + 尺寸 `ui-pill--sm|md`
  - 下拉容器统一：`class="ui-popover-menu ..."`
  - 多选值 chip：`class="ui-chip"`
- 过滤条：`FilterBar.vue`
  - 外层：`class="ui-filter-bar ..."`
- 搜索框：`GlassSearchInput.vue`
  - 输入框：`class="ui-pill--input ui-pill--pl ui-pill--sm|md"`

- 徽章：`Badge.vue`
  - 统一玻璃风格：组件内部使用 `glass-ultraThin glass-interactive border`，暗黑/明亮模式自动适配
  - 语义颜色（variant）：`primary|secondary|success|warning|danger|info`
  - 大小（size）：`sm|md|lg`
  - 使用建议：状态、分类、等级、优先级等统一用 `Badge`，禁用自定义 `rounded-full` 标签

### 教师端视图替换
- 将原生 `<input>` 搜索改用 `GlassSearchInput.vue`
- 将原生 `<select>` 改为 `GlassSelect` 或 `GlassPopoverSelect`（或至少使用 `.ui-pill--select` 类）
- 移除局部 `.card` 覆盖，统一使用全局卡片玻璃风格
 - 状态徽章/标签统一替换为 `Badge`（例如作业状态、表内小圆标）

### 社区颜色映射
- 分类到 `Badge` 的颜色：
  - study → primary
  - help → warning
  - share → success
  - qa → info
  - chat/其他 → secondary

### 使用示例
```vue
<GlassPopoverSelect v-model="value" :options="opts" size="sm" class="w-48" />

<GlassSearchInput v-model="q" :placeholder="t('common.search')" size="sm" class="w-64" />

<select v-model="cat" class="ui-pill--select ui-pill--pl ui-pill--md ui-pill--pr-select">
  <option value="">请选择</option>
  <option v-for="c in cats" :key="c" :value="c">{{ c }}</option>
  </select>
```

### 注意事项
- 所有新样式必须复用 `/ui` 提供的工具类，禁止在页面内写死 `rounded-*`/`border-*` 与玻璃参数；
- 暗黑模式边框与背景由全局变量控制，不要在页面内自定义；
- 交互反馈（hover/active）由 `.glass-interactive` 与组件内部处理，无需重复。
 - 页面内仍出现 `input input--glass`、原生 `<select>`、手写 `rounded-full` 圆角标签等，应替换为 `/ui` 组件或 `.ui-pill--*`/`Badge`。

### 变更影响
- 视觉与交互统一，提高一致性与可维护性；
- 少量模板类名变更，不影响接口与业务逻辑；
- 如有第三方控件嵌入，需包一层容器以应用 `.ui-pill--input`。
- 徽章统一后，暗黑模式对比度提升，减少了页面内散落的自定义类。
# 前端 UI 统一规范

> 版本：v0.2.1（与 frontend/package.json 对齐）

## 目标
- 统一按钮风格为药丸形，所有交互按钮均带图标与明确底色。
- 统一玻璃容器圆角为 `rounded-2xl`，教师端与学生端风格一致。
- 统一菜单项与弹出层：使用统一的玻璃强度与圆角，交互反馈一致。

## 按钮（Button）
- 组件：`@/components/ui/Button.vue`
- 形状：药丸形（`rounded-full`）
- 图标：
  - 推荐使用 `icon` 属性：`plus|edit|delete|save|confirm|cancel|download|arrow-left|arrow-right|close|search|key|user-plus`。
  - 可使用具名插槽 `#icon` 和 `#suffix` 自定义图标位置。
- 颜色（variant）：
  - 主操作：`primary`
  - 次操作：`secondary`
  - 危险操作：`danger`
  - 信息类：`info|teal|indigo|purple`（按场景）
  - 菜单项：`menu`（左对齐、轻量交互，适合下拉菜单）
- 尺寸（size）：`xs|sm|md|lg|xl`，默认 `md`
- 链接场景：使用 `as="a"`，同时保留 `href`。
- 禁用原生 `<button>` 与任何 `.btn` 类，必须使用 `Button`。

示例：
```vue
<Button variant="primary" icon="confirm">提交</Button>
<Button variant="danger" size="sm" icon="delete">删除</Button>
<Button as="a" href="/path" variant="secondary" icon="arrow-right">查看</Button>
```

## 玻璃容器（Card / GlassModal）
- 统一圆角：`rounded-2xl`
- 组件：
  - 卡片容器：`@/components/ui/Card.vue`（默认 `rounded-2xl`、可控 padding、可选 `hoverable`）
  - 弹窗容器：`@/components/ui/GlassModal.vue`（框体 `rounded-2xl`，关闭按钮复用 `Button`）
- 规则：
  - 学生端与教师端的所有主要容器统一 `rounded-2xl`。
  - 若页面已有自定义玻璃块，优先替换为 `Card.vue`，否则保证圆角为 `rounded-2xl`。

## 布局与菜单
- 顶部图标按钮统一为 `Button` 的 `glass` 变体，`size="sm"`，通过图标插槽渲染 heroicons。
- 下拉菜单/面板：容器 `rounded-2xl`，菜单项使用 `Button` 的 `menu` 变体。

## 改造约束
- 不得开发使用原生 `<button>`、`.btn` 或非 `Button` 的按钮实现。
- 不得使用非 `rounded-2xl` 的主要容器圆角。特殊极小组件（如日历单元格）除外。
- 新增页面与组件必须遵循本规范。

## 更新记录
- v0.2.1：
  - 实时通知改为浅色玻璃药丸（`App.vue`）。
  - 通知下拉容器统一浅色玻璃与圆角 2xl；下拉内按钮为药丸玻璃（`NotificationBell.vue`）。
  - 聊天抽屉去除变暗/模糊遮罩，抽屉容器改为浅色玻璃，与 `GlassModal` 一致；底部输入改为 `GlassTextarea`（`ChatDrawer.vue`）。
  - 语言切换弹层内菜单项使用原生按钮（轻量），触发按钮为浅玻璃药丸（`LanguageSwitcher.vue`）。
  - 统一搜索输入：学生端“我的作业”、教师端“管理课程”等使用 `GlassSearchInput`。
- v0.2.0：统一按钮为药丸形+图标；统一容器圆角为 `rounded-2xl`；布局与菜单统一；替换遗留原生按钮。
