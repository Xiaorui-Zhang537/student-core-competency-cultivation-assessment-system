# 前端 API：student.api.ts

## 方法签名
- `getDashboardData()` / `getMyCourses(params)` / `getCourseProgress(courseId)`
- `getLessonDetails(lessonId)` / `markLessonAsCompleted(lessonId)` / `markLessonAsIncomplete(lessonId)`
- `getMySubmissions(params)` / `getAnalysis(params)`
- `getCourseParticipants(courseId, keyword?)` / `getAssignments(params)`

## UI 约定：PageHeader 标题/简介
- 学生端页面统一使用 `components/ui/PageHeader.vue` 渲染标题与副标题；文案来自 i18n（`student.*`）。
- 不依赖接口返回值作为标题文本，避免异步导致闪烁；动态信息应放在主体或 actions 插槽。
- 示例：
```vue
<PageHeader :title="t('student.assignments.title')" :subtitle="t('student.assignments.subtitle')" />
```

## 注意事项
- 学生端接口需登录；部分与课程成员身份绑定

## 参数与返回
- 分页：`{ page?: number; size?: number; q?: string }`（注意：仅当 `q` 非空时传参）
- 学生作业筛选：
  - `GET /api/students/assignments?courseId&status&q&page&size`
  - `status` 取值：`pending | submitted | graded`（小写）。若不传表示全部；待提交由后端根据发布/提交状态聚合。
  - `q` 为关键字搜索，与 `/students/my-courses/paged` 保持一致。
- `StudentDashboardData`/`StudentCourse`/`StudentLesson`/`StudentSubmission` 见 `@/types/student`
- `getAnalysis`：`{ range?: '7d'|'30d'|'term' }`，返回包含 KPI/雷达/趋势/近期成绩

## 示例
// 学生作业（分页/筛选/搜索）
const items = await studentApi.getAssignments({ courseId: '1001', status: 'submitted', q: 'ML', page: 1, size: 10 })
```ts
// 仪表盘
const dashboard = await studentApi.getDashboardData()

// 我的课程（仅当 q 非空时传）
const myCourses = await studentApi.getMyCourses({ page: 1, size: 10 })

// 课程进度
const progress = await studentApi.getCourseProgress('1001')

// 课时详情+完成/撤销完成
const lesson = await studentApi.getLessonDetails('3001')
await studentApi.markLessonAsCompleted('3001')
await studentApi.markLessonAsIncomplete('3001')

// 我的作业（分页/筛选）
const myAssignments = await studentApi.getAssignments({ courseId: '1001', status: 'pending', page: 1, size: 10 })

// 我的提交与成绩
const subs = await studentApi.getMySubmissions({ courseId: '1001' })

// 学业分析
const analysis = await studentApi.getAnalysis({ range: '30d' })
```

## 错误处理
- 401：未登录
- 403：非该课程成员
- 404：课程/课时不存在
