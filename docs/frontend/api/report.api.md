# 前端 API：report.api.ts

## 方法签名
- `createReport(data: CreateReportRequest): Promise<ApiResponse<{ id: number }>>`

## CreateReportRequest 字段
- `reportedStudentId`（必填）
- `courseId/assignmentId/submissionId`（可选）
- `reason`（必填）
- `details/evidenceFileId`（可选）

## 注意事项
- 需登录；字段校验失败会返回 400
