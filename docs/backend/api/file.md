---
title: 文件 API（File）
description: 上传、下载、预览、流式播放、删除与文件列表
outline: [2, 3]
---

# 文件 API（File）

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

本项目后端 `context-path=/api`，下文接口路径均以 `/api/...` 展示。

## 1. 通用约定

权限：所有文件接口都要求已登录（`isAuthenticated()`）。

返回封装：

- 上传/列表/信息/删除：返回 `ApiResponse<T>`
- 下载/预览/流式：返回原始二进制（不包 `ApiResponse`）

## 2. 数据结构：FileRecord（常用字段）

```json
{
  "id": 9876,
  "originalName": "file.pdf",
  "storedName": "20241228_103000_file.pdf",
  "filePath": "/uploads/...",
  "fileSize": 102400,
  "extension": "pdf",
  "mimeType": "application/pdf",
  "uploadPurpose": "assignment",
  "uploaderId": 1001,
  "relatedType": "assignment",
  "relatedId": 123,
  "status": "active",
  "downloadCount": 0,
  "createdAt": "2026-03-01 12:00:00",
  "updatedAt": "2026-03-01 12:00:00"
}
```

## 3. 上传：POST `/api/files/upload`

权限：已登录

Content-Type：`multipart/form-data`

表单字段：

- `file`：必填（文件本体）
- `purpose`：可选，默认 `general`
- `relatedId`：可选（用于把文件与业务对象关联）

curl：

```bash
curl -X POST 'http://localhost:8080/api/files/upload' \
  -H 'Authorization: Bearer <access_jwt>' \
  -F file=@/path/to/file.pdf \
  -F purpose=assignment \
  -F relatedId=123
```

响应：`ApiResponse<FileRecord>`

## 4. 下载/预览/流式（二进制）

### GET `/api/files/{fileId}/download` 下载

权限：已登录

说明：

- 返回二进制内容，响应头包含 `Content-Type` 与 `Content-Disposition: inline; filename="..."`。
- 管理员（`ADMIN`）有审计放行逻辑，可下载学生上传的文件。

curl：

```bash
curl -L 'http://localhost:8080/api/files/9876/download' \
  -H 'Authorization: Bearer <access_jwt>' \
  -o file_9876.bin
```

### GET `/api/files/{fileId}/preview` 预览（仅 image/* 与 application/pdf）

权限：已登录

说明：

- 仅允许图片与 PDF；其它类型会返回 400。
- 若图片文件丢失（如头像引用的文件被清理），后端会返回 1x1 透明 PNG 用于降级显示。

curl：

```bash
curl 'http://localhost:8080/api/files/9876/preview' \
  -H 'Authorization: Bearer <access_jwt>' \
  --output preview.bin
```

### GET `/api/files/{fileId}/stream` 流式播放（支持 Range）

权限：已登录

说明：

- 支持 `Range: bytes=start-end`，返回 `206 Partial Content`，并带 `Accept-Ranges/Content-Range/Content-Length`。
- 用于 `<video>`/`<audio>` 播放更稳定。

curl（取前 1MB）：

```bash
curl 'http://localhost:8080/api/files/9876/stream' \
  -H 'Authorization: Bearer <access_jwt>' \
  -H 'Range: bytes=0-1048575' \
  --output seg.bin
```

## 5. 文件信息与列表

### GET `/api/files/{fileId}/info` 获取文件信息

权限：已登录

响应：`ApiResponse<FileRecord>`

### GET `/api/files/my` 获取我的文件列表

权限：已登录

Query：

- `purpose`：可选

响应：`ApiResponse<FileRecord[]>`

### GET `/api/files/related` 获取关联文件列表

权限：已登录

Query：

- `purpose`：必填
- `relatedId`：必填

响应：`ApiResponse<FileRecord[]>`

### GET `/api/files/storage/usage` 存储使用量

权限：已登录

响应（示例）：

```json
{
  "code": 200,
  "data": {
    "usage": 5242880,
    "usageFormatted": "5.0 MB",
    "limit": 104857600,
    "limitFormatted": "100MB"
  }
}
```

## 6. 删除

### DELETE `/api/files/{fileId}` 删除单个文件

权限：已登录

响应：成功返回 200，无 data。

### DELETE `/api/files/batch` 批量删除

权限：已登录

请求 Body：`number[]`

```json
[9876, 9999, 10000]
```

响应：`ApiResponse<Map>`（以服务端实现为准）

## 7. 常见错误与排查

- 400：上传缺少 `file`；或 `preview` 非图片/PDF；或 Range 格式不合法。
- 401：未登录或 token 过期。
- 403：无权访问他人文件（管理员审计场景除外）。
- 404：文件不存在或已被删除。

