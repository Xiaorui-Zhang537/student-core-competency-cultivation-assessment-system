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

## 参数与返回
- 分页参数：`{ page?: number; size?: number; sort?: string }`
- 检索参数：`{ query?: string; status?: string; teacherId?: string }`
- `Course`/`CourseDetailed`/`PaginatedResponse<T>` 参见 `@/types/course` 与 `@/types/api`

## 进阶示例
```ts
// 搜索+分页
const { items, total } = await courseApi.searchCourses({ query: 'AI', page: 1, size: 20 })

// 选课/退课
await courseApi.enrollInCourse(1001)
await courseApi.unenrollFromCourse(1001)

// 课程学生（教师）
const students = await courseApi.getCourseStudents('1001', { page: 1, size: 50 })

// 邀请/移除学生（教师）
await courseApi.inviteStudents(1001, [2001, '2002'])
await courseApi.removeStudent(1001, 2001)

// 批量状态更新（教师）
await courseApi.updateBatchStatus({ ids: [1,2,3], status: 'PUBLISHED' } as any)
```

## 错误处理与权限
- 401：未登录；自动跳转登录
- 403：非课程教师执行敏感操作
- 404：课程不存在
- 409：非法状态迁移或重复发布/下线
