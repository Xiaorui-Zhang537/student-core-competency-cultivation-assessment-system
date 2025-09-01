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
