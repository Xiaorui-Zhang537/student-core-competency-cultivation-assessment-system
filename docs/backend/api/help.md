# 帮助中心后端 API（中文）

本文档描述帮助中心相关的后端接口，所有接口统一前缀为 `/api/help`。

## 分类（Categories）
- GET `/api/help/categories`
  - 说明：获取帮助分类列表
  - 返回：`HelpCategory[]`
  - 字段：`id, name, slug, sort, createdAt, updatedAt`

## 文章（Articles）
- GET `/api/help/articles`
  - 说明：按条件查询帮助文章列表
  - 查询参数：
    - `q?` 关键字（标题/内容模糊匹配）
    - `categoryId?` 分类 ID
    - `tag?` 标签（FIND_IN_SET）
    - `sort?` 排序方式：`latest`（默认）或 `hot`
  - 返回：`HelpArticle[]`

- GET `/api/help/articles/{slug}`
  - 说明：获取指定文章详情
  - 查询参数：
    - `inc?` 是否递增浏览量（默认 `true`）
  - 返回：`HelpArticle`

- POST `/api/help/articles/{id}/feedback`
  - 说明：提交文章反馈/有用性投票
  - 查询参数：
    - `helpful?` 是否有帮助（`true/false`），可选
    - `content?` 文字反馈，可选
  - 返回：空（统一 `ApiResponse.success()`）

## 工单（Tickets）
- POST `/api/help/tickets`
  - 说明：创建技术支持工单（需要登录）
  - 查询参数：
    - `title` 工单标题（必填）
    - `description` 工单描述（必填）
  - 返回：`HelpTicket`

- GET `/api/help/tickets`
  - 说明：获取当前登录用户的工单列表（需要登录）
  - 返回：`HelpTicket[]`

## 数据模型（简要）
- `HelpCategory`：`id, name, slug, sort, createdAt, updatedAt`
- `HelpArticle`：`id, categoryId, title, slug, contentMd, contentHtml, tags, views, upVotes, downVotes, published, createdAt, updatedAt`
- `HelpTicket`：`id, userId, title, description, status, createdAt, updatedAt`

前端类型参见：`frontend/src/types/help.ts`。
