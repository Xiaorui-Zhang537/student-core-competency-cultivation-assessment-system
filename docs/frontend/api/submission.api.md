# 前端 API：submission.api.ts

## 方法签名
- `getSubmissionById(id)` / `getSubmissionForAssignment(assignmentId)`
- `getSubmissionsByAssignment(assignmentId, params)`
- `submitAssignment(assignmentId, data)` / `saveDraft(assignmentId, data)`
- `exportSubmission(id)`（blob）

## 注意事项
- 学生提交/草稿需学生身份；教师可分页查看该作业提交
- 导出需使用 axios 客户端以带鉴权头
