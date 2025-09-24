# 前端页面序列（关键页面结构）

## 学生端 · 课程/小节/作业 路由串联（v0.2.4）

- 我的课程：`/student/courses`
- 课程详情：`/student/courses/:id`
  - 布局：两行双列（7/3 + 7/3）
    - 第一排：
      - 左（7）：课程元信息卡 `CourseMetaCard`
        - 标题、描述
        - 开课时间/结课时间（玻璃 Badge）
        - 难度/分类/标签（统一玻璃 Badge，标签无标题）
        - 进度条（复用 `/components/ui/Progress.vue`）
        - 已报名学生（头像+姓名；点击打开资料弹窗，含“联系TA”→聊天抽屉）
      - 右（3）：教师信息卡 `TeacherInfoCard`（头像、姓名、头衔/简介、联系老师跳转聊天）
    - 第二排：
      - 左（7）：章节/课时列表 `ChapterLessonList`（按章节分组，支持查看/展开）
      - 右（3）：课程资料 `MaterialsCard`（按 `course_material` 关联文件展示下载）
  - 数据来源：
    - 课程详情：`GET /api/courses/{id}`
    - 课时列表：`GET /api/lessons/course/{courseId}`
    - 课程资料：`GET /api/files/related?purpose=course_material&relatedId={courseId}`
    - 参与者：`GET /api/students/courses/{courseId}/participants` → `teachers` / `classmates`
    - 联系（老师/学生）：前端 `chat.openChat(peerId, peerName, courseId)`
  - 跳转：小节详情 `/student/lessons/:id`
- 2025-09-24 补充：
  - 面包屑统一：课程详情 `我的课程 > 课程名称`；小节详情 `我的课程 > 课程名称 > 节次标题`
  - 课程内容卡：每节使用 `rounded-xl glass-ultraThin`；标题与说明之间 `mt-1.5`；右侧按进度展示 `已完成/未完成` 徽章
  - 小节详情：左侧目录树条目圆角（选中态描边）、章行 `py-2.5` 与标题 `pl-2`；右侧卡片标题与正文 `mb-4`
  - 进度：视频+资料合成并“只增不减”上报；进入优先显示后端持久化；无视频与资料首次进入自动完成
- 小节详情：`/student/lessons/:id`
  - 中部卡片：视频（支持文件ID/直链/HLS，失败回退 Blob）、资料列表、学习笔记、关联作业
  - 关联作业：跳转 `/student/assignments/:assignmentId/submit`，即使过期仍可进入查看
- 我的作业：`/student/assignments`
  - 过滤：状态、课程、关键字
  - 查看/提交：`/student/assignments/:id/submit`

### 可见性规则（与教师端对齐）

- 草稿（draft/crafted）：学生端不显示
- 定时（scheduled）：发布时间未到不显示；到时后显示
- 已发布（published）：正常显示；过期后仍保留路由入口，进入为只读

### 视频播放策略

- 文件ID或受保护直链：优先以 Blob 方式加载，规避 <video> 不带鉴权头导致失败
- HLS：Safari 原生优先；其它浏览器尝试动态加载 `hls.js`，失败则提示不支持
- 回退与提示：统一 i18n 文案 `student.courses.video.playFailed` 与 `student.courses.video.hlsNotSupported`

## 学生端 · 作业提交页（v0.2.2）

结构自上而下：

1) 顶部双卡片（并排）
   - 左：作业信息（标题、描述、发布日期、截止日期、状态徽章）
   - 右：教师附件列表（文件名/大小/下载）

2) 中部双卡片（并排）
   - 左：提交内容（`GlassTextarea`）
   - 右：上传附件（`FileUpload` + 已上传列表）
   - 下方：操作按钮（保存草稿、提交）

3) 底部
   - 成绩卡（动画分值条、等级、评分时间、教师反馈/优点/改进）
   - AI 能力报告卡（综合评分、维度条、建议；支持查看详情与导出）

响应式：`md` 及以上两列，小屏单列堆叠。

实现位置：`frontend/src/features/student/views/AssignmentSubmitView.vue`

复用组件：
- `features/shared/AssignmentInfoCard.vue`
- `features/shared/AttachmentList.vue`
- `components/ui/Card.vue`

国际化：新增 `student.assignments.publish`（发布日期）。


