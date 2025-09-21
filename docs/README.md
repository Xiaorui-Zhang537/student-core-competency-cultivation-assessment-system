# 新人上手手册（导航）

欢迎来到“学生核心能力培养评估系统”。本手册面向完全新手：不需要预先了解项目或相关技术，按顺序阅读并动手实践，你将能在本地跑起来并逐步理解系统。

## 建议阅读路线
1. `quickstart-beginner.md`：零基础快速上手，先跑通项目（包含环境安装、数据库初始化、前后端启动、Swagger 验证）
2. `architecture-overview.md`：端到端架构与一次请求的生命周期（含鉴权/数据流时序图）
3. `file-walkthrough.md`：逐文件导览（关键文件逐条说明与调用链）
4. `e2e-examples.md`：端到端样例（登录/课程/AI/通知等时序图）
5. 深入：`backend-deep-dive.md`、`frontend-deep-dive.md`
6. 参考：API 参考、模型与配置、安全配置、Cookbook、FAQ、术语、团队约定
7. `learning-path.md`：1–2 周成长路线（配实践任务）

## 角色路线
- 学生端路线（Beta）：`quickstart` → `frontend-deep-dive` → `student.api.ts` → 完成 Cookbook 任务A/C
- 教师端路线（稳定）：`quickstart` → `backend-deep-dive` → `teacher.api.ts` → 完成 任务B/E/D

## 先决条件清单
- 已安装：Java 17、Maven、Node.js 18+、MySQL 8.x（Redis 可选）
- 数据库：已创建并导入 `schema.sql`
- 后端已启动：`http://localhost:8080/api`
- 前端已启动：`http://localhost:5173`（或自定义端口）
- 文档站点（可选）：`http://localhost:4174`

## 计划（Planning）建议
- 明确变更目标：新接口/页面/报表/图表
- 列出相关文件：Controller/Service/Mapper/Entity/DTO 与前端 API/Store/视图/类型
- 画出链路草图（Mermaid）确定数据流向与边界条件
- 拆分任务并估时：后端/前端/联调/文档/测试
- 风险评估：权限/性能/一致性/可扩展性

## 常用入口
- Swagger：`http://localhost:8080/api/swagger-ui.html`
- 前端开发：`http://localhost:5173`
- 文档站点（本地）：`http://localhost:4174`

## 相关技术官方链接
- Java：`https://dev.java/`
- Spring Boot：`https://spring.io/projects/spring-boot`
- MyBatis：`https://mybatis.org/`
- Spring Security：`https://spring.io/projects/spring-security`
- SpringDoc OpenAPI：`https://springdoc.org/`
- Vue 3：`https://vuejs.org/`
- TypeScript：`https://www.typescriptlang.org/`
- Vite：`https://vitejs.dev/`
- Pinia：`https://pinia.vuejs.org/`
- Tailwind CSS：`https://tailwindcss.com/`
- Axios：`https://axios-http.com/`
- ECharts：`https://echarts.apache.org/`

> 提示：本手册中的命令默认在 macOS / Linux 环境下执行；Windows 可用 PowerShell 等价命令。


## 变更记录（Changelog）

### 教师端 - 学生详情画像/活跃度/预警/建议与能力雷达（2025-09-19）
- 新增端点：
  - `GET /api/teachers/students/{studentId}` 返回用户完整画像与汇总指标
  - `GET /api/teachers/students/{studentId}/activity` 近 7/30 天活跃度与最近学习
  - `GET /api/teachers/students/{studentId}/alerts` 风险预警
  - `GET /api/teachers/students/{studentId}/recommendations` 个性化建议
- 前端：`StudentDetailView.vue` 展示档案、联系方式、课程/平均分/完成率/最近活跃、最近学习、能力雷达、预警与建议
- API 文档：更新 `docs/backend/api/teacher.md` 与 `docs/frontend/api/teacher-student.api.md`
- i18n：补充中英文文案（teacher.studentDetail.*）

### 教师端 - AI 批改弹窗改造与上传忽略策略（2025-09-17）
- 弹窗交互重构（`features/teacher/views/TeacherAIGradingView.vue`）
  - 选择器：移除课程选择，基于页面 `courseId` 自动加载当前课程的作业；新增“作业选择”“学生”两个选择器，水平排列且标题不换行，选择器自适应宽度（clamp）。
  - 预览：选中学生后展示提交文本与附件列表；支持“使用文本内容”勾选、附件多选、全选附件、清空选择；“添加已选”将勾选内容加入批改队列。
  - 路由参数支持：`courseId`、`assignmentId`、`studentId`、`openPicker`，可从“作业/提交”页带参跳转并自动打开弹窗与预选。
  - 组件与样式：`GlassPopoverSelect` 新增 `truncateLabel` 属性（默认 true），批改弹窗设置为 `false` 以避免“...” 截断；按钮统一加入图标与主题色背景（主色/靛蓝/蓝绿/红色等）。
- 接口保持不变（前端复用 `assignmentApi.getAssignments[ByCourse]`、`submissionApi.getSubmissionsByAssignment`、`fileApi.getRelatedFiles`、`aiGradingApi.gradeFiles/gradeEssay`）。
- i18n：新增/补齐键
  - 批改弹窗：`teacher.aiGrading.picker.{assignment,student,preview,useText,attachments,selectAllFiles,clearFiles,text,reload}`
  - 学生管理入课密钥：`teacher.students.enrollKey.{title,require,keyLabel,placeholder,tip,save,saved,failed}` 与 `teacher.students.actions.setEnrollKey`
- 版本控制与上传策略：根 `.gitignore` 合并冲突已修复，并新增忽略上传目录的规则：`/upload/`, `/uploads/`, `backend/uploads/`（避免将实际上传的文件纳入版本控制）。

### 学生端 - 我的课程/课程详情/节次详情 + 入课密钥与播放控制（2025-09-11）
- 新功能：
  - 教师端“学生管理”支持设置课程入课密钥（是否需要密钥、密钥哈希保存）
  - 学生端“我的课程”统计卡（StartCard）与 FilterBar 筛选对齐“我的作业”
  - 课程详情：展示描述与进度、右侧课程资料下载、节次按钮统一“查看详情”
  - 节次详情页：左侧目录、遵循教师设置的播放限制（拖动/倍速）、每 5 秒自动进度上报、学习笔记
- 端点同步：
  - `PUT /api/courses/{id}/enroll-key`（教师设置密钥）
  - `POST /api/courses/{id}/enroll`（学生选课，Body 可带 `{ enrollKey }`）
  - `PUT /lessons/{id}/content` 载荷新增 `allowScrubbing/allowSpeedChange`
  - `POST /lessons/{id}/progress`、`POST /lessons/{id}/notes`
- 文档：已更新 `docs/backend/api/course.md` 与 `docs/frontend/api/lesson.api.md`

### 界面标题与简介统一组件（PageHeader）（2025-09-03）
- 统一前台页面的标题与简介展示，采用 `components/ui/PageHeader.vue` 复用组件；支持 actions 插槽放置筛选器与操作按钮；组件在 `main.ts` 已全局注册。
- 学生端：Dashboard、Assignments、AssignmentSubmit、Courses、CourseDetail、Analysis、Analytics、Grades、Community、Ability（本次新增）、AIAssistant（共享）。
- 教师端：Dashboard、ManageCourse、CourseDetail、CourseStudents、Analytics、ReviewAssignment、AssignmentSubmissions、GradeAssignment、StudentDetail、EditCourse。
- 共享：Community、AIAssistant、Notifications、NotificationDetail、PostDetail、CourseDiscovery、Help。
- 使用方式示例：
  - 基础用法：`<PageHeader :title="t('xxx.title')" :subtitle="t('xxx.subtitle')" />`
  - 携带操作区：
    ```vue
    <PageHeader :title="pageTitle" :subtitle="pageSubtitle">
      <template #actions>
        <Button variant="primary">操作</Button>
      </template>
    </PageHeader>
    ```

### 学生端 - 我的作业（2025-09-02）
- 列表页（AssignmentsView）：对齐筛选/搜索/分页与状态映射；新增错误提示与重试按钮；样式统一为玻璃风格。
- 提交页（AssignmentSubmitView）：统一标题+描述与状态徽章；支持草稿与提交按钮禁用逻辑；上传/删除附件流程完善；已评分时按需拉取并展示分数与教师反馈；补齐 i18n 文案（中英）。

### 教师端/学生端 - AI 批改与五维雷达接入（2025-09-19）
- 教师评分页（GradeAssignmentView）：
  - 新增“生成AI建议”按钮，自动从提交附件或文本调用后端 `POST /ai/grade/files|essay`，前端进行结构化规范化，展示“建议分/准确性/完整性/清晰度/整体分析/改进点”。
  - 新增“一键写入五维能力分”，将AI四维映射至五维（道德认知/学习态度/学习能力/学习方法），写入 `POST /ability/teacher/assessment`，学习成绩维度在发布成绩后按百分比折算自动写入。
- 学生作业提交页（AssignmentSubmitView）：
  - 在“已评分”区域下方展示“AI能力报告（最近一次）”，显示综合分、四维条与综合建议，来源于 `/ability/student/report/latest`（若后端返回JSON字符串字段会在前端解析）。
- i18n：补充了教师端“写入五维能力分”、学生端“AI能力报告（最近一次）/综合评分”等文案。
- 文档：补充交互案例、页面时序与 `students/assignments` 参数说明。
