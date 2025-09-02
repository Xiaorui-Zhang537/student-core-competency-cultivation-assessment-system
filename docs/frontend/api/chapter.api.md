# 前端 API：chapter.api.ts

## 方法签名
- `listByCourse(courseId)` → `GET /chapters/course/{courseId}`
- `create(data)` → `POST /chapters`
- `update(id, data)` → `PUT /chapters/{id}`
- `remove(id)` → `DELETE /chapters/{id}`

### 类型（节选）
```ts
export interface ChapterPayload {
  courseId: number;
  title: string;
  description?: string;
  orderIndex?: number;
}
```

## 最小示例
```ts
import { chapterApi } from '@/api/chapter.api'

// 列出课程下章节
const { data: list } = await chapterApi.listByCourse(2001)

// 创建
await chapterApi.create({ courseId: 2001, title: '绪论', orderIndex: 1 })

// 更新
await chapterApi.update(301, { title: '绪论（更新）' })

// 删除
await chapterApi.remove(301)
```

## 注意事项
- 章节属于课程，`courseId` 为必填
- 排序字段为 `orderIndex`，配合课时排序使用
- 敏感操作仅教师/管理员可用（以后端鉴权为准）

## 参数与返回
- `ChapterPayload`：`{ courseId: number; title: string; description?: string; orderIndex?: number }`
- 列表返回：课程下章节数组

## 示例
```ts
// 查询课程章节
const list = await chapterApi.listByCourse(2001)

// 创建章节
await chapterApi.create({ courseId: 2001, title: '第一章', orderIndex: 1 })

// 更新章节
await chapterApi.update(301, { title: '第一章（更新）' })

// 删除章节
await chapterApi.remove(301)
```

## 错误处理
- 400：参数不合法
- 401/403：未登录或权限不足
- 404：课程或章节不存在
