---
title: 学生端 API（Student）
description: 学生仪表盘、课程/课节进度、作业列表、提交与学习分析聚合
outline: [2, 3]
---

# 学生端 API（Student）

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

本项目后端 `context-path=/api`，下文接口路径均以 `/api/...` 展示。

## 1. 通用约定

- 认证：`Authorization: Bearer <token>`
- 响应：统一返回 `ApiResponse<T>`（`code/message/data`）
- 角色：本页接口主要面向 `STUDENT`（少量接口允许 `TEACHER` 一并访问）

## 2. 进度口径（业务说明）

进度/完成口径最终以 `LessonService`/`EnrollmentService` 的实现为准。当前项目中常用的展示口径：

- 课程进度：按课节聚合得到的完成进度（通常是 0-100 的百分比数值）。
- 最近学习：按学生最近一次学习记录聚合到课程/课节维度。

## 3. 仪表盘与课程

### GET `/api/students/dashboard` 学生仪表盘

权限：`STUDENT`

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/students/dashboard
```

响应（示例，字段以实际返回为准）：

```json
{
  "code": 200,
  "message": "Success",
  "data": { "kpis": { "courses": 5, "assignments": 12 }, "recentActivities": [] }
}
```

### GET `/api/students/my-courses/paged` 我的课程（分页）

权限：`STUDENT`

Query：

- `page`：默认 1
- `size`：默认 12
- `q`：可选关键词

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/students/my-courses/paged?page=1&size=12&q=AI"
```

响应：`ApiResponse<PageResult<StudentCourseResponse>>`（示例）：

```json
{
  "code": 200,
  "message": "Success",
  "data": { "items": [{ "id": 2, "title": "AI 101" }], "total": 1, "page": 1, "size": 12 }
}
```

### GET `/api/students/my/courses` 我参加的课程（非分页）

权限：`STUDENT`

兼容路径：

- GET `/api/students/my-courses`

响应：`ApiResponse<Course[]>`

## 4. 课程进度

### GET `/api/students/courses/{id}/progress` 获取课程学习进度

权限：`STUDENT`

响应：`ApiResponse<StudentCourseProgressResponse>`，核心字段：

```json
{
  "courseId": 2,
  "progress": 82.5,
  "totalStudyMinutes": 1200,
  "weeklyStudyMinutes": 180,
  "lastStudiedLessonTitle": "第1讲"
}
```

说明：

- `progress`：当前实现返回的进度值通常是 `0-100`（百分比）。

## 5. 课节详情与完成标记

### GET `/api/students/lessons/{lessonId}` 获取课节详情与我的进度

权限：`STUDENT`

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/students/lessons/123
```

响应：`ApiResponse<StudentLessonDetailResponse>`（示例）：

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "lesson": { "id": 123, "title": "第1讲", "courseId": 2 },
    "progress": { "lessonId": 123, "studentId": 1001, "progress": 0.75 }
  }
}
```

### POST `/api/students/lessons/{lessonId}/complete` 标记课节完成

权限：`STUDENT`

curl：

```bash
curl -X POST -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/students/lessons/123/complete
```

### POST `/api/students/lessons/{lessonId}/incomplete` 取消课节完成（将进度置 0）

权限：`STUDENT`

curl：

```bash
curl -X POST -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/students/lessons/123/incomplete
```

## 6. 作业列表与待办

### GET `/api/students/assignments` 查询我的作业（分页+过滤）

权限：`STUDENT`

Query：

- `page`：默认 1
- `size`：默认 10
- `courseId`：可选
- `status`：可选（以 `Assignment.status` 实现为准）
- `q`：可选关键词
- `includeHistory`：是否包含历史课程作业，默认 `false`
- `onlyPending`：是否仅返回待完成作业，默认 `false`
- `excludeCourseBound`：是否排除“课程绑定作业”（课节完成口径相关），默认 `false`

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/students/assignments?page=1&size=10&includeHistory=false&onlyPending=true"
```

响应：`ApiResponse<PageResult<Assignment>>`

### GET `/api/students/my/assignments/pending` 我待完成的作业（简单列表）

权限：`STUDENT`

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/students/my/assignments/pending
```

响应：`ApiResponse<Assignment[]>`

## 7. 我的提交

### GET `/api/students/my-submissions` 分页获取我的提交

权限：`STUDENT`

Query：

- `page`：默认 1
- `size`：默认 10
- `courseId`：可选（当前控制器签名保留该参数，但服务实现未使用，暂不保证可过滤）

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/students/my-submissions?page=1&size=10"
```

响应：`ApiResponse<PageResult<Submission>>`

## 8. 学生分析（聚合）

### GET `/api/students/analysis` 获取成绩分析聚合数据

权限：`STUDENT`

Query：

- `range`：可选；当前实现支持 `7d|30d|term`，为空或非法值时默认按 `30d` 处理

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/students/analysis?range=30d"
```

响应：`ApiResponse<StudentAnalysisResponse>`（示例，数值口径以实现为准）：

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "kpi": { "avgScore": 88.2, "completionRate": 93.0, "studyHours": 20, "activeDays": 10 },
    "radar": { "invest": 75.0, "quality": 75.0, "mastery": 75.0, "stability": 75.0, "growth": 75.0 },
    "trends": { "score": [], "completion": [], "hours": [] },
    "recentGrades": []
  }
}
```

说明：

- `completionRate`：当前实现返回 `0-100` 的百分比数值。

## 9. 课程参与者

### GET `/api/students/courses/{id}/participants` 获取课程参与者（教师与学生）

权限：`STUDENT` 或 `TEACHER`

Query：

- `keyword`：可选关键词过滤

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/students/courses/2/participants?keyword=tom"
```

## 10. Troubleshooting

- 401：未登录或 token 失效
- 403：非学生角色、或访问无权限课程/课节
- 404：课程/课节不存在
