# 前端 API：lesson.api.ts

## 方法签名
- `getLessonsByCourse(courseId)` → `GET /lessons/course/{courseId}`
- `getLesson(lessonId)` → `GET /lessons/{lessonId}`
- `createLesson(data)` → `POST /lessons`
- `updateLesson(lessonId, data)` → `PUT /lessons/{lessonId}`
- `deleteLesson(lessonId)` → `DELETE /lessons/{lessonId}`
- `updateLessonContent(lessonId, payload)` → `PUT /lessons/{lessonId}/content`
  - `payload: { videoUrl?: string; materialFileIds?: number[]; allowScrubbing?: boolean; allowSpeedChange?: boolean }`
- `updateLessonOrder(lessonId, order)` → `PUT /lessons/{lessonId}/order`
- `completeLesson(lessonId)` → `POST /lessons/{lessonId}/complete`
- `updateLessonProgress(lessonId, payload)` → `POST /lessons/{lessonId}/progress`（学生）
  - `payload: { progress?: number; studyTime?: number; lastPosition?: number }`
## 2025-09-24 更新（节次详情合成进度/只增不减）

- 合成规则：
  - 视频：基于 `<video>` 的 `timeupdate/ended` 推导百分比
  - 资料：每个文件滚动到底部（IntersectionObserver threshold=1.0）计为“已查看”；资料进度=已查看/总数
  - 综合：二者平均；仅其一则采用该项；均无则初始化为 100%
- 上报策略：
  - 进入时先读取后端持久化（`GET /students/lessons/{id}`）并显示
  - 自动上报（5s）与即时上报（视频结束/资料到底）均采用“只增不减”：仅当新合成进度>已存进度才 `POST /lessons/{id}/progress`
  - 课程详情统计：通过 `GET /lessons/course/{courseId}/student-progress` 拉取整课节次的进度，用于“已完成计数”和每节徽章

- `addLessonNotes(lessonId, notes)` → `POST /lessons/{lessonId}/notes`（学生）

### 角色与权限

- 创建/更新/删除课时：教师或具有相应角色
- 进度与笔记：学生（需登录），仅能操作/查看自己的进度与笔记
- 内容编辑字段：`allowScrubbing/allowSpeedChange` 会影响学生端播放器行为

## 最小示例
```ts
import { lessonApi } from '@/api/lesson.api'

// 获取课程下的课时
const { data: lessons } = await lessonApi.getLessonsByCourse('2001')

// 创建课时（教师）
const created = await lessonApi.createLesson({ courseId: 2001, title: '第一课', order: 1 } as any)

// 更新内容（视频+资料）
await lessonApi.updateLessonContent(String(created.data.id), {
  videoUrl: 'https://example.com/intro.mp4',
  materialFileIds: [9876]
})

// 学生完成课时
await lessonApi.completeLesson('3001')

// 更新学习进度（0-100）
await lessonApi.updateLessonProgress('3001', { progress: 80, lastPosition: 120, studyTime: 30 })

// 保存笔记
await lessonApi.addLessonNotes('3001', '本节重点：……')
```

## 注意事项
- 资料文件由 `file.api.ts` 上传，获得 `fileId` 后通过 `materialFileIds` 绑定
- 进度上报频率建议节流，避免产生大量请求
- 敏感操作需教师角色，具体以后端鉴权为准
 - 字段约定：`content` 表示“本节说明/正文”，`description` 为“简介摘要”。前端创建/编辑时会同步两者，后端也做互补（任一为空将被补齐为另一方）。学生端列表展示 `description || content`

## 参数与返回
- `Lesson` 字段参见 `@/types/lesson`
- `updateLessonContent` 载荷：`{ videoUrl?: string; materialFileIds?: number[] }`

## 进阶示例
```ts
// 排序
await lessonApi.updateLessonOrder('3001', 2)

// 局部更新课时
await lessonApi.updateLesson('3001', { title: '新标题' })

// 学生端进度上报（节流）
import throttle from 'lodash.throttle'
const report = throttle((progress:number)=> lessonApi.updateLessonProgress('3001', progress), 1500)
```

## 错误处理
- 400：参数校验失败（顺序/进度范围 0-100）
- 401：未登录；
- 403：非教师更新/删除课时
