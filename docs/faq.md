# 常见问题（FAQ）与冒烟测试清单

## 学生端功能冒烟测试清单

- 核心路径
  - 我的课程 → 课程详情 → 小节详情 → 作业提交页
  - 路由应分别为：`/student/courses` → `/student/courses/:id` → `/student/lessons/:id` → `/student/assignments/:assignmentId/submit`

- 课程详情（CourseDetailView）
  - 是否展示课程进度、讲师信息（若后端提供）、课程资料下载列表
  - 小节列表支持“展开/收起”，展开后显示：
    - 资料列表（可下载）
    - 关联作业列表（可跳转至提交页）
  - 课程为空/无资料/无作业时的空态文案

- 小节详情（LessonDetailView）
  - 视频播放：
    - 文件ID资源：可正常播放；若 `<video>` 直链失败，将自动回退为 Blob URL
    - 受保护直链（/files/{id}/download）：优先 Blob；失败回退直链
    - HLS 链接（.m3u8）：Safari 原生可播；其它浏览器尝试动态加载 hls.js；失败显示“不支持 HLS”提示
    - 错误信息是否使用 i18n 文案（`student.courses.video.playFailed`/`student.courses.video.hlsNotSupported`）
  - 资料卡片：有则展示文档预览组件，无则显示“暂无资料”
  - 关联作业：按 lessonId 过滤展示；可跳转提交页；过期也应可进入（只读在提交页判断）
  - 学习笔记：可输入保存

- 作业页面（Assignments/AssignmentSubmit）
  - 列表过滤：状态、课程、搜索关键字
  - 可见性对齐：
    - 草稿（draft/crafted）：不显示
    - 定时（scheduled）：未到发布时间不显示；到时后显示
    - 已发布（published）：正常显示；过期仍保留入口
  - 提交页：已过截止/已提交/已评分时只读；“打回重做”且未过重交截止时可编辑

- 路由与权限
  - 未登录访问上述页面：应被重定向到登录
  - 非学生角色访问学生端路由：应被重定向到对应工作台

- 视觉一致性
  - 主要卡片、过滤器、按钮使用 `/components/ui` 下玻璃风格组件
  - 空态、错误态文案有中英双语

## 视频相关问题

- 问：为什么同一视频在某些浏览器无法播放？
  - 答：部分浏览器 `<video>` 请求不会带鉴权头，已采用 Blob 方式回退；HLS 在 Safari 原生支持，其它浏览器依赖 hls.js，若未能加载则显示“不支持 HLS”提示。

- 问：支持哪些视频格式？
  - 答：mp4、mov、webm、avi、mkv 等常见格式；HLS（.m3u8）根据浏览器支持情况播放。

## 可见性与路由

- 问：作业过期后为什么仍可从课程/小节进入？
  - 答：为保证学习留痕与复查路径，过期仍保留路由；提交页内会根据截止与提交状态进入只读模式。

## i18n 文案

- 本次新增：
  - `student.courses.video.playFailed`
  - `student.courses.video.hlsNotSupported`
  - `student.assignments.viewAll`

## 排查建议

- 数据为空检查：后端返回的字段命名差异（如 `lessonId`/`lesson_id`、`publishAt`/`publish_at`）
- 鉴权：本地开发环境跨域/鉴权导致 `<video>` 直链失效，优先验证 Blob 播放路径
- HLS：确认浏览器是否支持 HLS 或 hls.js 是否能被加载（网络/CDN/构建策略）
