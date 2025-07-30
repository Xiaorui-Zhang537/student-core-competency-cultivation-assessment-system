import apiRequest from './config'

export interface PostData {
  id: string
  title: string
  content: string
  category: string
  author: {
    id: string
    name: string
    avatar: string
  }
  tags: string[]
  pinned: boolean
  views: number
  replies: number
  likes: number
  liked: boolean
  createdAt: string
  lastReply?: {
    author: string
    createdAt: string
  }
  sampleReplies?: Array<{
    id: string
    author: string
    content: string
    createdAt: string
  }>
}

export interface CommunityStats {
  totalPosts: number
  activeMembers: number
  todayPosts: number
  totalViews: number
}

export interface HotTopic {
  id: string
  name: string
  rank: number
  postCount: number
}

export interface ActiveUser {
  id: string
  name: string
  avatar: string
  postCount: number
}

export interface CreatePostRequest {
  title: string
  content: string
  category: string
  tagsInput: string
  anonymous?: boolean
  allowComments?: boolean
}

export interface CreateCommentRequest {
  content: string
  parentId?: number
}

// 获取帖子列表
export const getPostList = (params: {
  page?: number
  size?: number
  category?: string
  keyword?: string
  orderBy?: string
}) => {
  return apiRequest.get('/api/community/posts', { params })
}

// 获取帖子详情
export const getPostDetail = (id: string) => {
  return apiRequest.get(`/api/community/posts/${id}`)
}

// 发布帖子
export const createPost = (data: CreatePostRequest) => {
  return apiRequest.post('/api/community/posts', data)
}

// 点赞/取消点赞帖子
export const likePost = (id: string) => {
  return apiRequest.post(`/api/community/posts/${id}/like`)
}

// 发表评论
export const createComment = (postId: string, data: CreateCommentRequest) => {
  return apiRequest.post(`/api/community/posts/${postId}/comments`, data)
}

// 获取评论列表
export const getCommentList = (postId: string, params: {
  page?: number
  size?: number
}) => {
  return apiRequest.get(`/api/community/posts/${postId}/comments`, { params })
}

// 获取社区统计
export const getCommunityStats = () => {
  return apiRequest.get('/api/community/stats')
}

// 获取热门话题
export const getHotTopics = (limit = 10) => {
  return apiRequest.get('/api/community/hot-topics', { params: { limit } })
}

// 获取活跃用户
export const getActiveUsers = (limit = 10) => {
  return apiRequest.get('/api/community/active-users', { params: { limit } })
}

// 获取我的帖子
export const getMyPosts = (params: {
  page?: number
  size?: number
}) => {
  return apiRequest.get('/api/community/my-posts', { params })
}

// 搜索标签
export const searchTags = (keyword: string, limit = 10) => {
  return apiRequest.get('/api/community/search/tags', { params: { keyword, limit } })
} 