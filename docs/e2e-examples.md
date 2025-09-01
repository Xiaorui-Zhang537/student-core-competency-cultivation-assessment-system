# 端到端样例（API → 前端组件 E2E）

面向新手，逐步演示“一个后端接口，如何被前端消费并展示”。每个示例都覆盖：接口 → 类型 → 前端 API 封装 → Store → 视图/组件 → 交互与验证。

---

## 示例 1：登录与会话维护（POST /api/auth/login）

- 后端接口（Swagger 可测）：`POST /api/auth/login`
  - 请求体（示例）：`{ username, password }`
  - 响应体（示例）：`{ code: 200, data: { token, refreshToken, user } }`
- 前端对应文件：
  - 请求封装：`src/api/auth.api.ts` → `authApi.login(data)`
  - Axios 拦截器：`src/api/config.ts`
    - 登录/注册请求不附带旧 token
    - 其它请求附带 `Authorization: Bearer <token>`
    - 401 时清 token 并跳转登录页
  - Store：`src/stores/auth.ts`（命名可能为 `useAuthStore`）
    - `login` action 调用 `authApi.login`
    - 成功后保存 token 与 user
  - 视图：`src/features/auth/views/LoginView.vue`
    - 表单校验 → 触发 Store `login`
    - 成功后路由跳转至首页/仪表盘
- 验证要点：
  - 登录成功后，刷新页面仍保持登录态（token 持久化）
  - 未登录访问受限路由，路由守卫应跳转到登录页（见 `router/index.ts`）

时序图：
```mermaid
sequenceDiagram
  participant U as User
  participant V as Vue(LoginView)
  participant A as Axios
  participant C as Controller(/auth)
  participant S as Service
  U->>V: 输入用户名密码
  V->>A: POST /api/auth/login
  A->>C: 请求
  C->>S: 校验并签发JWT
  S-->>C: { token, refreshToken, user }
  C-->>A: ApiResponse< AuthResponse >
  A-->>V: 响应
  V->>V: 保存token/用户信息
  V-->>U: 跳转首页/仪表盘
```

---

## 示例 2：课程列表分页（GET /api/courses?page=&size=&q=）

- 后端接口：`GET /api/courses`
  - 查询参数：`page`、`size`、`q`
  - 响应体：`{ code: 200, data: PageResult<CourseDto> }`
- 前端对应文件：
  - 请求封装：`src/api/course.api.ts`（示例方法：`listCourses(params)`）
  - Store：`src/stores/course.ts`（示例 `useCourseStore`）
    - `fetchCourses` action：调用 `listCourses`，保存 `items`、`total`、`page`、`size`
  - 视图：教师端 `src/features/teacher/views/ManageCourseView.vue`
    - 搜索框与分页器联动 → 触发 `fetchCourses`
    - 渲染课程卡片列表
- 验证要点：
  - 分页器切换页码/大小，请求参数变化正确
  - 空态、加载态、异常态提示清晰

时序图：
```mermaid
sequenceDiagram
  participant U as User
  participant V as Vue(ManageCourseView)
  participant ST as Store(useCourseStore)
  participant A as Axios
  participant C as Controller(/courses)
  U->>V: 搜索/翻页
  V->>ST: fetchCourses(params)
  ST->>A: GET /api/courses?page=...&size=...
  A->>C: 请求
  C-->>A: ApiResponse<PageResult<Course>>
  A-->>ST: data
  ST-->>V: items/total/page/size
  V-->>U: 渲染课程列表
```

---

## 示例 3：AI 助手对话（POST /api/ai/chat）

- 后端接口：`POST /api/ai/chat`
  - 请求体（要点）：`messages[]`、可选 `conversationId`、可选 `studentIds[]`
  - 响应体：`{ code: 200, data: { answer, conversationId, messageId } }`
- 前端对应文件：
  - 请求封装：`src/api/ai.api.ts`（示例方法：`chat(payload)`）
  - Store：`src/stores/ai.ts`（示例 `useAiStore`）
    - 管理当前会话 ID、消息列表、loading 状态
  - 视图：`src/features/shared/views/AiAssistantView.vue`（或同名位置）
    - 输入框回车/点击发送 → 调用 `chat` → 渲染助手回答
- 验证要点：
  - 首次会话 `conversationId` 为空时，后端会新建会话并返回 ID
  - 后续轮次复用 `conversationId` 追加消息
  - 401 时应自动跳转到登录页（拦截器行为）

时序图：
```mermaid
sequenceDiagram
  participant U as User
  participant V as Vue(AiAssistantView)
  participant ST as Store(useAiStore)
  participant A as Axios
  participant C as Controller(/ai)
  participant S as AiService
  participant P as Provider(DeepSeek/OpenRouter)
  U->>V: 输入消息并发送
  V->>ST: chat(messages)
  ST->>A: POST /api/ai/chat
  A->>C: 请求
  C->>S: generateAnswer
  S->>P: 调用模型
  P-->>S: answer
  S-->>C: 保存消息 & 返回
  C-->>A: ApiResponse<{answer,conversationId,messageId}>
  A-->>ST: data
  ST-->>V: 追加到消息列表
  V-->>U: 展示助手回答
```

---

## 示例 4：通知流（GET /api/notifications 或轮询/流）

- 后端接口：`GET /api/notifications`（实际以后端为准）
- 前端对应文件：
  - 组合式函数：`src/composables/useNotificationStream.ts`
  - 组件：`src/components/notifications/NotificationBell.vue`、`NotificationCenter.vue`
  - Store：`src/stores/notifications.ts`
- 验证要点：
  - 新通知能被感知并高亮提示
  - 点击查看后变为已读

时序图：
```mermaid
sequenceDiagram
  participant V as Vue(NotificationBell)
  participant CO as Composable(useNotificationStream)
  participant A as Axios
  participant C as Controller(/notifications)
  loop 轮询/事件
    V->>CO: start()
    CO->>A: GET /api/notifications/my?page=1&size=...
    A->>C: 请求
    C-->>A: ApiResponse<PageResult<Notification>>
    A-->>CO: data
    CO-->>V: 更新未读数/列表
  end
  V->>A: PUT /api/notifications/{id}/read
  A->>C: 标记已读
  C-->>A: ok
  A-->>V: 更新状态
```

---

## 约定与类型对齐

- 统一响应：`{ code, message, data }`，业务成功 `code=200`
- 分页体 `PageResult<T>`：前端 Store 应保存 `items`、`total`、`page`、`size`
- TS 类型与后端 DTO 字段保持一致（`src/types/*`）

---

## 新增一个 E2E 功能的清单（Checklist）

1) 后端
   - 定义 DTO（request/response）
   - Controller 暴露端点，标注 Swagger 注解
   - Service 编写业务逻辑
   - Mapper + XML 实现数据访问
   - 在 Swagger 中验证通过
2) 前端
   - `src/types/` 增加/更新类型
   - `src/api/xxx.api.ts` 增加请求方法
   - `src/stores/xxx.ts` 增加 action/state/getters
   - 视图/组件联动 Store（加载/空态/异常态）
   - i18n 文案、路由守卫
3) 自测
   - 控制台无错误；网络请求参数/响应正确
   - 401/404/500 等异常路径有友好提示

---

## 快速排查指南
- 404：检查 URL（是否包含 `/api` 前缀）、代理与 Nginx 配置
- 401：token 是否在拦截器中附带；是否过期被清理
- 500：后端日志（`logs/application.log`）与异常栈
- 跨域：开发模式通过 Vite 代理 `/api`；生产需 Nginx 反代

---

## 示例 5：课程发布/下线（POST /api/courses/{id}/publish|unpublish）

- 前端：`ManageCourseView.vue` → `useCourseStore` → `course.api.ts`
- 后端：`CourseController` → `CourseService`

时序图：
```mermaid
sequenceDiagram
  participant T as Vue(ManageCourseView)
  participant ST as Store(useCourseStore)
  participant A as Axios
  participant C as CourseController
  T->>ST: publishCourse(id)
  ST->>A: POST /api/courses/{id}/publish
  A->>C: 请求
  alt 成功
    C-->>A: { code:200 }
    A-->>ST: ok
    ST-->>T: 更新状态=已发布
  else 409/403
    C-->>A: error
    A-->>ST: 抛错
    ST-->>T: 提示“状态冲突/无权限”
  end
```

---

## 示例 6：师生绑定/解绑（teacher-student.api.ts）

- 前端：`TeacherStudentView.vue` → `useTeacherStore` → `teacher-student.api.ts`
- 后端：`TeacherStudentController`

时序图：
```mermaid
sequenceDiagram
  participant V as Vue(TeacherStudentView)
  participant ST as Store(useTeacherStore)
  participant A as Axios
  participant C as TeacherStudentController
  V->>ST: bindStudents(courseId, studentIds)
  ST->>A: POST /api/teacher-student/bind
  A->>C: 请求
  alt 成功
    C-->>A: { code:200 }
    A-->>ST: ok
    ST-->>V: 刷新名单
  else 400/403
    C-->>A: error
    A-->>ST: 抛错
    ST-->>V: 提示校验/权限错误
  end
```

---

## 示例 7：课程详情加载（GET /api/courses/{id}）

- 前端：`CourseDetailView.vue` → `useCourseStore` → `course.api.ts`
- 后端：`CourseController`

```mermaid
sequenceDiagram
  participant V as Vue(CourseDetailView)
  participant ST as Store(useCourseStore)
  participant A as Axios
  participant C as CourseController
  V->>ST: onMounted(load(id))
  ST->>A: GET /api/courses/{id}
  A->>C: 请求
  alt 成功
    C-->>A: { code:200, data: Course }
    A-->>ST: data
    ST-->>V: 渲染详情
  else 404
    C-->>A: error
    A-->>ST: 抛错
    ST-->>V: 跳回课程列表并提示
  end
```

---

## 示例 8：成绩批改与发布（POST /api/grades → /api/grades/{id}/publish）

- 前端：`GradeAssignmentView.vue` → `useTeacherStore` → `grade.api.ts`
- 后端：`GradeController`

```mermaid
sequenceDiagram
  participant V as Vue(GradeAssignmentView)
  participant ST as Store(useTeacherStore)
  participant A as Axios
  participant GC as GradeController
  V->>ST: gradeSubmission(payload)
  ST->>A: POST /api/grades { GradeRequest }
  A->>GC: 保存成绩
  GC-->>A: { id, isPublished:false }
  A-->>ST: 保存 gradeId
  V->>ST: publishGrade(id)
  ST->>A: POST /api/grades/{id}/publish
  A->>GC: 发布
  alt 成功
    GC-->>A: ok
    A-->>ST: 更新状态
    ST-->>V: 学生可见
  else 409/403
    GC-->>A: error
    A-->>ST: 抛错
    ST-->>V: 提示重试或权限不足
  end
```

---

## 示例 9：作业列表与筛选（GET /api/assignments）

- 前端：`AssignmentListView.vue` → `useAssignmentStore` → `assignment.api.ts`
- 后端：`AssignmentController`

```mermaid
sequenceDiagram
  participant V as Vue(AssignmentListView)
  participant ST as Store(useAssignmentStore)
  participant A as Axios
  participant C as AssignmentController
  V->>ST: fetchAssignments({page,size,status,keyword})
  ST->>A: GET /api/assignments?page=...&size=...&status=...&keyword=...
  A->>C: 请求
  C-->>A: ApiResponse<PageResult<Assignment>>
  A-->>ST: data
  ST-->>V: 渲染列表与分页
```

---

## 示例 10：通知中心标记已读/全部已读

- 前端：`NotificationCenter.vue` → `useNotificationStore` → `notification.api.ts`
- 后端：`NotificationController`

```mermaid
sequenceDiagram
  participant V as Vue(NotificationCenter)
  participant ST as Store(useNotificationStore)
  participant A as Axios
  participant C as NotificationController
  V->>ST: fetchMy(page,size)
  ST->>A: GET /api/notifications/my
  A->>C: 请求
  C-->>A: PageResult<Notification>
  A-->>ST: data
  ST-->>V: 展示未读数/列表
  V->>ST: markAsRead(id)
  ST->>A: PUT /api/notifications/{id}/read
  A->>C: 标记已读
  C-->>A: ok
  A-->>ST: 成功
  ST-->>V: 更新未读数
  V->>ST: markAllAsRead()
  ST->>A: PUT /api/notifications/read-all
  A->>C: 全部已读
  C-->>A: ok
  A-->>ST: 成功
  ST-->>V: 未读数=0
```

---

## 示例 11：文件上传与下载

- 前端：`FileManager.vue` → `useFileStore` → `file.api.ts`
- 后端：`FileController`

```mermaid
sequenceDiagram
  participant V as Vue(FileManager)
  participant ST as Store(useFileStore)
  participant A as Axios
  participant C as FileController
  V->>ST: upload(formData)
  ST->>A: POST /api/files/upload (multipart)
  A->>C: 上传
  alt 成功
    C-->>A: FileRecord
    A-->>ST: 保存记录
    ST-->>V: 列表头部显示
  else 400/413
    C-->>A: error
    A-->>ST: 抛错
    ST-->>V: 提示类型/大小限制
  end
  V->>ST: download(id)
  ST->>A: GET /api/files/{id}/download
  A->>C: 下载
  C-->>A: blob
  A-->>V: 触发保存
```

---

## 示例 12：课时进度异常重传（POST /api/lessons/{id}/progress）

- 前端：`LessonPlayer.vue` → `useLessonStore` → `lesson.api.ts`
- 后端：`LessonController`

```mermaid
sequenceDiagram
  participant V as Vue(LessonPlayer)
  participant ST as Store(useLessonStore)
  participant A as Axios
  participant C as LessonController
  V->>ST: onProgress(80)
  ST->>A: POST /api/lessons/{id}/progress
  A->>C: 上报进度
  alt 成功
    C-->>A: ok
    A-->>ST: 更新本地进度
    ST-->>V: 正常播放
  else 401/429/503
    C-->>A: error
    A-->>ST: 抛错
    ST-->>ST: 指数退避重试(1s→2s→5s→10s)
    ST-->>V: 提示“已暂存，将自动重传”
  end
```

---

## 示例 13：社区发帖与评论（POST /api/community/posts, POST /api/community/posts/{id}/comments）

- 前端：`CommunityView.vue` → `useCommunityStore` → `community.api.ts`
- 后端：`CommunityController`

```mermaid
sequenceDiagram
  participant V as Vue(CommunityView)
  participant ST as Store(useCommunityStore)
  participant A as Axios
  participant C as CommunityController
  V->>ST: createPost(content)
  ST->>A: POST /api/community/posts
  A->>C: 创建帖子
  C-->>A: { id, content, author, createdAt }
  A-->>ST: 更新 posts.unshift
  ST-->>V: 列表出现新帖子
  V->>ST: addComment(postId, content)
  ST->>A: POST /api/community/posts/{id}/comments
  A->>C: 创建评论
  C-->>A: { id, content, author, createdAt }
  A-->>ST: 更新 post.comments.unshift
  ST-->>V: 列表显示新评论
```

---

## 示例 14：教师仪表盘指标聚合（GET /api/teacher/dashboard）

- 前端：`DashboardView.vue` → `useDashboardStore` → `teacher.api.ts`
- 后端：`TeacherController`/`DashboardServiceImpl`

```mermaid
sequenceDiagram
  participant V as Vue(DashboardView)
  participant ST as Store(useDashboardStore)
  participant A as Axios
  participant C as TeacherController
  participant S as DashboardService
  V->>ST: fetchMetrics(range)
  ST->>A: GET /api/teacher/dashboard?range=7d
  A->>C: 请求
  C->>S: 聚合统计
  S-->>C: { kpis, trends }
  C-->>A: ApiResponse
  A-->>ST: data
  ST-->>V: 渲染卡片与趋势图
```

---

## 示例 15：学生能力雷达/趋势（GET /api/ability/student/{id}）

- 前端：`StudentAbilityView.vue` → `useStudentStore` → `ability.api.ts`
- 后端：`AbilityController`/相关服务

```mermaid
sequenceDiagram
  participant V as Vue(StudentAbilityView)
  participant ST as Store(useStudentStore)
  participant A as Axios
  participant C as AbilityController
  V->>ST: fetchAbility(studentId)
  ST->>A: GET /api/ability/student/{id}
  A->>C: 请求
  C-->>A: { radar, trends }
  A-->>ST: data
  ST-->>V: 渲染雷达与趋势图
```
