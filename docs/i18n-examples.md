# i18n 文案示例集（中英双语）

面向前端的国际化实践指南：键名约定、语言包结构、插值/复数、组件用法与验收清单。

## 1. 目录与文件组织
```
src/
  locales/
    zh-CN.json
    en-US.json
```

## 2. 键名约定
- 命名规则：`领域.功能.元素`（例：`auth.login.title`、`course.list.emptyTitle`）
- 含变量使用驼峰拼接（例：`file.upload.maxSizeHint`）
- 避免生造缩写；作废键名保留一版兼容期

## 3. 语言包示例
```json
// zh-CN.json
{
  "common": {
    "ok": "确定",
    "cancel": "取消",
    "retry": "重试",
    "create": "新建"
  },
  "empty": {
    "noData": "暂无数据",
    "tryAdjustFilters": "尝试调整筛选条件"
  },
  "auth": {
    "login": {
      "title": "登录",
      "username": "用户名",
      "password": "密码",
      "submit": "登录"
    }
  },
  "course": {
    "list": {
      "title": "课程列表",
      "emptyTitle": "没有找到相关课程"
    }
  },
  "error": {
    "unauthorized": "未登录或登录已过期",
    "forbidden": "没有权限执行该操作",
    "rateLimited": "请求过于频繁，请稍后再试",
    "server": "服务器开小差了，请稍后再试"
  },
  "file": {
    "upload": {
      "maxSizeHint": "文件大小不能超过 {size}MB",
      "typeNotAllowed": "不支持的文件类型"
    }
  }
}
```
```json
// en-US.json
{
  "common": { "ok": "OK", "cancel": "Cancel", "retry": "Retry", "create": "Create" },
  "empty": { "noData": "No data", "tryAdjustFilters": "Try adjusting filters" },
  "auth": {
    "login": { "title": "Sign In", "username": "Username", "password": "Password", "submit": "Sign In" }
  },
  "course": { "list": { "title": "Courses", "emptyTitle": "No matching courses" } },
  "error": {
    "unauthorized": "Not authenticated or session expired",
    "forbidden": "You do not have permission",
    "rateLimited": "Too many requests. Please try again later",
    "server": "Server error. Please try again later"
  },
  "file": {
    "upload": { "maxSizeHint": "File size must not exceed {size}MB", "typeNotAllowed": "Unsupported file type" }
  }
}
```

## 4. 组件用法
```vue
<script setup lang="ts">
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
</script>
<template>
  <h1>{{ t('course.list.title') }}</h1>
  <Empty :description="t('empty.tryAdjustFilters')" />
  <NButton>{{ t('common.create') }}</NButton>
</template>
```

## 5. 插值/复数/选择
```ts
// 插值
const hint = t('file.upload.maxSizeHint', { size: 50 })

// 复数（需在语言包按 ICU 语法定义）
// "notifications.count": "{count, plural, =0{No notifications} one{# notification} other{# notifications}}"
const msg = t('notifications.count', { count: 3 })

// 选择（复杂提示分支）
// "grade.publish.status": "{status, select, draft{Draft} published{Published} other{Unknown}}"
const statusText = t('grade.publish.status', { status: 'draft' })
```

## 6. 语言切换
```ts
// i18n 实例
import { createI18n } from 'vue-i18n'
const i18n = createI18n({ legacy: false, locale: 'zh-CN', fallbackLocale: 'en-US', messages: { 'zh-CN': zhCN, 'en-US': enUS } })
// 切换
i18n.global.locale.value = 'en-US'
```

## 7. 验收清单
- 所有新增文案均在语言包中定义，无硬编码
- 插值/复数在两种语言下语序与占位正确
- 错误提示对齐后端错误码语义
- 默认中文、回退英文（可配置）
- 页面空态、按钮文本与通知消息全部可本地化

## 8. 学生端使用规范补充（本次改造）
- 页面使用 `useI18n` 获取 `t`，移除 `|| '中文兜底'` 这类本地硬编码回退。
- 新增 `student.grades.*` 键：标题、副标题、加载态、空态、分数字样（outOf）。
- `AssignmentsView`、`AnalyticsView`、`CourseDetailView` 已统一移除兜底并走 i18n。
- 统一品牌样式：优先使用 `glass-*` 类与 `v-glass` 指令，按钮/卡片用 `/components/ui` 目录组件。

### 新增键（v0.2.2）
- `student.assignments.publish`：发布日期
  - zh-CN：`发布日期`
  - en-US：`Published`

## 9. 教师端 AI 批改弹窗新增键（2025-09-17）
- 新增：`teacher.aiGrading.picker.{assignment,student,preview,useText,attachments,selectAllFiles,clearFiles,text,reload}`
- 示例用法：
```vue
<GlassPopoverSelect :label="t('teacher.aiGrading.picker.assignment')" :placeholder="t('common.pleaseSelect') + t('teacher.assignments.title')" />
```

## 10. 学生管理入课密钥键（2025-09-11）
- `teacher.students.enrollKey.{title,require,keyLabel,placeholder,tip,save,saved,failed}`
- `teacher.students.actions.setEnrollKey`
