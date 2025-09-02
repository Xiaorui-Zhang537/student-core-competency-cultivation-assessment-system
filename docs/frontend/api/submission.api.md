# 前端 API：submission.api.ts

## 方法签名
- `getSubmissionById(id)` / `getSubmissionForAssignment(assignmentId)`
- `getSubmissionsByAssignment(assignmentId, params)`
- `submitAssignment(assignmentId, data)` / `saveDraft(assignmentId, data)`
- `exportSubmission(id)`（blob）

## 注意事项
- 学生提交/草稿需学生身份；教师可分页查看该作业提交
- 导出需使用 axios 客户端以带鉴权头

## 参数与返回
- `SubmissionRequest`：正文/附件等（见 `@/types/submission`）
- `DraftRequest`：草稿内容结构（见 `@/types/submission`）
- 分页：`{ page?: number; size?: number }`
- 返回：`Submission` 或 `PaginatedResponse<Submission>`，由拦截器解包为业务数据

## 示例
```ts
// 学生提交作业
await submissionApi.submitAssignment('5001', { content: '答案', attachments: [123] } as any)

// 保存草稿
await submissionApi.saveDraft('5001', { content: '草稿内容' } as any)

// 教师分页查看该作业提交
const page = await submissionApi.getSubmissionsByAssignment('5001', { page: 1, size: 20 })

// 教师查看单条提交
const sub = await submissionApi.getSubmissionById('8001')

// 导出提交（zip Blob）
const blob = await submissionApi.exportSubmission('8001')
```

## 错误处理与权限
- 400：提交内容校验失败（缺字段/格式不符）
- 401：未登录；
- 403：教师/学生角色不匹配导致无权限
- 404：作业或提交不存在
