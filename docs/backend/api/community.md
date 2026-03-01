---
title: 社区 API（Community）
description: 社区帖子、评论、点赞、统计与标签检索
outline: [2, 3]
---

# 社区 API（Community）

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

本项目后端 `context-path=/api`，下文接口路径均以 `/api/...` 展示。

## 1. 通用约定

- 匿名/游客：部分读接口允许未登录（例如帖子列表/详情），但会影响 `liked` 等“与当前用户相关”的字段填充
- 认证：写操作需要 `Authorization: Bearer <token>`
- 响应：统一返回 `ApiResponse<T>`（`code/message/data`）

## 2. 帖子（Posts）

### GET `/api/community/posts` 帖子列表（分页）

Query：

- `page`：默认 1
- `size`：默认 20
- `category`：可选
- `keyword`：可选
- `tag`：可选（传入时会走标签检索逻辑）
- `orderBy`：默认 `latest`

curl（可不带 token）：

```bash
curl "http://localhost:8080/api/community/posts?page=1&size=20&orderBy=latest"
```

响应：`ApiResponse<PageResult<Post>>`

### GET `/api/community/posts/{id}` 帖子详情

curl：

```bash
curl "http://localhost:8080/api/community/posts/101"
```

### POST `/api/community/posts` 发布帖子

权限：已登录（`isAuthenticated()`）

请求 Body：`PostCreateRequest`

```json
{
  "title": "How to learn AI?",
  "content": "......",
  "category": "qa",
  "allowComments": true,
  "anonymous": false,
  "pinned": false,
  "tags": ["ai", "study"]
}
```

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"title":"How to learn AI?","content":"...","category":"qa","allowComments":true,"anonymous":false,"pinned":false,"tags":["ai","study"]}' \
  http://localhost:8080/api/community/posts
```

响应：`ApiResponse<Post>`

### PUT `/api/community/posts/{id}` 编辑帖子（仅作者）

权限：已登录（作者校验在 service 内完成）

请求 Body：同 `POST /api/community/posts`

### DELETE `/api/community/posts/{id}` 删除帖子（仅作者）

curl：

```bash
curl -X DELETE -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/community/posts/101
```

### POST `/api/community/posts/{id}/like` 点赞/取消点赞帖子

返回：`ApiResponse<{ liked: boolean }>`

curl：

```bash
curl -X POST -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/community/posts/101/like
```

## 3. 评论（Comments）

### GET `/api/community/posts/{id}/comments` 评论列表（分页）

Query：

- `page`：默认 1
- `size`：默认 10
- `parentId`：可选（用于只看某条评论下的回复）
- `orderBy`：默认 `time`

### POST `/api/community/posts/{id}/comments` 发表评论/回复

权限：已登录

请求 Body：使用 `PostComment` 作为入参。建议只传：

- `content`（必填）
- `parentId`（可选，回复某条评论时传）

```json
{ "content": "Nice post!", "parentId": null }
```

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"content":"Nice post!","parentId":null}' \
  http://localhost:8080/api/community/posts/101/comments
```

响应：`ApiResponse<PostComment>`

### POST `/api/community/comments/{commentId}/like` 点赞/取消点赞评论

curl：

```bash
curl -X POST -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/community/comments/9001/like
```

### DELETE `/api/community/comments/{commentId}` 删除评论

curl：

```bash
curl -X DELETE -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/community/comments/9001
```

## 4. 统计与我的内容

### GET `/api/community/stats` 社区统计信息

### GET `/api/community/hot-topics` 热门话题

Query：`limit`（默认 10）

### GET `/api/community/active-users` 活跃用户

Query：`limit`（默认 10）

### GET `/api/community/my-posts` 我的帖子（分页）

权限：已登录

Query：`page`（默认 1）、`size`（默认 10）

### GET `/api/community/search/tags` 搜索标签

Query：

- `keyword`（必填）
- `limit`（默认 10）

## 5. Troubleshooting

- 401：未登录（写操作、我的帖子等）
- 403：无权编辑/删除非本人帖子或评论
- 404：帖子/评论不存在
