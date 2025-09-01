# 前端 API：course.api.ts

## 方法签名（节选）
- `getCourses(params)` / `getCourseById(id)` / `createCourse(data)` / `updateCourse(id, data)` / `deleteCourse(id)`
- `searchCourses(params)` / `getPopularCourses()` / `getRecommendedCourses()` / `getCoursesByCategory(category, params)`
- `enrollInCourse(id)` / `unenrollFromCourse(id)` / `getEnrollmentStatus(id)`
- `getCourseStudents(courseId, params)` / `inviteStudents(courseId, studentIds)` / `removeStudent(courseId, studentId)`
- `publishCourse(id)` / `unpublishCourse(id)` / `updateBatchStatus(data)`

## 最小示例
```ts
const { data } = await courseApi.getCourses({ page: 1, size: 10 })
```

## 注意事项
- 敏感操作需要教师角色
- 分类/推荐接口与后端实际实现保持一致
