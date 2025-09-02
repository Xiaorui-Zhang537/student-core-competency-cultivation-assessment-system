# 前端 API：teacher.api.ts

## 方法签名
- `getCourseAnalytics(courseId)` / `getAssignmentAnalytics(assignmentId)` / `getClassPerformance(courseId)`
- `getCourseStudentPerformance(courseId, params)` / `exportCourseStudents(courseId, params)`
- `getAllCourseStudentsBasic(courseId, keyword?)`
- `getAbilityRadar(params)` / `getAbilityWeights(courseId)` / `updateAbilityWeights(payload)`
- `exportAbilityRadarCsv(params)` / `postAbilityRadarCompare(body)` / `exportAbilityRadarCompareCsv(body)` / `postAbilityDimensionInsights(body)`
- `resetStudentCourseProgress(courseId, studentId)`

## 注意事项
- 多数接口需教师身份；部分导出接口返回 blob

## 参数与返回
- 课程/作业分析：返回统计与分布数据（见 `@/types/teacher`）
- 学生表现分页：`{ page?: number; size?: number; search?: string; sortBy?: string; activity?: string; grade?: string; progress?: string }`
- 能力图/权重接口：`{ courseId; classId?; studentId?; startDate; endDate }` 与 `weights: Record<string, number>`

## 示例
```ts
// 课程分析
const courseStats = await teacherApi.getCourseAnalytics('1001')

// 作业分析
const assignStats = await teacherApi.getAssignmentAnalytics('5001')

// 班级表现
const perf = await teacherApi.getClassPerformance('1001')

// 学生表现分页
const page = await teacherApi.getCourseStudentPerformance('1001', { page: 1, size: 20, sortBy: 'grade' })

// 导出学生列表
const blob = await teacherApi.exportCourseStudents('1001', { search: '张' })

// 能力权重
const weights = await teacherApi.getAbilityWeights('1001')
await teacherApi.updateAbilityWeights({ courseId: '1001', weights: { collaboration: 1.2, creativity: 0.8 } })

// 雷达比较与导出
await teacherApi.postAbilityRadarCompare({ courseId: '1001', studentId: '2001', includeClassAvg: 'both' })
const csv = await teacherApi.exportAbilityRadarCompareCsv({ courseId: '1001', studentId: '2001' })
```

## 错误处理
- 401/403：未登录或权限不足
- 404：课程/作业不存在
