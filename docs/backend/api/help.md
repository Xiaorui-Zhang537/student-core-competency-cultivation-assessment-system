---
title: 帮助中心 API（Help Center）
description: 分类、文章、反馈与工单（/api/help/**）
outline: [2, 3]
---

# 帮助中心 API（Help Center）

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

本项目后端 `context-path=/api`，下文接口路径均以 `/api/...` 展示。

## 1. 通用约定

响应封装：统一返回 `ApiResponse<T>`。

权限：

- 分类/文章/反馈：无需登录也可调用（反馈会尝试记录 userId，未登录则为空）
- 工单：需要登录（`isAuthenticated()`）

## 2. 分类（Categories）

### GET `/api/help/categories`

响应：`ApiResponse<HelpCategory[]>`

curl：

```bash
curl 'http://localhost:8080/api/help/categories'
```

## 3. 文章（Articles）

### GET `/api/help/articles`

Query（均可选）：

- `q`：关键字（标题/内容模糊匹配）
- `categoryId`：分类 ID
- `tag`：标签（FIND_IN_SET）
- `sort`：排序，默认 `latest`，可选 `hot`

响应：`ApiResponse<HelpArticle[]>`

### GET `/api/help/articles/{slug}`

Query：

- `inc`：是否递增浏览量，默认 `true`

响应：`ApiResponse<HelpArticle>`

### POST `/api/help/articles/{id}/feedback` 提交文章反馈

Query（均可选）：

- `helpful`：`true|false`
- `content`：文字反馈

响应：成功返回 200，无 data。

curl：

```bash
curl -X POST 'http://localhost:8080/api/help/articles/1/feedback?helpful=true&content=%E5%BE%88%E6%9C%89%E7%94%A8'
```

## 4. 工单（Tickets）

### POST `/api/help/tickets` 创建工单

权限：已登录

请求 Body：`HelpTicketCreateRequest`

```json
{ "title": "无法提交作业", "description": "点击提交后提示 500" }
```

响应：`ApiResponse<HelpTicket>`

### GET `/api/help/tickets` 我的工单列表

权限：已登录

响应：`ApiResponse<HelpTicket[]>`

### PUT `/api/help/tickets/{id}` 编辑我的工单

权限：已登录

请求 Body：同创建

响应：`ApiResponse<HelpTicket>`

### DELETE `/api/help/tickets/{id}` 删除/撤回我的工单

权限：已登录

响应：成功返回 200，无 data。

## 5. 常见错误与排查

- 401：工单接口未登录或 token 过期。
- 404：文章/工单不存在，或无权访问他人工单。

前端类型参考：`frontend/src/types/help.ts`。

