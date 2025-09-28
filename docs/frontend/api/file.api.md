# 前端 API：file.api.ts

## 方法签名
- `uploadFile(file, extra?)`（multipart）
- `getFileInfo(fileId)` / `getRelatedFiles(purpose, relatedId)`
- `deleteFile(fileId)`
- `downloadFile(fileId, filename?)`（blob 下载）
- `getPreview(fileId)`（blob 预览）

## 注意事项
- Content-Type 设置为 `multipart/form-data`
- 下载/预览需鉴权头，使用 axios 客户端发起
 - 聊天抽屉附件必须使用 `getPreview` 与 `downloadFile`（带 Token）。不要直接 `<img src="/api/files/{id}/preview">` 或 `<a href="/api/files/{id}/download">`，否则在鉴权环境下会 401/403。

## 参数与返回
- `uploadFile(file, extra?)`：`extra` 可传 `{ purpose?: string; relatedId?: string|number }`
- `getRelatedFiles(purpose, relatedId)`：用于按用途/绑定实体查询文件
- 返回类型参见 `@/types/file`，下载/预览返回 Blob

## 示例
```ts
// 上传
const fileInfo = await fileApi.uploadFile(file, { purpose: 'assignment', relatedId: 5001 })

// 预览图片
const blob = await fileApi.getPreview(fileInfo.id)
const url = URL.createObjectURL(blob)

// 下载
await fileApi.downloadFile(fileInfo.id, '附件.zip')
// 聊天附件（示例）：
// 1) 上传（purpose=chat, relatedId=对端用户ID 或 课程上下文）
await fileApi.uploadFile(file, { purpose: 'chat', relatedId: peerId })
// 2) 预览（图片）
const chatBlob = await fileApi.getPreview(fileId)
const chatUrl = URL.createObjectURL(chatBlob)
// 3) 下载（任意类型）
await fileApi.downloadFile(fileId, '聊天附件')

// 关联查询
const list = await fileApi.getRelatedFiles('assignment', 5001)
```

## 错误处理
- 400：未选择文件/文件超限/类型不支持
- 401：未登录；
- 413：文件过大
