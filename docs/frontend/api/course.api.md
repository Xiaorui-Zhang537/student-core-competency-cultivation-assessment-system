---
title: "`course.api.ts`"
description: 课程 CRUD、选课与课程学生管理 API 封装
outline: [2, 3]
---

# 前端 API：course.api.ts

## 方法签名（节选）
- `getCourses(params)` / `getCourseById(id)` / `createCourse(data)` / `updateCourse(id, data)` / `deleteCourse(id)`
- `enrollInCourse(id)` / `unenrollFromCourse(id)`
- `getCourseStudents(courseId, params)` / `inviteStudents(courseId, studentIds)` / `removeStudent(courseId, studentId)`
- `publishCourse(id)` / `setCourseEnrollKey(courseId, require, key?)`

## 最小示例
```ts
const data = await courseApi.getCourses({ page: 1, size: 10, query: 'AI' })
console.log(data.items, data.total)
```

## 注意事项
- 敏感操作需要教师角色
- 学生端选课商城当前直接复用 `getCourses(params)` 分页接口，不再依赖独立“课程发现”前端封装

## 参数与返回
- 分页参数：`{ page?: number; size?: number; sort?: string }`
- 检索参数：`{ query?: string; status?: string; teacherId?: string }`
- `Course`/`CourseDetailed`/`PaginatedResponse<T>` 参见 `@/types/course` 与 `@/types/api`

## 进阶示例
```ts
// 搜索+分页（分页接口：/courses）
const page = await courseApi.getCourses({ query: 'AI', page: 1, size: 20 })
console.log(page.items, page.total)

// 选课/退课
await courseApi.enrollInCourse(1001)
await courseApi.unenrollFromCourse(1001)

// 课程学生（教师）
const students = await courseApi.getCourseStudents('1001', { page: 1, size: 50 })

// 邀请/移除学生（教师）
await courseApi.inviteStudents(1001, [2001, '2002'])
await courseApi.removeStudent(1001, 2001)

// 课程发布（教师）
await courseApi.publishCourse(1001)
```

## 错误处理与权限
- 401：未登录；自动跳转登录
- 403：非课程教师执行敏感操作
- 404：课程不存在
- 409：非法状态迁移或重复发布/下线
