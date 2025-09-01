# 前端 API：community.api.ts

## 方法签名（节选）
- `getPosts(params)` / `getPostById(id)` / `createPost(data)` / `updatePost(id, data)` / `deletePost(id)` / `likePost(id)`
- `getCommentsByPostId(postId, params)` / `createComment(postId, content, parentId?)` / `likeComment(commentId)` / `deleteComment(commentId)`
- `getCommunityStats()` / `getHotTopics(limit?)` / `getActiveUsers(limit?)` / `getMyPosts(params)` / `searchTags(keyword, limit?)`

## 注意事项
- 编辑/删除仅作者可执行；点赞接口可切换状态
