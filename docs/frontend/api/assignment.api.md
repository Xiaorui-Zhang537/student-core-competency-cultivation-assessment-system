# 前端 API：assignment.api.ts

## 方法签名
- `getAssignments(params)` / `getAssignmentsByCourse(courseId, params)` / `getAssignmentById(id)`
- `createAssignment(data)` / `updateAssignment(id, data)` / `deleteAssignment(id)`
- `publishAssignment(id)` / `closeAssignment(id)`
- `getAssignmentSubmissionStats(id)` / `remindUnsubmitted(id, message?)`

## 注意事项
- `dueDate` 自动归一为 `yyyy-MM-dd HH:mm:ss` 格式
- 发布/关闭需教师权限

## 参数与返回
- 分页：`{ page?: number; size?: number; sort?: string }`
- 检索：`{ courseId?: string; teacherId?: string; status?: string; keyword?: string }`
- 创建/更新：`AssignmentCreationRequest` / `AssignmentUpdateRequest`（见 `@/types/assignment`）
- 统计：`{ assignmentId, courseId, totalEnrolled, submittedCount, unsubmittedCount }`

`create/update` 会自动规范 `dueDate`：
- 传 `YYYY-MM-DD` 自动补全为当天 23:59:59
- 传 ISO 日期会转为 `yyyy-MM-dd HH:mm:ss`

## 示例
```ts
// 列表
const { items } = await assignmentApi.getAssignments({ courseId: '1001', page: 1, size: 20 })

// 创建
await assignmentApi.createAssignment({ title: 'HW1', dueDate: '2025-09-01' } as any)

// 绑定课时
await assignmentApi.bindLesson('5001', '3001')

// 发布/关闭
await assignmentApi.publishAssignment('5001')
await assignmentApi.closeAssignment('5001')

// 提交统计
const stats = await assignmentApi.getAssignmentSubmissionStats('5001')
```

## 错误处理
- 400：字段或日期格式不合法
- 401/403：鉴权失败或权限不足
- 409：重复发布/关闭
