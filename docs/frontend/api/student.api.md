# 前端 API：student.api.ts

## 方法签名
- `getDashboardData()` / `getMyCourses(params)` / `getCourseProgress(courseId)`
- `getLessonDetails(lessonId)` / `markLessonAsCompleted(lessonId)` / `markLessonAsIncomplete(lessonId)`
- `getMySubmissions(params)` / `getAnalysis(params)`
- `getCourseParticipants(courseId, keyword?)` / `getAssignments(params)`

## 注意事项
- 学生端接口需登录；部分与课程成员身份绑定

## 参数与返回
- 分页：`{ page?: number; size?: number; q?: string }`
- `StudentDashboardData`/`StudentCourse`/`StudentLesson`/`StudentSubmission` 见 `@/types/student`
- `getAnalysis`：`{ range?: '7d'|'30d'|'term' }`，返回包含 KPI/雷达/趋势/近期成绩

## 示例
```ts
// 仪表盘
const dashboard = await studentApi.getDashboardData()

// 我的课程
const myCourses = await studentApi.getMyCourses({ page: 1, size: 10, q: 'AI' })

// 课程进度
const progress = await studentApi.getCourseProgress('1001')

// 课时详情+完成/撤销完成
const lesson = await studentApi.getLessonDetails('3001')
await studentApi.markLessonAsCompleted('3001')
await studentApi.markLessonAsIncomplete('3001')

// 我的作业与成绩
const subs = await studentApi.getMySubmissions({ courseId: '1001' })

// 学业分析
const analysis = await studentApi.getAnalysis({ range: '30d' })
```

## 错误处理
- 401：未登录
- 403：非该课程成员
- 404：课程/课时不存在
