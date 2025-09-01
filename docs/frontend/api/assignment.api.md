# 前端 API：assignment.api.ts

## 方法签名
- `getAssignments(params)` / `getAssignmentsByCourse(courseId, params)` / `getAssignmentById(id)`
- `createAssignment(data)` / `updateAssignment(id, data)` / `deleteAssignment(id)`
- `publishAssignment(id)` / `closeAssignment(id)`
- `getAssignmentSubmissionStats(id)` / `remindUnsubmitted(id, message?)`

## 注意事项
- `dueDate` 自动归一为 `yyyy-MM-dd HH:mm:ss` 格式
- 发布/关闭需教师权限
