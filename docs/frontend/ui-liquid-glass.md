# LiquidGlass（液态玻璃）材质说明

本文档说明项目内 `LiquidGlass`（`frontend/src/components/ui/LiquidGlass.vue`）的参数含义、推荐用法，以及“无折射（仅遮蔽+模糊）”的配置建议。

## 背景与目标

项目当前存在两类“玻璃”：

- **CSS 玻璃（v-glass / glass tokens）**：基于 `backdrop-filter: blur(...)`，统一由 `frontend/src/styles/glass.tokens.postcss` 提供强度档位与主题适配。
- **LiquidGlass（液态玻璃）**：为了实现更强的“液态/折射”效果，内部使用 SVG filter 的 `feDisplacementMap` 对背景进行位移扭曲。

在某些场景（例如 **底部 DockBar**、**顶部/弹层 Popover**），希望更接近苹果官网的表现：**只有遮蔽+背景模糊，不产生折射/扭曲**，以提升可读性与稳定感。

## 关键参数说明

### `effect`

`effect` 用于选择材质模式：

- **`refract`（默认）**：折射/位移模式。通过 `backdrop-filter: url(#displacementFilter)` + `feDisplacementMap` 产生“液态折射”。
- **`occlusionBlur`**：遮蔽+模糊模式。**不做位移扭曲**，改为使用 token 化的 `blur/saturate/contrast`，更接近苹果官网玻璃。

推荐：

- **DockBar / Tooltip / Popover / 顶栏菜单**：优先使用 `occlusionBlur`。
- **装饰性强、可容忍扭曲的卡片/按钮背景**：可继续使用默认 `refract`。

### `frost`

遮蔽层强度（越大越“发白/发黑”），用于控制“挡住背景”的感觉。

推荐值（经验值，按项目现有观感）：

- **DockBar**：`0.12 ~ 0.16`（建议从 `0.14` 开始）
- **Tooltip / 小型 Popover**：`0.05 ~ 0.10`
- **大弹层 / 菜单**：`0.10 ~ 0.16`

### `radius`

圆角半径（px）。Dock/药丸形态一般用较大值（如 28/30），弹层通常 12~16。

### `tint` / `tintFrom` / `tintTo` / `blend`

用于位移贴图的颜色渐变与混合模式（主要影响 `refract` 的观感）。在 `occlusionBlur` 下通常建议关闭 `tint`（避免出现与品牌无关的彩色折射感）。

## 推荐 preset（直接可用）

### DockBar（底部 Dock）

目标：明显遮蔽 + 背景模糊，无折射。

建议：

- `effect="occlusionBlur"`
- `:frost="0.14"`
- `:tint="false"`

（Dock 内部按钮/元素保持透明，避免盖掉遮蔽层）

### Tooltip（提示）

目标：轻遮蔽 + 模糊，无折射，文本清晰。

建议：

- `effect="occlusionBlur"`
- `:frost="0.06"`

### 顶栏菜单（主题/背景/用户菜单等）

目标：遮蔽更强一点，避免内容透底影响可读性。

建议：

- `effect="occlusionBlur"`
- `:frost="0.14"`
- 其它参数（alpha/blur/scale）可以继续保留现状（它们在 occlusionBlur 下不会引入位移折射）

## 设计与实现注意事项

- **不要对根容器强制 `background: transparent !important`**：这会把遮蔽层完全抹掉，导致只剩“折射/模糊”，最终变成“看不清”。\n- 如果需要让内容层透明，只对内部元素（如 `.slot-container` 或子元素）做透明覆盖。\n- `occlusionBlur` 的 blur/saturate/contrast 复用 `glass.tokens.postcss` 的变量，保证全站一致性与可维护性。

