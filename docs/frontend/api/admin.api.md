# `admin.api.ts`（管理员端）

对应文件：`frontend/src/api/admin.api.ts`。

> 说明：前端 Axios 拦截器会将后端统一返回 `{ code, message, data }` 自动解包为 `data`；文件下载类接口使用 `responseType: 'blob'`。

## 1. 仪表盘

- `adminApi.getDashboardOverview(days?: number)`
  - `GET /api/admin/dashboard/overview`

## 2. 课程

- `adminApi.pageCourses(params)`
  - `GET /api/admin/courses`
- `adminApi.getCourse(id)`
  - `GET /api/admin/courses/{id}`

## 3. 用户管理

- `adminApi.pageUsers(params)`
  - `GET /api/admin/users`
- `adminApi.createUser(data)`
  - `POST /api/admin/users`
- `adminApi.updateUserRole(id, role)`
  - `PUT /api/admin/users/{id}/role`
- `adminApi.updateUserStatus(id, status)`
  - `PUT /api/admin/users/{id}/status`
- `adminApi.sendResetPasswordEmail(id, lang?)`
  - `POST /api/admin/users/{id}/password/reset-email`

## 4. 学生/教师数据中心

- `adminApi.getStudentDetail(id)`
  - `GET /api/admin/people/students/{id}`
- `adminApi.getTeacherDetail(id)`
  - `GET /api/admin/people/teachers/{id}`

## 5. 报告中心

- `adminApi.pageAbilityReports(params)`
  - `GET /api/admin/ability-reports`
- `adminApi.getAbilityReport(id)`
  - `GET /api/admin/ability-reports/{id}`
- `adminApi.pageReports(params)`
  - `GET /api/reports`（管理员举报列表）
- `adminApi.updateReportStatus(id, status)`
  - `PUT /api/reports/{id}/status`

## 6. 分析看板

- `adminApi.getAnalyticsSeriesOverview(days?: number)`
  - `GET /api/admin/analytics/series/overview`

## 7. 社区治理

- `adminApi.pageCommunityPosts(params)`
  - `GET /api/admin/community/posts`
- `adminApi.moderatePost(id, data)`
  - `PUT /api/admin/community/posts/{id}`
- `adminApi.pageCommunityComments(params)`
  - `GET /api/admin/community/comments`
- `adminApi.moderateComment(id, data)`
  - `PUT /api/admin/community/comments/{id}`

## 8. 导出中心（CSV）

- `adminApi.getExportCapabilities()`
  - `GET /api/admin/exports/capabilities`
- `adminApi.exportUsersCsv(params)`
  - `GET /api/admin/exports/users.csv`（Blob）
- `adminApi.exportAbilityReportsCsv(params)`
  - `GET /api/admin/exports/ability-reports.csv`（Blob）

