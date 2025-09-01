# 课程与课时 API（Course & Lesson）

> 以 Swagger 为准：`http://localhost:8080/api/swagger-ui.html`

## 1. 课程列表与检索
- `GET /api/courses`：分页列表（`page/size/sort/query/status/teacherId`）
请求：
```
GET /api/courses?page=1&size=10&query=AI
Authorization: Bearer <token>
```
响应（成功）：
```json
{ "code": 200, "message": "OK", "data": { "items": [{"id":1,"title":"AI"}], "total": 100, "page":1, "size":10 }}
```

- `GET /api/courses/search`：关键词搜索（`query/page/size`）
- `GET /api/courses/popular`：热门课程
- `GET /api/courses/recommended`：推荐课程
- `GET /api/courses/category/{category}`：按分类

## 2. 课程 CRUD
- `GET /api/courses/{id}`：详情
- `POST /api/courses`：创建
请求：
```json
{ "title":"Intro", "description":"...", "category":"CS", "tags":["ai"] }
```
响应：
```json
{ "code": 200, "data": { "id": 2, "title": "Intro", "status": "DRAFT" } }
```
- `PUT /api/courses/{id}`：更新
- `DELETE /api/courses/{id}`：删除
- 批量状态：`PUT /api/courses/batch-status`
- 发布/下线：`POST /api/courses/{id}/publish`、`POST /api/courses/{id}/unpublish`

## 3. 选课与名单
- `POST /api/courses/{id}/enroll`：选课
- `DELETE /api/courses/{id}/enroll`：退课
- `GET /api/courses/{id}/enrollment-status`：我的选课状态
- `GET /api/courses/{id}/students`：课程学生（教师）
- `POST /api/courses/{id}/students/invite`：邀请学生加入
- `DELETE /api/courses/{id}/students/{studentId}`：移除学生

## 4. 课时（示例端点，具体以 Swagger 为准）
- `GET /api/lessons?courseId=...`：课程下课时列表
- `POST /api/lessons`：创建课时
- `PUT /api/lessons/{id}`、`DELETE /api/lessons/{id}`

## 5. 返回码对照
- 200：成功
- 400：非法参数（分页/过滤）
- 401：未认证
- 403：无权限（非授课教师执行敏感操作）
- 404：资源不存在
- 409：状态冲突（重复发布/非法状态迁移）
- 5xx：服务端错误

---

# 前端对接（course.api.ts）

## 1. 方法映射（节选）
- `getCourses(params)` ↔ `GET /api/courses`
- `getCourseById(id)` ↔ `GET /api/courses/{id}`
- `createCourse(data)` ↔ `POST /api/courses`
- `updateCourse(id, data)` ↔ `PUT /api/courses/{id}`
- `deleteCourse(id)` ↔ `DELETE /api/courses/{id}`
- `searchCourses(params)` ↔ `GET /api/courses/search`
- `getPopularCourses()` ↔ `GET /api/courses/popular`
- `getRecommendedCourses()` ↔ `GET /api/courses/recommended`
- `getCoursesByCategory(category, params)` ↔ `GET /api/courses/category/{category}`
- `enrollInCourse(id)` / `unenrollFromCourse(id)` ↔ 选课/退课
- `getCourseStudents(courseId, params)` ↔ 学生名单
- `inviteStudents(courseId, studentIds)` / `removeStudent(courseId, studentId)`
- `publishCourse(id)` / `unpublishCourse(id)`
- `updateBatchStatus(data)`

## 2. Store 与视图（示意）
- 教师端：`ManageCourseView.vue` ↔ `useCourseStore` ↔ `course.api.ts`
- 学生端：`CourseDetailView.vue` ↔ `useCourseStore` ↔ `course.api.ts`

## 3. 最小示例（分页请求）
```ts
import { courseApi } from '@/api/course.api'

const { data } = await courseApi.getCourses({ page: 1, size: 10, query: 'AI' })
// data: PaginatedResponse<Course>
```

## 4. 常见错误与排查
- 401：未登录；检查 token 与拦截器
- 403：非课程教师进行敏感操作
- 400：分页参数/状态非法；检查请求参数

## 5. curl 示例
分页查询课程：
```bash
curl 'http://localhost:8080/api/courses?page=1&size=10&query=AI' \
  -H 'Authorization: Bearer <access_jwt>'
```

获取课程详情：
```bash
curl 'http://localhost:8080/api/courses/2' \
  -H 'Authorization: Bearer <access_jwt>'
```

创建课程：
```bash
curl -X POST 'http://localhost:8080/api/courses' \
  -H 'Authorization: Bearer <access_jwt>' \
  -H 'Content-Type: application/json' \
  -d '{"title":"Intro","description":"...","category":"CS","tags":["ai"]}'
```

选课/退课：
```bash
curl -X POST 'http://localhost:8080/api/courses/2/enroll' \
  -H 'Authorization: Bearer <access_jwt>'

curl -X DELETE 'http://localhost:8080/api/courses/2/enroll' \
  -H 'Authorization: Bearer <access_jwt>'
```

## 6. 错误示例
非法分页参数：
```json
{ "code": 400, "message": "Invalid page/size" }
```

未授权：
```json
{ "code": 401, "message": "Unauthorized" }
```
