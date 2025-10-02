# 项目 API（Project）

> 以 Swagger 为准：`/api/swagger-ui.html`

## 1. 端点
- `GET /api/public/project/tree?depth=5`：公开获取项目目录树（受限于 backend / frontend / docs，默认深度 5，最大 10；传 0 表示无限深度）

### 响应示例
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    { "name": "backend", "directory": true, "children": [ { "name": "src", "directory": true }, { "name": "pom.xml", "directory": false } ] },
    { "name": "frontend", "directory": true, "children": [ { "name": "src", "directory": true }, { "name": "package.json", "directory": false } ] },
    { "name": "docs", "directory": true, "children": [ { "name": "README.md", "directory": false } ] }
  ]
}
```

## 2. 约束
- 仅返回工作区内 `backend`、`frontend`、`docs` 三个根目录（若存在）。
- 忽略目录：`.git`、`.idea`、`.vscode`、`node_modules`、`target`、`dist`、`out`。
- 最大深度：默认 3 层（包含根层）。
- 只读，无写操作。

## 3. 前端对接
- `GET /api/public/project/tree` ↔ `getProjectTree()`（见 `frontend/src/api/project.api.ts`）
- 首页 `HomeView.vue` 调用并使用 `FileTree.vue`（无卡片、可折叠）进行展示。
