## 帮助入口

## 学生端：我的课程与节次详情

- 我的课程
  - 统计卡：已完成/进行中/平均进度（StartCard，玻璃样式）
  - 筛选：采用作业页一致的 FilterBar（关键词 + 分类）
  - 课程商店：支持直接报名；当课程开启密钥，报名需输入密钥

- 课程详情
  - 显示课程描述与进度
  - 右侧显示课程资料，支持下载
  - 节次条目使用“查看详情”进入节次详情页

- 节次详情
  - 左侧目录：显示同课程下所有节次，支持跳转
  - 视频播放：遵循教师端设置（允许拖动/倍速）；浏览器层面通过 `controlsList` 限制
  - 自动进度：每 5 秒上报当前进度、学习时长、最后位置
  - 学习笔记：底部文本框支持保存

## 教师端：课程与节次

- 学生管理：新增“设置入课密钥”，可开启是否需要密钥并设置新密钥（仅保存哈希）
- 课程详情：移除冗余“课程内容”块，仅保留“课程描述”和“节次编辑”
- 节次编辑：新增“允许拖动进度条/允许倍速”两个开关并保存

## 接口与类型

- 课程密钥
  - `PUT /api/courses/{id}/enroll-key` 设置开关与密钥
  - `POST /api/courses/{id}/enroll` 学生选课；如需密钥在 body 传 `{ enrollKey }`

- 节次播放与进度/笔记
  - `PUT /lessons/{id}/content` 载荷增加：`allowScrubbing`、`allowSpeedChange`
  - `POST /lessons/{id}/progress` 载荷：`{ progress?, studyTime?, lastPosition? }`
  - `POST /lessons/{id}/notes` 保存学习笔记

前端类型 `types/lesson.ts` 增加：`allowScrubbing?`、`allowSpeedChange?`、`orderIndex?`。

- 学生端与教师端：在右上角头像菜单新增“帮助”，指向 `/help`（无需登录可访问）。
- 登录页：在登录表单下新增“帮助中心”按钮，点击进入 `/help`。

实现要点：
- 路由新增：`/help` 映射到 `features/shared/views/HelpView.vue`。
- i18n：使用 `layout.common.help`，已在中英文 `layout.json` 中补充。

