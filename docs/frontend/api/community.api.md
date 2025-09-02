# 前端 API：community.api.ts

## 方法签名（节选）
- `getPosts(params)` / `getPostById(id)` / `createPost(data)` / `updatePost(id, data)` / `deletePost(id)` / `likePost(id)`
- `getCommentsByPostId(postId, params)` / `createComment(postId, content, parentId?)` / `likeComment(commentId)` / `deleteComment(commentId)`
- `getCommunityStats()` / `getHotTopics(limit?)` / `getActiveUsers(limit?)` / `getMyPosts(params)` / `searchTags(keyword, limit?)`

## 注意事项
- 编辑/删除仅作者可执行；点赞接口可切换状态

## 参数与返回
- 分页：`{ page?: number; size?: number }`
- 列表筛选：`{ category?: string; keyword?: string; orderBy?: 'latest'|'popular'|'hot'|'comments'|'likes'|'views' }`
- 评论分页与排序：`{ page?: number; size?: number; parentId?: number; orderBy?: 'time'|'hot' }`
- 类型：`Post` / `PostComment` / `Tag` / `CommunityStats` / `HotTopic` / `ActiveUser`（见 `@/types/community`）

## 示例
```ts
// 帖子列表
const posts = await communityApi.getPosts({ page: 1, size: 20, orderBy: 'hot' })

// 详情 + 评论
const detail = await communityApi.getPostById(123)
const comments = await communityApi.getCommentsByPostId(123, { page: 1, size: 50, orderBy: 'time' })

// 发帖与编辑
await communityApi.createPost({ title: '标题', content: '正文', tags: ['前端'] } as any)
await communityApi.updatePost(123, { title: '新标题' })

// 点赞/取消点赞
const { liked } = await communityApi.likePost(123)

// 统计/热门/活跃
const stats = await communityApi.getCommunityStats()
const topics = await communityApi.getHotTopics(10)
const active = await communityApi.getActiveUsers(10)
```

## 错误处理与权限
- 401：未登录
- 403：非作者编辑/删除
- 404：帖子或评论不存在
