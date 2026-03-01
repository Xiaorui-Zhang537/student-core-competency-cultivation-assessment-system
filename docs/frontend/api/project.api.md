---
title: "`project.api.ts`"
description: 公开项目目录树（用于首页 FileTree 展示）
outline: [2, 3]
---

# `project.api.ts`（Project Tree）

对应后端公开端点：`GET /api/public/project/tree`，用于展示项目目录结构（首页 FileTree 等）。

## 1. 方法

- `getProjectTree(params?)`

## 2. 参数

- `depth?`：目录深度（数字，越大返回越深的 children；按后端实现为准）

## 3. 返回

- `ProjectTreeNode[]`
  - `name`：节点名称
  - `directory`：是否为目录
  - `children?`：子节点

## 4. 最小调用示例

```ts
import { getProjectTree } from '@/api/project.api'

const tree = await getProjectTree({ depth: 0 })
```

