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
