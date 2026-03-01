---
title: 评分 API（Grade）
description: 成绩录入、查询、发布、统计等接口（后端 /api/grades）
---

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

本项目后端 `context-path=/api`，下文接口路径均以 `/api/...` 展示。

## 通用约定

认证：除少数公开接口外，均需携带 `Authorization: Bearer <access_jwt>`。

响应封装：统一返回 `ApiResponse<T>`。

```json
{ "code": 200, "message": "Success", "data": {} }
```

时间格式：`yyyy-MM-dd HH:mm:ss`（例如 `2026-03-01 12:00:00`）。

## 数据结构：Grade（核心字段）

`Grade` 为成绩实体，常用字段如下（未列出字段一般由后端生成或可忽略）。

```json
{
  "id": 1,
  "studentId": 1001,
  "assignmentId": 88,
  "submissionId": 123,
  "graderId": 11,
  "score": 95.0,
  "maxScore": 100.0,
  "percentage": 95.0,
  "gradeLevel": "A",
  "feedback": "不错，但第2题需要补充推导",
  "status": "draft",
  "strengths": "结构清晰",
  "improvements": "建议补充例子",
  "allowResubmit": false,
  "resubmitUntil": "2026-03-05 23:59:59",
  "rubricJson": "{...}",
  "regradeReason": "复核更正",
  "createdAt": "2026-03-01 12:00:00",
  "updatedAt": "2026-03-01 12:00:00",
  "publishedAt": "2026-03-01 12:30:00"
}
```

`status` 可选值：`draft`（草稿未发布）、`published`（已发布）、`returned`（打回重做）。

## 创建/更新/删除

### POST `/api/grades` 创建成绩（Upsert）

权限：`TEACHER` / `ADMIN`

说明：

- 该接口直接接收 `Grade` JSON，不存在 `GradeRequest` 或 `publishImmediately` 之类字段。
- 以 `(studentId, assignmentId)` 为准做 upsert：若该学生该作业已有成绩，则本次会更新已有记录。
- `maxScore` 若不传，会尝试用作业的 `Assignment.maxScore` 兜底。
- `allowResubmit` 若不传，后端会兜底为 `false`（避免数据库 NOT NULL 错误）。

请求 Body（示例）：

```json
{
  "studentId": 1001,
  "assignmentId": 88,
  "submissionId": 123,
  "score": 95,
  "maxScore": 100,
  "feedback": "Great job",
  "strengths": "结构清晰",
  "improvements": "建议补充论据"
}
```

响应（示例）：

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "studentId": 1001,
    "assignmentId": 88,
    "submissionId": 123,
    "graderId": 11,
    "score": 95,
    "maxScore": 100,
    "percentage": 95.0,
    "gradeLevel": "A",
    "status": "draft",
    "allowResubmit": false,
    "createdAt": "2026-03-01 12:00:00",
    "updatedAt": "2026-03-01 12:00:00"
  }
}
```

curl：

```bash
curl -X POST 'http://localhost:8080/api/grades' \
  -H 'Authorization: Bearer <access_jwt>' \
  -H 'Content-Type: application/json' \
  -d '{"studentId":1001,"assignmentId":88,"submissionId":123,"score":95,"maxScore":100,"feedback":"Great job"}'
```

### PUT `/api/grades/{id}` 更新成绩

权限：`TEACHER` / `ADMIN`，且必须是该成绩对应作业的创建教师（否则 403）。

说明：更新使用“非空字段覆盖”策略，未传字段不会被置空。

请求 Body（示例，仅更新部分字段）：

```json
{ "score": 96, "feedback": "更正：第3题也写得不错" }
```

响应：`ApiResponse<Grade>`

### DELETE `/api/grades/{id}` 删除成绩（软删）

权限：`TEACHER` / `ADMIN`，且必须是该成绩对应作业的创建教师（否则 403）。

响应：

```json
{ "code": 200, "message": "Success", "data": null }
```

## 发布/打回/批量

### POST `/api/grades/{id}/publish` 发布成绩

权限：`TEACHER` / `ADMIN`，且必须是该成绩对应作业的创建教师（否则 403）。

响应：

```json
{ "code": 200, "message": "Success", "data": null }
```

### POST `/api/grades/batch-publish` 批量发布成绩

权限：`TEACHER` / `ADMIN`

请求 Body：成绩 ID 数组

```json
[1, 2, 3]
```

响应（示例）：

```json
{
  "code": 200,
  "data": {
    "successCount": 3,
    "failCount": 0,
    "errors": []
  }
}
```

### POST `/api/grades/{id}/return` 打回重做

权限：`TEACHER` / `ADMIN`，且必须是该成绩对应作业的创建教师（否则 403）。

请求 Body（可选）：

```json
{
  "reason": "请补充实验过程",
  "resubmitUntil": "2026-03-05 23:59:59"
}
```

说明：`resubmitUntil` 支持 `2026-03-05 23:59:59` 或 `2026-03-05T23:59:59`。

响应：成功返回 200，无 data。

### POST `/api/grades/batch` 批量评分

权限：`TEACHER` / `ADMIN`

请求 Body：`Grade[]`（每项至少建议包含 `studentId/assignmentId/score`，其余可选）

响应：`ApiResponse<Map>`（结构以服务实现为准）

## 查询

### GET `/api/grades/{id}` 成绩详情

权限：`TEACHER` / `STUDENT` / `ADMIN`

响应：`ApiResponse<Grade>`

### GET `/api/grades/{id}/history` 成绩历史（简化）

权限：`TEACHER` / `ADMIN`

响应（示例）：

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "score": 95,
      "maxScore": 100,
      "percentage": 95.0,
      "gradeLevel": "A",
      "feedback": "Great job",
      "strengths": "结构清晰",
      "improvements": "建议补充论据",
      "status": "published",
      "gradedAt": "2026-03-01 12:30:00",
      "graderName": "teacherA"
    }
  ]
}
```

### GET `/api/grades/student/{studentId}` 学生成绩列表（非分页）

权限：`TEACHER` / `STUDENT` / `ADMIN`（学生仅可查询本人）

响应：`ApiResponse<Grade[]>`

### GET `/api/grades/student/{studentId}/page` 学生成绩分页

权限：`TEACHER` / `STUDENT` / `ADMIN`（学生仅可查询本人）

Query：

- `page`：默认 1
- `size`：默认 10
- `courseId`：可选

响应：`ApiResponse<PageResult<GradeListItem>>`

### GET `/api/grades/assignment/{assignmentId}` 作业成绩列表（非分页）

权限：`TEACHER` / `ADMIN`

响应：`ApiResponse<Grade[]>`

### GET `/api/grades/assignment/{assignmentId}/page` 作业成绩分页

权限：`TEACHER` / `ADMIN`

Query：`page`（默认 1）、`size`（默认 10）

响应：`ApiResponse<PageResult<Grade>>`

### GET `/api/grades/student/{studentId}/assignment/{assignmentId}` 学生某作业成绩

权限：`TEACHER` / `STUDENT` / `ADMIN`（学生仅可查询本人）

响应：`ApiResponse<Grade>`

## 统计/分析/导出

### GET `/api/grades/student/{studentId}/average` 学生平均分

权限：`TEACHER` / `STUDENT` / `ADMIN`（学生仅可查询本人）

响应：`ApiResponse<number>`（BigDecimal）

### GET `/api/grades/student/{studentId}/course/{courseId}/average` 学生某课程平均分

权限：`TEACHER` / `STUDENT` / `ADMIN`（学生仅可查询本人）

响应：`ApiResponse<number>`

### GET `/api/grades/assignment/{assignmentId}/average` 作业平均分

权限：`TEACHER` / `ADMIN`

响应：`ApiResponse<number>`

### GET `/api/grades/assignment/{assignmentId}/distribution` 作业成绩分布

权限：`TEACHER` / `ADMIN`

响应（示例）：

```json
{
  "code": 200,
  "data": [
    { "grade_level": "A", "count": 12, "percentage": 30.0 },
    { "grade_level": "B", "count": 18, "percentage": 45.0 }
  ]
}
```

### GET `/api/grades/course/{courseId}/statistics` 课程成绩统计

权限：`TEACHER` / `ADMIN`

响应：`ApiResponse<GradeStatsResponse>`

### GET `/api/grades/student/{studentId}/statistics` 学生成绩统计

权限：`TEACHER` / `STUDENT` / `ADMIN`（学生仅可查询本人）

响应（示例）：

```json
{
  "code": 200,
  "data": {
    "totalGrades": 12,
    "gradedCount": 12,
    "averageScore": 84.50,
    "highestScore": 96.00,
    "lowestScore": 72.00
  }
}
```

### GET `/api/grades/pending` 待评分列表（教师）

权限：`TEACHER` / `ADMIN`

Query：`page`（默认 1）、`size`（默认 10）

响应：`ApiResponse<PageResult<Map>>`

说明：当前实现返回的 `Map` 字段较简化（含 `gradeId/studentId/assignmentId/score/status/createdAt` 等，部分 `studentName/assignmentTitle` 为占位字段）。

### GET `/api/grades/student/{studentId}/trend` 学生成绩趋势

权限：`TEACHER` / `STUDENT` / `ADMIN`（学生仅可查询本人）

Query：

- `courseId`：可选
- `days`：默认 30

响应：`ApiResponse<Map[]>`（示例字段：`assignmentTitle/score/percentage/grade_level/gradedAt`）

### GET `/api/grades/student/{studentId}/gpa` 计算 GPA

权限：`TEACHER` / `STUDENT` / `ADMIN`（学生仅可查询本人）

Query：`courseId`（可选）

响应：`ApiResponse<number>`

### GET `/api/grades/ranking` 课程/作业排名

权限：`TEACHER` / `ADMIN`

Query：

- `courseId`：必填
- `assignmentId`：可选（传入则返回该作业的排名；否则返回课程维度排名）

响应：`ApiResponse<Map[]>`

示例：

```bash
curl 'http://localhost:8080/api/grades/ranking?courseId=2&assignmentId=88' \
  -H 'Authorization: Bearer <access_jwt>'
```

### GET `/api/grades/student/{studentId}/ranking` 学生课程内排名

权限：`TEACHER` / `STUDENT` / `ADMIN`（学生仅可查询本人）

Query：

- `courseId`：必填

响应：`ApiResponse<Map>`

示例：

```bash
curl 'http://localhost:8080/api/grades/student/1001/ranking?courseId=2' \
  -H 'Authorization: Bearer <access_jwt>'
```

### POST `/api/grades/export` 导出成绩（返回数据集）

权限：`TEACHER` / `ADMIN`

请求 Body（示例）：

```json
{ "courseId": 2, "assignmentId": 88, "studentId": 1001, "format": "excel" }
```

说明：

- `format` 默认 `excel`。
- 当前实现不直接生成文件，而是返回导出数据 `data`（数组，每项为一行）。

响应（示例，截断）：

```json
{
  "code": 200,
  "data": {
    "format": "excel",
    "message": "Grades exported successfully.",
    "data": [
      {
        "studentId": 1001,
        "studentName": "alice",
        "studentNo": "S20250001",
        "courseTitle": "高等数学",
        "assignmentTitle": "HW1",
        "score": 95,
        "maxScore": 100,
        "percentage": 95.0,
        "grade_level": "A",
        "status": "published",
        "createdAt": "2026-03-01 12:00:00"
      }
    ]
  }
}
```

## 反馈与重评

### POST `/api/grades/{id}/feedback` 添加评语

权限：`TEACHER` / `ADMIN`，且必须是该成绩对应作业的创建教师（否则 403）。

请求 Body：

```json
{ "feedback": "建议把结论再总结一下" }
```

响应：成功返回 200，无 data。

### POST `/api/grades/{id}/regrade` 重评分

权限：`TEACHER` / `ADMIN`，且必须是该成绩对应作业的创建教师（否则 403）。

请求 Body：

```json
{ "newScore": 96, "reason": "复核更正" }
```

响应：成功返回 200，无 data。

## 常见错误与排查

- 400：参数非法。创建/更新作业成绩时，至少确保 `studentId/assignmentId` 存在；`maxScore` 不能为 0；建议传入 `score`。
- 401：未登录或 token 过期。
- 403：非教师/管理员，或当前教师并非该成绩对应作业的创建者。
- 404：成绩或作业不存在。
