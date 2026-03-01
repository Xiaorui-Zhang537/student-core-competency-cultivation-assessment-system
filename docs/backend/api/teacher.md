---
title: 教师端 API（Teacher）
description: 教师分析指标、课程学生表现与导出、五维雷达与权重、学生画像、联系人与进度维护
outline: [2, 3]
---

# 教师端 API（Teacher）

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

本项目后端 `context-path=/api`，下文接口路径均以 `/api/...` 展示。

## 1. 通用约定

- 认证：`Authorization: Bearer <token>`
- 响应：统一返回 `ApiResponse<T>`（`code/message/data`）
- 权限：本页接口由 `TEACHER` / `ADMIN` 使用（控制器层有角色校验）

## 2. 教学分析（Analytics）

### GET `/api/teachers/analytics/course/{courseId}` 课程分析

Query：

- `timeRange`：可选（字符串；口径以服务实现为准）

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/teachers/analytics/course/2?timeRange=last_30_days"
```

### GET `/api/teachers/analytics/assignment/{assignmentId}` 作业分析

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/teachers/analytics/assignment/301
```

### GET `/api/teachers/analytics/class-performance/{courseId}` 班级整体表现

Query：

- `timeRange`：默认 `last_30_days`

### GET `/api/teachers/analytics/course/{courseId}/students` 课程学生表现（分页+过滤）

Query：

- `page`：默认 1
- `size`：默认 20
- `search`：可选关键词
- `sortBy`：默认 `name`（可选：`name|grade|progress|lastActive|activity`）
- `activity`：可选筛选
- `grade`：可选筛选
- `progress`：可选筛选

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/teachers/analytics/course/2/students?page=1&size=20&sortBy=progress"
```

### GET `/api/teachers/analytics/course/{courseId}/students/export` 导出课程学生表现（CSV）

说明：返回二进制 CSV（`Content-Disposition: attachment`）。

curl：

```bash
curl -L -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/teachers/analytics/course/2/students/export?sortBy=progress" \
  -o course_2_students.csv
```

## 3. 五维能力雷达（教师端）

本节接口由 `AbilityAnalyticsController` 提供，路径前缀为 `/api/teachers/ability`。

### GET `/api/teachers/ability/radar` 获取五维雷达

Query（按需传参）：

- `courseId`（必填）
- `classId`（可选）
- `studentId`（可选；不传通常表示按班级/课程聚合的对比基准）
- `startDate` / `endDate`（可选，格式 `YYYY-MM-DD`）

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/teachers/ability/radar?courseId=2&studentId=1001&startDate=2026-03-01&endDate=2026-03-31"
```

### GET `/api/teachers/ability/weights` 读取课程五维权重

Query：`courseId`（必填）

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/teachers/ability/weights?courseId=2"
```

### PUT `/api/teachers/ability/weights` 更新课程五维权重

请求 Body：`UpdateAbilityWeightsRequest`

```json
{
  "courseId": 2,
  "weights": { "invest": 0.2, "quality": 0.2, "mastery": 0.2, "stability": 0.2, "growth": 0.2 }
}
```

响应：`ApiResponse<AbilityWeightsResponse>`

### GET `/api/teachers/ability/radar/export` 导出雷达数据（CSV）

Query：同 `GET /api/teachers/ability/radar`，额外支持：

- `scope`：默认 `single`；可选 `single|all`

curl：

```bash
curl -L -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/teachers/ability/radar/export?courseId=2&studentId=1001&scope=single" \
  -o ability_radar.csv
```

### POST `/api/teachers/ability/radar/compare` 双区间对比（A/B）

请求 Body：`AbilityCompareQuery`（示例）：

```json
{
  "courseId": 2,
  "studentId": 1001,
  "startDateA": "2026-03-01",
  "endDateA": "2026-03-31",
  "startDateB": "2026-04-01",
  "endDateB": "2026-04-30",
  "includeClassAvg": true
}
```

### POST `/api/teachers/ability/radar/compare/export` 导出对比结果（CSV）

curl：

```bash
curl -L -H "Authorization: Bearer $TOKEN" \
  -X POST -H "Content-Type: application/json" \
  -d '{"courseId":2,"studentId":1001,"startDateA":"2026-03-01","endDateA":"2026-03-31","startDateB":"2026-04-01","endDateB":"2026-04-30"}' \
  "http://localhost:8080/api/teachers/ability/radar/compare/export?scope=single" \
  -o ability_radar_compare.csv
```

### POST `/api/teachers/ability/dimension-insights` 维度解析（结构化洞察）

请求 Body：同 `AbilityCompareQuery`

## 4. 学生画像与详情（教师视角）

说明：该组接口由 `TeacherStudentController` 提供，路径前缀为 `/api/teachers/students`。服务端会校验“教师与学生存在课程交集”，否则返回 403。

### GET `/api/teachers/students/{studentId}` 获取学生概况

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/teachers/students/1001
```

### GET `/api/teachers/students/{studentId}/courses` 获取学生参与课程列表

### GET `/api/teachers/students/basic` 课程学生基础列表（用于下拉）

Query：

- `courseId`（必填）
- `page`：默认 1
- `size`：默认 10000
- `keyword`：可选

### GET `/api/teachers/students/{studentId}/activity` 活跃度与最近学习

Query：

- `days`：默认 7
- `limit`：默认 5

### GET `/api/teachers/students/{studentId}/alerts` 风险预警

### GET `/api/teachers/students/{studentId}/recommendations` 个性化学习建议

Query：

- `limit`：默认 6

## 5. 课程进度维护

### POST `/api/teachers/courses/{courseId}/students/{studentId}/progress/reset` 重置学生课程进度

curl：

```bash
curl -X POST -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/teachers/courses/2/students/1001/progress/reset
```

## 6. 教师我的课程与联系人

### GET `/api/teachers/my-courses` 我授课的课程列表

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/teachers/my-courses
```

响应：`ApiResponse<Course[]>`

### GET `/api/teachers/contacts` 教师联系人聚合（按课程分组返回学生）

Query：

- `keyword`：可选关键词（用于筛选学生）

curl：

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/teachers/contacts?keyword=tom"
```

响应：`ApiResponse<TeacherContactsResponse>`（示例）：

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "groups": [
      {
        "courseId": 101,
        "courseTitle": "高等数学",
        "students": [{ "id": 2001, "username": "tom", "avatar": "/u/2001.png" }]
      }
    ]
  }
}
```

## 7. Troubleshooting

- 401：未登录或 token 失效
- 403：非教师/管理员角色，或教师与学生无课程交集
- 400：参数不合法（例如日期区间、权重校验失败等）
