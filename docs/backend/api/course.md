---
title: 课程 API（Course）
description: 课程创建、发布、选课、课程学生管理与统计
outline: [2, 3]
---

# 课程 API（Course）

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

本项目后端 `context-path=/api`，下文接口路径均以 `/api/...` 展示。

## 1. 通用约定

响应封装：统一返回 `ApiResponse<T>`。

角色权限（按接口注解）：

- 公开读接口：一般无需特殊角色
- 写操作：`TEACHER` / `ADMIN`
- 选课：`STUDENT` / `ADMIN`

## 2. 数据结构：Course（核心字段）

```json
{
  "id": 2,
  "title": "Intro",
  "description": "...",
  "content": "<p>...</p>",
  "coverImage": "https://...",
  "teacherId": 11,
  "category": "CS",
  "difficulty": "beginner",
  "status": "draft",
  "startDate": "2026-03-01",
  "endDate": "2026-06-30",
  "enrollmentCount": 0,
  "rating": 0.0,
  "tags": "[\"ai\",\"math\"]",
  "requireEnrollKey": false
}
```

补充说明：
- `rating` / `reviewCount` 目前不是通过独立“课程评分”接口写入。
- 它们来自学生对课时的 `POST /api/lessons/{lessonId}/rating` 聚合结果：先按“单个学生在该课程下已评分课时的平均分”求个人课程评分，再对所有已评分学生求课程均值。
- 因此前端当前更多是“展示课程口碑分”，而不是提供单独的课程打星入口。

`status`：`draft|published|archived`（以实现为准）。

## 3. 课程列表与详情

### GET `/api/courses` 分页查询课程

Query（均可选，page/size 有默认值）：

- `page`：默认 1
- `size`：默认 10
- `query`：关键词
- `category`
- `difficulty`
- `status`
- `teacherId`

响应：`ApiResponse<PageResult<Course>>`

curl：

```bash
curl 'http://localhost:8080/api/courses?page=1&size=10&query=AI'
```

### GET `/api/courses/{id}` 课程详情

响应：`ApiResponse<Course>`

## 4. 课程 CRUD

### POST `/api/courses` 创建课程

权限：`TEACHER` / `ADMIN`

说明：后端会把 `teacherId` 设为当前登录用户。

请求 Body（示例）：

```json
{ "title": "Intro", "description": "...", "category": "CS", "difficulty": "beginner" }
```

响应：`ApiResponse<Course>`

### PUT `/api/courses/{id}` 更新课程

权限：`TEACHER` / `ADMIN`

请求 Body：`Course`

响应：`ApiResponse<Course>`

### DELETE `/api/courses/{id}` 删除课程

权限：`TEACHER` / `ADMIN`

响应：成功返回 200，无 data。

## 5. 发布/下架/批量状态

### POST `/api/courses/{id}/publish` 发布课程

权限：`TEACHER` / `ADMIN`

响应：成功返回 200，无 data。

### POST `/api/courses/{id}/unpublish` 下架课程

权限：`TEACHER` / `ADMIN`

响应：成功返回 200，无 data。

### PUT `/api/courses/batch-status` 批量更新课程状态

权限：`TEACHER` / `ADMIN`

请求 Body：

```json
{ "courseIds": [1, 2], "status": "published" }
```

响应：成功返回 200，无 data。

## 6. 课程发现（推荐/热门/分类/搜索）

### GET `/api/courses/popular` 热门课程

Query：`limit`（默认 10）

响应：`ApiResponse<Course[]>`

### GET `/api/courses/recommended` 推荐课程

Query：`limit`（默认 10）

响应：`ApiResponse<Course[]>`

### GET `/api/courses/category/{category}` 分类课程（简单列表）

响应：`ApiResponse<Course[]>`

### GET `/api/courses/search` 搜索课程（简单列表）

Query：`keyword`（必填）

响应：`ApiResponse<Course[]>`

## 7. 选课与状态

### POST `/api/courses/{id}/enroll` 学生选课

权限：`STUDENT` / `ADMIN`

请求 Body（可选，课程开启密钥时使用）：

```json
{ "enrollKey": "xxxx" }
```

响应：成功返回 200，无 data。

### DELETE `/api/courses/{id}/enroll` 学生退课

权限：`STUDENT` / `ADMIN`

响应：成功返回 200，无 data。

### GET `/api/courses/{id}/enrollment-status` 我的选课状态

权限：`STUDENT` / `ADMIN`

响应：`ApiResponse<boolean>`

## 8. 入课密钥（教师）

### PUT `/api/courses/{id}/enroll-key`

权限：`TEACHER` / `ADMIN`

请求 Body：

```json
{ "require": true, "key": "明文密钥(可选)" }
```

说明：后端只保存密钥哈希，不保存明文。

响应：成功返回 200，无 data。

## 9. 课程学生管理（教师）

### GET `/api/courses/{id}/students` 课程学生列表（分页）

权限：`TEACHER` / `ADMIN`

Query：

- `page`：默认 1
- `size`：默认 20
- `search`：可选（昵称/用户名/学号/工号）
- `sortBy`：默认 `joinDate`（支持 `name|progress|grade|lastActive|joinDate`）
- `activity`：可选（`high|medium|low|inactive`）
- `grade`：可选（`excellent|good|average|below`）
- `progress`：可选（`not-started|in-progress|completed`）

响应：`ApiResponse<PageResult<User>>`

### POST `/api/courses/{id}/students/invite` 批量添加学生

权限：`TEACHER` / `ADMIN`

请求 Body：

```json
{ "studentIds": [1001, 1002] }
```

响应：成功返回 200，无 data。

### DELETE `/api/courses/{id}/students/{studentId}` 移除学生

权限：`TEACHER` / `ADMIN`

响应：成功返回 200，无 data。

## 10. 课程统计

### GET `/api/courses/statistics`

权限：`TEACHER` / `ADMIN`

Query：

- `teacherId`：可选。不传且当前是教师时，后端会默认统计本人。

响应：`ApiResponse<CourseStatisticsResponse>`

## 11. 常见错误与排查

- 400：参数校验失败（分页、keyword 为空、密钥格式等）。
- 401：未登录或 token 过期。
- 403：非教师/管理员调用写接口，或越权管理他人课程。
- 404：课程不存在。
