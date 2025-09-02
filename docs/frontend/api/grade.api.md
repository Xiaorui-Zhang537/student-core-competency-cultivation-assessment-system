# 前端 API：grade.api.ts

## 方法签名
- `gradeSubmission(data)` / `publishGrade(id)` / `gradeBatchSubmissions(grades)` / `publishBatchGrades(ids)`
- `getGradeById(id)` / `getGradeHistory(id)`
- `getGradeForStudentAssignment(studentId, assignmentId)`
- `getGradesByStudent(studentId, params)` / `getGradesByAssignment(assignmentId, params)`

## 注意事项
- 打分/发布需教师身份
- 历史/分页查询用于成绩演进展示

## 参数与返回
- `GradeRequest`：`{ submissionId: string; score: number; feedback?: string }`
- `Grade`：成绩详情（见 `@/types/grade`）
- 分页参数：`{ page?: number; size?: number }`

## 示例
```ts
// 打分
const g = await gradeApi.gradeSubmission({ submissionId: '8001', score: 92, feedback: '做得很好' })

// 发布成绩
await gradeApi.publishGrade(String(g.id))

// 学生维度分页查询
const page = await gradeApi.getGradesByStudent('1001', { page: 1, size: 10 })

// 作业维度分页查询
const page2 = await gradeApi.getGradesByAssignment('5001', { page: 1, size: 10 })
```

## 错误处理
- 400：成绩/反馈字段校验失败
- 401/403：未登录或权限不足
- 404：提交或成绩不存在
