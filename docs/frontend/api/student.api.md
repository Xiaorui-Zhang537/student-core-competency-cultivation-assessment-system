# 前端 API：student.api.ts

## 方法签名
- `getDashboardData()` / `getMyCourses(params)` / `getCourseProgress(courseId)`
- `getLessonDetails(lessonId)` / `markLessonAsCompleted(lessonId)` / `markLessonAsIncomplete(lessonId)`
- `getMySubmissions(params)` / `getAnalysis(params)`
- `getCourseParticipants(courseId, keyword?)` / `getAssignments(params)`

## 注意事项
- 学生端接口需登录；部分与课程成员身份绑定
