# 页面级时序图（Component ↔ Store ↔ API ↔ Backend）

> 用于快速理解关键页面从组件到后端的完整调用链。

## PageHeader 渲染时机
- 所有采用 PageHeader 的页面，会在组件模板初始化阶段立即渲染标题与副标题；右上角 actions 插槽里的筛选/按钮与后续数据加载并行。
- 建议：不要将需要后端返回的数据作为标题文本来源，标题文案走 i18n；动态数据只放在 actions 或主体区。

## 登录页（LoginView）
```mermaid
sequenceDiagram
  participant V as Vue(LoginView)
  participant S as AuthStore
  participant A as auth.api.ts
  participant B as AuthController

  V->>S: onSubmit(credentials)
  S->>A: login(data)
  A->>B: POST /auth/login
  alt 成功
    B-->>A: { token, user }
    A-->>S: 设置会话
    S-->>V: 跳转首页
  else 401/400
    B-->>A: error(401/400)
    A-->>S: 抛错
    S-->>V: 显示错误toast
  end
```

## 课程列表（CourseListView）
```mermaid
sequenceDiagram
  participant V as Vue(CourseListView)
  participant S as CourseStore
  participant A as course.api.ts
  participant B as CourseController

  V->>S: onMounted()
  S->>A: getCourses({ page, size, query })
  A->>B: GET /courses
  alt 成功
    B-->>A: PageResult<Course>
    A-->>S: commit 列表
    S-->>V: 渲染分页
  else 空列表
    B-->>A: { items:[], total:0 }
    A-->>S: 空态
    S-->>V: 显示空状态/引导
  else 5xx/网络
    B-->>A: error
    A-->>S: 抛错
    S-->>V: 显示错误并提供“重试”
    V->>S: 重试
    S->>A: getCourses(...)
  end
```

## 作业详情（AssignmentDetailView）
```mermaid
sequenceDiagram
  participant V as Vue(AssignmentDetailView)
  participant S as AssignmentStore
  participant A as assignment.api.ts
  participant B as AssignmentController

  V->>S: load(assignmentId)
  S->>A: getAssignment(id)
  A->>B: GET /assignments/{id}
  alt 成功
    B-->>A: Assignment
    A-->>S: 保存详情
    S-->>V: 渲染
  else 404
    B-->>A: not found
    A-->>S: 抛错
    S-->>V: 显示“未找到”并返回列表
  end
```

## 通知中心（NotificationCenter）
```mermaid
sequenceDiagram
  participant V as Vue(NotificationCenter)
  participant S as NotificationStore
  participant A as notification.api.ts
  participant B as NotificationController
  participant SSE as NotificationStream

  V->>S: init()
  S->>A: getMyNotifications()
  A->>B: GET /notifications/my
  alt 成功
    B-->>A: PageResult
    A-->>S: 保存列表
    S-->>V: 渲染
  else 401
    B-->>A: error 401
    A-->>S: 抛错
    S-->>V: 跳登录
  end
  V->>S: subscribe()
  S->>SSE: GET /notifications/stream
  alt SSE 成功
    SSE-->>S: event:new-message
    S-->>V: 刷新未读/列表
  else SSE 断开
    SSE-->>S: error/close
    S-->>V: 指示“尝试重连”
    V->>S: 重连逻辑（指数退避）
  end
```

## 学生作业列表（AssignmentsView）
```mermaid
sequenceDiagram
  participant V as Vue(AssignmentsView)
  participant A as student.api.ts
  participant B as StudentController

  V->>A: getAssignments({ courseId?, status?, q?, page, size })
  A->>B: GET /students/assignments
  alt 成功
    B-->>A: PageResult<Assignment>
    A-->>V: 渲染列表/分页
  else 空列表
    B-->>A: { items:[], total:0 }
    A-->>V: 显示空状态
  else 401/403
    B-->>A: error
    A-->>V: 跳登录/无权限提示
  end
```

## 学生提交-草稿-评分展示（AssignmentSubmitView）
```mermaid
sequenceDiagram
  participant V as Vue(AssignmentSubmitView)
  participant SA as submission.api.ts
  participant GA as grade.api.ts
  participant SC as SubmissionController
  participant GC as GradeController

  V->>SA: POST /assignments/{id}/draft { content, fileIds }
  SA->>SC: 保存草稿
  SC-->>SA: OK
  SA-->>V: 通知“草稿已保存”

  V->>SA: POST /assignments/{id}/submit { content, fileIds }
  SA->>SC: 提交作业
  SC-->>SA: { id, status: submitted }
  SA-->>V: 更新状态为 submitted

  Note over V,GA: 当状态为 graded 时
  V->>GA: GET /grades/student/{sid}/assignment/{id}
  GA->>GC: 查询成绩
  GC-->>GA: { score, feedback, gradedAt }
  GA-->>V: 渲染成绩块
```

## AI 聊天（AiChatView）
```mermaid
sequenceDiagram
  participant V as Vue(AiChatView)
  participant S as AiStore
  participant A as ai.api.ts
  participant B as AiController

  V->>S: send(message)
  S->>A: chat({ messages, conversationId? })
  A->>B: POST /ai/chat
  alt 成功
    B-->>A: { answer, conversationId }
    A-->>S: append 消息
    S-->>V: 渲染应答
  else 400/429/5xx
    B-->>A: error
    A-->>S: 抛错
    S-->>V: 显示错误，提供“稍后再试/切换模型”
  end
```

## 文件管理（FileManagerView）
```mermaid
sequenceDiagram
  participant V as Vue(FileManager)
  participant S as FileStore
  participant A as file.api.ts
  participant B as FileController

  V->>S: upload(file)
  S->>A: uploadFile(formData)
  A->>B: POST /files/upload
  alt 成功
    B-->>A: FileRecord
    A-->>S: 保存
    S-->>V: 显示卡片
  else 400/413
    B-->>A: error(类型/大小)
    A-->>S: 抛错
    S-->>V: 显示校验提示
  end
  V->>S: download(id)
  S->>A: downloadFile(id)
  alt 成功
    A-->>V: Blob → 触发下载
  else 401/403/404
    A-->>S: 抛错
    S-->>V: 显示鉴权或不存在提示
  end
```

## 举报表单（ReportFormView）
```mermaid
sequenceDiagram
  participant V as Vue(ReportForm)
  participant S as ReportStore
  participant A as report.api.ts
  participant B as ReportController

  V->>S: submit(payload)
  S->>A: createReport(data)
  A->>B: POST /reports
  alt 成功
    B-->>A: { id, status }
    A-->>S: 保存
    S-->>V: 显示编号/跳转
  else 400/403
    B-->>A: error
    A-->>S: 抛错
    S-->>V: 显示校验/权限提示
  end
```

## 学生课时进度（LessonPlayerView）
```mermaid
sequenceDiagram
  participant V as Vue(LessonPlayer)
  participant S as LessonStore
  participant A as lesson.api.ts
  participant B as LessonController

  V->>S: onMounted(lessonId)
  S->>A: getLesson(lessonId)
  A->>B: GET /lessons/{id}
  alt 成功
    B-->>A: Lesson
    A-->>S: 保存
    S-->>V: 渲染播放器
  else 404
    B-->>A: not found
    A-->>S: 抛错
    S-->>V: 返回课程页
  end
  V->>S: onProgress(80)
  S->>A: updateLessonProgress(id, 80)
  A->>B: POST /lessons/{id}/progress
  alt 成功
    B-->>A: ok
    A-->>S: 更新本地进度
  else 401/5xx
    B-->>A: error
    A-->>S: 抛错
    S-->>V: 暂存本地并提示“稍后重传”
  end
```

## 教师端：从提交选择 → AI 批改（TeacherAIGradingView）
```mermaid
sequenceDiagram
  participant V as Vue(TeacherAIGradingView)
  participant AA as assignment.api.ts
  participant SA as submission.api.ts
  participant AG as aiGrading.api.ts
  participant C as AiController

  Note over V: 读取路由中的 courseId（面包屑保持上下文）

  V->>AA: getAssignmentsByCourse(courseId, { page, size })
  AA->>C: GET /assignments?courseId={courseId}
  C-->>AA: PageResult<Assignment>
  AA-->>V: 渲染“作业选择”（GlassPopoverSelect）

  V->>SA: getSubmissionsByAssignment(assignmentId, { page, size })
  SA->>C: GET /assignments/{assignmentId}/submissions
  C-->>SA: PageResult<Submission>
  SA-->>V: 渲染“提交选择”（GlassPopoverSelect，可预览）

  alt 以文本内容加入队列
    Note over V: 从 Submission 读取 content 作为作文文本
    V->>AG: gradeEssay({ messages:[{role:'user', content:text}], model:'google/gemini-2.5-pro' })
    AG->>C: POST /ai/grade/essay
    C->>C: 调用 Gemini(JSON-only)，解析结构化结果
    C-->>AG: { overall, dimensions, suggestions, ... }
    AG-->>V: 入队并在顶部动态进度条反映 completed/total
  else 以提交文件加入队列
    Note over V: 读取 Submission.fileIds 作为输入
    V->>AG: gradeFiles({ fileIds, model:'google/gemini-2.5-pro' })
    AG->>C: POST /ai/grade/files
    C->>C: 下载文件→抽取文本→Gemini(JSON-only)→保存历史
    C-->>AG: { results:[{ fileId, fileName, result|error }] }
    AG-->>V: 入队并更新顶部动态进度条
  end

  Note over V: 历史入口（带 courseId 上下文）
  V->>AG: listHistory({ q, page, size })
  AG->>C: GET /ai/grade/history
  C-->>AG: PageResult<AiGradingHistory>
  AG-->>V: 列表（移除ID列，左侧 page-size 玻璃选择器，右侧 Prev/Next + Page N 文案 i18n）

  V->>AG: getHistoryDetail(id)
  AG->>C: GET /ai/grade/history/{id}
  C-->>AG: AiGradingHistory(rawJson...)
  AG-->>V: 详情弹窗（标题为文件名，内部滚动隐藏滚动条，可导出 Text/PNG/PDF[单页]）

  V->>AG: deleteHistory(id)
  AG->>C: DELETE /ai/grade/history/{id}
  alt 环境禁用 DELETE
    AG->>C: POST /ai/grade/history/{id}/delete
  end
  C-->>AG: ok
  AG-->>V: 刷新列表
```

### UI 与 i18n 约定
- 作业选择器标签：`teacher.aiGrading.picker.assignment`
- 学生/提交选择器标签：`teacher.aiGrading.picker.submission`（或 `...student`，与实现保持一致）
- 请选择占位：`common.pleaseSelect`
- 分页“Page N”文本需 i18n：`common.pageX`（或项目已存在键）
