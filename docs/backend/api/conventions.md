---
title: 后端 API 通用约定
description: 认证、响应封装、分页、时间格式与常见错误（适用于 /api/**）
outline: [2, 3]
---

# 后端 API 通用约定

本项目后端 `server.servlet.context-path=/api`，因此本文档中所有端点均以 `/api/...` 展示。

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

## 1. 认证（JWT）

除少量公开接口外，大多数接口需要在请求头携带：

```text
Authorization: Bearer <token>
```

Token 来源：见 [/backend/api/auth](/backend/api/auth)。

## 2. 统一响应封装：`ApiResponse<T>`

大多数 JSON 接口统一返回：

```json
{ "code": 200, "message": "Success", "data": null }
```

- `code=200` 表示成功
- `message` 为提示信息（成功时通常是 `Success`）
- `data` 为业务数据（可能是对象、数组、分页结构，或 `null`）

:::tip 文件/流接口例外
下载/预览/导出类接口通常直接返回二进制（blob），不走 `ApiResponse<T>`。
:::

## 3. 分页：`PageResult<T>`（常见形态）

分页接口常见 Query：

- `page`（默认 1）
- `size`（默认 10/20；上限以接口实现为准）

常见返回结构（字段名以实现为准）：

```json
{
  "items": [],
  "page": 1,
  "size": 20,
  "total": 0,
  "totalPages": 0
}
```

## 4. 时间与日期格式

项目中同时存在两类时间格式（以具体字段 `@JsonFormat`/实现为准）：

- `yyyy-MM-dd HH:mm:ss`（例如行为摘要/洞察、社区评论时间）
- ISO 8601（例如部分 createdAt/updatedAt 字段）

文档中的示例以对应实体/DTO 的实际注解为准。

## 5. 常见错误码（通用理解）

- 400：参数非法/缺少必填
- 401：未认证或 token 失效
- 403：已登录但无权限
- 404：资源不存在
- 409：状态冲突（并发/重复操作）
- 5xx：服务端错误

## 6. curl 调用习惯（建议）

建议约定两个环境变量，便于复制粘贴：

```bash
export API_BASE="http://localhost:8080/api"
export TOKEN="<your-jwt>"
```

然后按需调用：

```bash
curl -H "Authorization: Bearer $TOKEN" "$API_BASE/users/me"
```

