---
title: 项目 API（Project）
description: 公开读取项目目录树（只读，用于工作台/首页展示）
outline: [2, 3]
---

# 项目 API（Project）

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

本项目后端 `context-path=/api`，下文接口路径均以 `/api/...` 展示。

## 1. GET `/api/public/project/tree` 获取项目目录树（公开）

Query：

- `depth`：可选
  - `0` 表示不限制深度（谨慎使用）
  - `1..10` 会被裁剪到该范围
  - 注意：当前控制器对 `depth` 的默认值为 `0`，因此不传时也会走“不限制深度”的分支

curl：

```bash
curl "http://localhost:8080/api/public/project/tree?depth=5"
```

响应：`ApiResponse<ProjectTreeService.Node[]>`（示例）：

```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "name": "backend",
      "directory": true,
      "children": [{ "name": "src", "directory": true }, { "name": "pom.xml", "directory": false }]
    }
  ]
}
```

Node 字段：

- `name`：文件/目录名
- `directory`：是否目录
- `children`：子节点（目录才有）

## 2. 约束（服务端防护）

- 仅返回工作区内 `backend`、`frontend`、`docs` 三个根目录（若存在）
- 忽略目录：`.git`、`.idea`、`.vscode`、`node_modules`、`target`、`dist`、`out`
- 只读接口，无写操作

## 3. 前端对接

- `GET /api/public/project/tree` ↔ `frontend/src/api/project.api.ts` 的 `getProjectTree()`
