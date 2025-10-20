---
title: 前端适配与首页交互说明
---

# 前端适配与首页交互说明

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


