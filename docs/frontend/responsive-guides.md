---
title: 前端适配与首页交互说明
---

# 前端适配与首页交互说明

## 首页模块结构（Home）

- 入口：`/` → `PublicLayout` + `Home*` 多页面（概览/特性/对比/版本/历程/结构/走马灯）
- 国际化：文案位于 `frontend/src/locales/*/app.json` 的 `app.home.*`
- 品牌统一：顶部 Dock 集成语言、主题、光标、Docs、GitHub、登录入口，复用 `/ui` 玻璃样式
- 组件与特效（参考 Inspira UI 示例）：
  - 文本滚动显现（Text Scroll Reveal）用于英雄区副标题
  - 流星（Meteors）用于卡片区背景点缀
  - Aurora 背景用于首屏淡化底色
  - Apple Card Carousel 展示六大模块卡片
  - Compare 展示学生端/教师端、明暗主题与玻璃风格对比
  - Timeline/Tracing Beam 展示版本更迭与开发历程
  - FileTree 展示 backend/frontend/docs 三棵根目录简版

## 首页标题与画廊

- 中文标题在小屏开启换行，避免被右侧画廊遮挡：
  - 组件：`frontend/src/components/ui/inspira/LetterPullup.vue`
  - 视图：`frontend/src/features/shared/views/HomeView.vue`
- 右侧 `BendingGallery` 的拖动范围限制在组件内部，避免影响标题区域。

## Sticky 区块在小屏的高度

- 将 `260vh` 在小屏降为 `160~180vh`，减少滚动距离并提升可读性。

## 顶部昵称与头像

- 默认状态完整显示头像与昵称，不依赖 Dock 放大；昵称超长时建议在业务上限制长度。

## 登录与验证邮箱

- 登录按钮采用药丸形态；验证邮箱页使用 `LiquidGlass` 容器，按钮统一样式。

