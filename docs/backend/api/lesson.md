---
title: 课时 API（Lesson）
description: 课时 CRUD、内容与资料绑定、学习进度、笔记与评分
outline: [2, 3]
---

# 课时 API（Lesson）

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

本项目后端 `context-path=/api`，下文接口路径均以 `/api/...` 展示。

## 1. 通用约定

响应封装：除少数二进制接口外，统一返回 `ApiResponse<T>`。

角色权限（按接口注解）：

- 课时维护（创建/更新/删除/发布/顺序/批量/复制/内容绑定）：`TEACHER` / `ADMIN`
- 学习进度、笔记、评分、最近学习：`STUDENT`
- 教师/管理员可查询学生进度或指定学生笔记（只读）

## 2. 数据结构

### Lesson（核心字段）

```json
{
  "id": 1,
  "courseId": 2,
  "title": "第一节 课程介绍",
  "description": "本节介绍课程目标",
  "content": "<p>...</p>",
  "videoUrl": "https://example.com/video.mp4",
  "allowScrubbing": true,
  "allowSpeedChange": true,
  "orderIndex": 1,
  "chapterId": 10,
  "status": "draft"
}
```

### LessonProgress（核心字段）

```json
{
  "studentId": 1001,
  "courseId": 2,
  "lessonId": 1,
  "progress": 75.5,
  "completed": false,
  "studyDuration": 45,
  "lastPosition": 1800,
  "status": "in_progress",
  "notes": "需要复习",
  "rating": 4
}
```

## 3. 课时 CRUD（教师/管理员）

### POST `/api/lessons` 创建课时

权限：`TEACHER` / `ADMIN`

请求 Body（示例）：

```json
{ "courseId": 2, "title": "第一节 课程介绍", "description": "本节介绍课程目标", "orderIndex": 1, "chapterId": 10 }
```

响应：`ApiResponse<Lesson>`

### GET `/api/lessons/{id}` 课时详情

响应：`ApiResponse<Lesson>`

### PUT `/api/lessons/{id}` 更新课时

权限：`TEACHER` / `ADMIN`

请求 Body：`Lesson`

响应：`ApiResponse<Lesson>`

### DELETE `/api/lessons/{id}` 删除课时

权限：`TEACHER` / `ADMIN`

响应：成功返回 200，无 data。

## 4. 内容与资料绑定

### PUT `/api/lessons/{id}/content` 设置课时内容（视频与资料）

权限：`TEACHER` / `ADMIN`

请求 Body（示例）：

```json
{
  "videoUrl": "https://example.com/video.mp4",
  "allowScrubbing": true,
  "allowSpeedChange": false,
  "materialFileIds": [9876, 9999]
}
```

说明：`materialFileIds` 为文件 ID 列表（先通过文件上传接口获得 `fileId`）。

响应：`ApiResponse<Map>`（包含 `lessonId/videoUrl/materialFileIds/allowScrubbing/allowSpeedChange`）

### GET `/api/lessons/{id}/materials` 获取课时资料文件

响应：`ApiResponse<FileRecord[]>`

## 5. 课程内列表与统计

### GET `/api/lessons/course/{courseId}` 课程下所有课时

响应：`ApiResponse<Lesson[]>`

### GET `/api/lessons/course/{courseId}/page` 课程下课时分页

Query：`page`（默认 1）、`size`（默认 10）

响应：`ApiResponse<PageResult<Lesson>>`

### GET `/api/lessons/popular` 热门课时

Query：`limit`（默认 10）

响应：`ApiResponse<Lesson[]>`

### GET `/api/lessons/search` 搜索课时

Query：

- `keyword`：必填
- `courseId`：可选

响应：`ApiResponse<Lesson[]>`

### GET `/api/lessons/course/{courseId}/statistics` 课时统计

权限：`TEACHER` / `ADMIN`

响应：`ApiResponse<Map>`

## 6. 发布/顺序/批量/复制（教师/管理员）

### POST `/api/lessons/{id}/publish` 发布课时

### POST `/api/lessons/{id}/unpublish` 取消发布

### PUT `/api/lessons/{id}/order` 调整顺序

请求 Body：

```json
{ "order": 3 }
```

### PUT `/api/lessons/batch-status` 批量更新状态

请求 Body：

```json
{ "lessonIds": [1, 2, 3], "status": "published" }
```

响应：`ApiResponse<Map>`

### POST `/api/lessons/{id}/copy` 复制到其它课程

请求 Body：

```json
{ "targetCourseId": 2 }
```

响应：`ApiResponse<Lesson>`

## 7. 学习进度（学生）

### GET `/api/lessons/{lessonId}/progress` 获取我的课时进度

权限：`STUDENT`（教师/管理员可通过 query 指定 studentId 获取）

Query：`studentId` 可选（仅教师/管理员）

响应：`ApiResponse<LessonProgress>`

### POST `/api/lessons/{lessonId}/progress` 更新进度

权限：`STUDENT`

请求 Body（示例）：

```json
{ "progress": 75.5, "studyTime": 10, "lastPosition": 1800 }
```

说明：`studyTime` 与 `lastPosition` 均为整数；`progress` 为数值（百分比）。

响应：成功返回 200，无 data。

### POST `/api/lessons/{lessonId}/complete` 标记完成

权限：`STUDENT`

响应：成功返回 200，无 data。

### GET `/api/lessons/course/{courseId}/student-progress` 课程内所有课时进度

权限：`STUDENT` / `TEACHER` / `ADMIN`

Query：`studentId` 可选（仅教师/管理员）

响应：`ApiResponse<LessonProgress[]>`

### GET `/api/lessons/course/{courseId}/progress-percentage` 课程整体进度（百分比）

权限：`STUDENT` / `TEACHER` / `ADMIN`

Query：`studentId` 可选（仅教师/管理员）

响应：`ApiResponse<number>`

## 8. 最近学习、笔记与评分（学生）

### GET `/api/lessons/recent-studied` 最近学习课时

权限：`STUDENT`

Query：`limit`（默认 5）

响应：`ApiResponse<Map[]>`

### GET `/api/lessons/{lessonId}/notes` 获取我的笔记

权限：`STUDENT`

响应（示例）：

```json
{ "code": 200, "data": { "notes": "..." } }
```

### POST `/api/lessons/{lessonId}/notes` 保存笔记

权限：`STUDENT`

请求 Body：

```json
{ "notes": "这个章节讲的很详细，需要多复习几遍" }
```

响应：成功返回 200，无 data。

### GET `/api/lessons/{lessonId}/notes/by-student?studentId=...` 教师/管理员读取学生笔记

权限：`TEACHER` / `ADMIN`

响应：`ApiResponse<Map>`（包含 `studentId/lessonId/notes/updatedAt`）

### POST `/api/lessons/{lessonId}/rating` 课时评分

权限：`STUDENT`

请求 Body：

```json
{ "rating": 4 }
```

说明：评分为空会返回 400；评分范围校验以服务端为准（进度实体约定 1~5）。

补充说明：
- 该接口写入的是 `lesson_progress.rating`（学生对单个课时的学习反馈）。
- 写入成功后，服务端会自动按“每个学生在该课程下已评分课时的平均分”重新聚合课程口碑分，并回写 `courses.rating / courses.review_count`。
- 目前没有单独的“课程打星”接口；课程评分展示来自这里的聚合结果。

响应：成功返回 200，无 data。

## 9. 常见错误与排查

- 400：顺序/评分/笔记为空等校验失败。
- 401：未登录或 token 过期。
- 403：非学生调用进度/笔记/评分；或非教师/管理员调用维护接口。
- 404：课时不存在。
