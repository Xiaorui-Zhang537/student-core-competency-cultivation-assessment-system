# 前端 API：lesson.api.ts

## 方法签名
- `getLessonsByCourse(courseId)` → `GET /lessons/course/{courseId}`
- `getLesson(lessonId)` → `GET /lessons/{lessonId}`
- `createLesson(data)` → `POST /lessons`
- `updateLesson(lessonId, data)` → `PUT /lessons/{lessonId}`
- `deleteLesson(lessonId)` → `DELETE /lessons/{lessonId}`
- `updateLessonContent(lessonId, payload)` → `PUT /lessons/{lessonId}/content`
  - `payload: { videoUrl?: string; materialFileIds?: number[] }`
- `updateLessonOrder(lessonId, order)` → `PUT /lessons/{lessonId}/order`
- `completeLesson(lessonId)` → `POST /lessons/{lessonId}/complete`
- `updateLessonProgress(lessonId, progress)` → `POST /lessons/{lessonId}/progress`

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
await lessonApi.updateLessonProgress('3001', 80)
```

## 注意事项
- 资料文件由 `file.api.ts` 上传，获得 `fileId` 后通过 `materialFileIds` 绑定
- 进度上报频率建议节流，避免产生大量请求
- 敏感操作需教师角色，具体以后端鉴权为准
