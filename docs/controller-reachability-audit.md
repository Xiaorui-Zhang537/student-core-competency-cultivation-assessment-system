---
title: Controller 可达性审计
description: 当前前端直连与弱链路后端端点清单（静态扫描）
outline: [2, 3]
---

# Controller 可达性审计

本页记录基于当前仓库代码的静态扫描结果：只判断“当前前端是否直接调用到”，不代表某个后端端点没有业务价值。

审计范围：

- `frontend/src/router/index.ts`
- `frontend/src/api/*.ts`
- `frontend/src` 内所有对 API 封装的直接调用
- `backend/src/main/java/com/noncore/assessment/controller/**/*.java`

## 1. 当前前端直连主链

以下控制器仍在当前前端主流程中，被页面、store 或原生 SSE 直接使用：

- `AuthController`
- `UserController`
- `CourseController` 的课程分页、详情、选课、学生管理等核心端点
- `LessonController`
- `StudentController`
- `AssignmentController`
- `SubmissionController`
- `GradeController`
- `AbilityController`
- `AbilityAnalyticsController`
- `BehaviorEvidenceController`
- `BehaviorInsightController`
- `AiController`
- `AiVoicePracticeController`
- `TeacherController`
- `TeacherStudentController`
- `CommunityController`
- `ChatController`
- `NotificationController`
- `NotificationStreamController`
- `HelpController`
- `FileController`
- 当前仍有页面接入的 `admin/*` 控制器

这些链路不应按“看起来冷门”直接删除。

## 2. 当前前端未直连，但建议保留的弱端点

以下端点在当前前端中没有直接调用或没有现成封装，但保留仍有价值，适合列为“保留候选”，而不是立刻删除：

- `POST /api/courses/{id}/unpublish`
  - 后端仍支持课程下架；当前前端没有直接按钮调用。
- `PUT /api/courses/batch-status`
  - 后端仍支持批量改状态；当前前端未使用。
- `GET /api/courses/statistics`
  - 后端仍支持课程统计；当前前端没有直接读取。
- `GET /api/admin/exports/course-students.csv`
  - 管理员导出端点仍可直接访问；当前前端已无独立封装。
- `GET /api/admin/exports/ai-conversations.csv`
  - 管理员 AI 会话导出；当前前端已无独立封装。
- `GET /api/admin/exports/voice-sessions.csv`
  - 管理员语音会话导出；当前前端已无独立封装。
- `POST /api/admin/ability/radar/compare`
  - 管理员能力雷达双区间对比；当前前端已无独立封装。
- `GET /api/admin/behavior/insights`
  - 管理员读取最新行为洞察；当前前端已无独立封装。
- `GET /api/admin/ai/voice/sessions/{sessionId}`
  - 管理员读取单条语音会话详情；当前前端已无独立封装。

这些端点当前更适合保留为“后台运维/直链/API 备用能力”，除非确认产品侧彻底不再需要。

## 3. 已确认可删的判断标准

只有同时满足下面条件，才适合继续删除：

- 当前前端无路由、无组件、无 store、无 API 封装调用
- 当前后端无主业务服务调用或只剩示例/兼容逻辑
- 不承担导出、审计、运维直链、SSE、后台工具用途

## 4. 下一轮建议

- 继续做“后端弱端点保留/删除候选”复核，但优先先补全当前仍在使用的真实业务逻辑。
- 前端优先清理“0 引用 API 封装、0 引用类型、0 引用页面/组件”，不要先删后端保留端点。

## 5. 前端死链清理记录（2026-03-05）

- 已删除确认 0 引用的工具文件：
  - `frontend/src/shared/utils/formatters.ts`
- 已删除确认 0 调用的 API 方法（前端封装层）：
  - `abilityApi.getStudentDashboard`
  - `abilityApi.getStudentTrends`
  - `abilityApi.getStudentRecommendations`
  - `abilityApi.getStudentReportHistory`
  - `aiApi.saveVoiceTurn`
  - `aiGradingApi.gradeEssayBatch`
  - `assignmentApi.closeAssignment`
  - `authApi.refreshToken`
  - `gradeApi.gradeBatchSubmissions`
  - `gradeApi.publishBatchGrades`
  - `gradeApi.getCourseGradeStatistics`
  - `gradeApi.getGradeTrend`
  - `helpApi.updateTicket`
  - `teacherApi.getAssignmentAnalytics`
  - `teacherApi.getAllCourseStudentsBasic`
  - `teacherApi.getAbilityWeights`
  - `teacherApi.updateAbilityWeights`
  - `voicePracticeApi.getSession`
- 已将教师端与管理员端重复的雷达展示格式化逻辑统一到：
  - `frontend/src/features/teacher/utils/radarDisplay.ts`
- 已删除确认 0 可达的旧页面文件：
  - `frontend/src/features/admin/views/AnalyticsView.vue`
  - `frontend/src/features/admin/views/DashboardView.vue`
  - `frontend/src/features/admin/views/ExportsView.vue`
  - `frontend/src/features/admin/views/UsersView.vue`
  - `frontend/src/features/teacher/views/EditCourseView.vue`
- 复核结果：
  - `courseApi` 与 `communityApi` 当前所有保留方法均在 `store` 或页面链路中有真实调用，不再继续删减。

## 6. 后端弱链路表审计（2026-03-05）

- `course_ability_weights`
  - 现状：后端主雷达聚合会读取该表（为空则默认等权）；`PUT /teachers/ability/weights` 可写入。
  - 结论：不是孤立表，但当前前端没有“权重编辑”入口，属于后端保留能力。
- `learning_recommendations`
  - 现状：`AbilityController` 与 `TeacherStudentController` 仍有建议读取接口，`AbilityServiceImpl/TeacherStudentServiceImpl` 仍会查询/生成该表。
  - 前端现状：主流程未直接消费该表（仅后端链路保留）。
  - 结论：当前判定为“弱链路保留”，不建议盲删表。
- `chat_message_attachments`
  - 现状：聊天发送接口会写入，消息查询会回填 `attachmentFileIds`，文件下载权限会回查该表。
  - 结论：处于主链路，不能删除。

本轮对以上三表未做结构删除；`schema.sql` 保持与运行链路一致。
