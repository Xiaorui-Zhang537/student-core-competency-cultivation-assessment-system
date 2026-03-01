---
title: 帮助中心（Help Center）
description: 帮助中心页面结构、接口对接与国际化落点
outline: [2, 3]
---

# 帮助中心（Help Center）

帮助中心前端位于 `frontend/src/features/shared/views/HelpView.vue`，遵循“后端即真理”。

## 功能点与接口对齐

- 顶部四张 StartCard（玻璃风格）：
  - 分类总数：`GET /api/help/categories`
  - 热门文章数：`GET /api/help/articles?sort=hot`
  - 我的工单数：`GET /api/help/tickets`（登录态）
  - 近 7 日新增文章：基于 `createdAt` 在前端统计（若后端提供聚合端点，应迁移为后端统计）
- 帮助文章：
  - 列表：按分类筛选、按“最新/最热”排序
  - 详情：支持“有帮助/没帮助”反馈：`POST /api/help/articles/{id}/feedback`
- FAQ：
  - 静态内容，但需要 i18n：`shared.help.faq.*`
- 使用指南：
  - 提供学生/教师关键步骤引导
  - 页首提供 “查看完整文档” CTA（跳转文档站 `/docs`）
- 技术支持与工单：
  - 联系方式来自 i18n/配置（示例：电话 `15362612345`、邮箱 `xiaorui537@gmail.com`）
  - 提交工单：`POST /api/help/tickets`
  - 我的工单：`GET /api/help/tickets`

## 已移除（后端无对应能力）

- 视频教程分区（暂无后端视频接口）
- 系统状态分区（暂无后端状态接口）

## 国际化（i18n）

- 相关词条位于：
  - `frontend/src/locales/zh-CN/shared.json`
  - `frontend/src/locales/en-US/shared.json`
- 命名空间：`shared.help.*`

