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
- `addLessonNotes(lessonId, notes)` → `POST /lessons/{lessonId}/notes`（学生）

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
