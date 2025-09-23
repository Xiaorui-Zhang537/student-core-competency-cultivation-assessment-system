# 前端页面序列（关键页面结构）

## 学生端 · 课程/小节/作业 路由串联（v0.2.3）

- 我的课程：`/student/courses`
- 课程详情：`/student/courses/:id`
  - 展示：课程进度、讲师信息、课程资料
  - 列表：课程小节（支持展开显示【资料+关联作业】）
  - 跳转：小节详情 `/student/lessons/:id`
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


